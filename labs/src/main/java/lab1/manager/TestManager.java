package lab1.manager;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

import lab1.functions.functionResult.FunctionResult;

public class TestManager {
  private static int timeLimit;    
  private static int n;
  private static Semaphore canFinish = new Semaphore(2);
  private static FunctionResult result1;
  private static FunctionResult result2;
  private static AsynchronousSocketChannel client1;
  private static AsynchronousSocketChannel client2;
  private static Process p1;
  private static Process p2;
  private static boolean hasResult = true;
  private static boolean isTimeExceeded = false;
  private static boolean isUserCanceled = false;
  private static boolean isCanceled = false;
  private static boolean isDone = false;
  private static JFrame frame;
  private static JButton menuButton = new JButton("Open menu");   ;
  private static JPanel mainPanel;
  private static JTextArea output = new JTextArea();

  public static void compute(JFrame frame, int n) throws Exception {  
    TestManager.n = n; 
    TestManager.frame = frame;  
    Properties props = new Properties();
    try {
      props.load(new FileInputStream("labs/src/main/resources/config.properties"));
    } catch (IOException e) {      
      e.printStackTrace();
    }
    timeLimit = Integer.parseInt(props.getProperty("time_limit"));
    ProcessBuilder pb1 = new ProcessBuilder("java.exe", "-cp", "labs/target/classes", "lab1.functions.function1.FunctionFComputation", String.valueOf(n));        
    ProcessBuilder pb2 = new ProcessBuilder("java.exe", "-cp", "labs/target/classes", "lab1.functions.function2.FunctionGComputation", String.valueOf(n));            
    
    AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
    server.bind(new InetSocketAddress("127.0.0.1", 1234));          
    
    p1 = pb1.start();    
    p2 = pb2.start();

    server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
      @Override
      public void completed(AsynchronousSocketChannel client, Void attachment) {
        server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
          @Override
          public void completed(AsynchronousSocketChannel client, Void attachment) {
            client2 = client;
            CompletableFuture<FunctionResult> clientFuture = setTimeLimit(client);        
            readMessage(client, ByteBuffer.allocate(1024), clientFuture);
          }

          @Override
          public void failed(Throwable exc, Void attachment) {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Unimplemented method 'failed'");
          }
          
        });

        client1 = client;
        CompletableFuture<FunctionResult> clientFuture = setTimeLimit(client);        
        readMessage(client, ByteBuffer.allocate(1024), clientFuture);
      }

      @Override
      public void failed(Throwable exc, Void attachment) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'failed'");
      }
      
    });    
    
    createUI();
  }  

  private static CompletableFuture<FunctionResult> setTimeLimit(AsynchronousSocketChannel client) {
    CompletableFuture<FunctionResult> future = new CompletableFuture<>();
    future.orTimeout(timeLimit, TimeUnit.MILLISECONDS);
    future.whenComplete((result, throwable) -> {
      if (throwable == null) {
        return;
      } 
      isTimeExceeded = true;
      try {
        canFinish.acquire(2);
      } catch (InterruptedException e) {					
        e.printStackTrace();
      }        
      stopCalculations();      
    });

    return future;
  }

  private static void readMessage(AsynchronousSocketChannel client, ByteBuffer buffer, CompletableFuture<FunctionResult> futureClient) {
    client.read(buffer, null, new CompletionHandler<Integer, Void>() {
      @Override
      public void completed(Integer result, Void attachment) {
        if (!client.isOpen()) return;
        FunctionResult res = getFunctionResult(buffer);
        readMessage(client, buffer, futureClient); 

        if (res.getIsComputed()) {  
          if (!isTimeExceeded) {
            futureClient.complete(res);
          }      
          if (isUserCanceled || isTimeExceeded) {
            canFinish.release();
          }
          handleResult(res);
        } else {
          output.append("Report:" + res + "\n\n");          
          canFinish.release();   
        }        
      }

      @Override
      public void failed(Throwable exc, Void attachment) {
        // TODO Auto-generated method stub
      }
      
    });
  }

  private static void handleResult(FunctionResult result) {  
    try {
      canFinish.acquire(2);
    } catch (InterruptedException e) {          
      e.printStackTrace();
    }

    if (result.getFunctionName().equals("f")) {             
      if (result1 != null) {
        canFinish.release(2);
        return;
      }        
      result1 = result; 

      if (result2 != null) {
        finishCalculations();
        try {
          client1.close();
          client2.close();
          p1.destroy();
          p2.destroy();
        } catch (IOException e) {        
          e.printStackTrace();
        }
      } else if (!result.hasResult()) {
        hasResult = false;
        stopCalculations();        
      } 
    } else if (result.getFunctionName().equals("g")) {           
      if (result2 != null) {
        canFinish.release(2);
        return;
      }         
      result2 = result;

      if (result1 != null) {
        finishCalculations();
        try {
          client1.close();
          client2.close();
          p1.destroy();
          p2.destroy();
        } catch (IOException e) {        
          e.printStackTrace();
        }
      } else if (!result.hasResult()) {
        hasResult = false;
        stopCalculations();
      }
    }
    canFinish.release(2);
  }  

  private static void getReport() {
    String message = "Report";
    client1.write(ByteBuffer.wrap(message.getBytes()));
    client2.write(ByteBuffer.wrap(message.getBytes()));
  }

  private static void stopCalculations() {  
    if (isCanceled) {
      canFinish.release(2);
      return;
    }  
    isCanceled = true;
    String message = "Close";
    client1.write(ByteBuffer.wrap(message.getBytes()));
    client2.write(ByteBuffer.wrap(message.getBytes()));
  }

  private static FunctionResult getFunctionResult(ByteBuffer buffer) {
    buffer.flip();
    byte[] rb = new byte[buffer.remaining()];
    buffer.get(rb);
    buffer.clear();
    ObjectInputStream ois = null;
    try {
      ois = new ObjectInputStream(new ByteArrayInputStream(rb));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    FunctionResult res = null;
    try {
      res = (FunctionResult) ois.readObject();
    } catch (ClassNotFoundException | IOException e) {          
      e.printStackTrace();
    }
    return res;
  }

  private static void createUI() {           
    output.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(output);
    mainPanel = new JPanel(null);
    
    menuButton.addActionListener(e -> {
      openMenu();
    });

    frame.setSize(500, 800);           
    menuButton.setBounds(200, 150, 100, 30);    
    scrollPane.setBounds(50, 300, 400, 400);
  
    mainPanel.add(menuButton);  
    mainPanel.add(scrollPane);
    frame.setContentPane(mainPanel);
    frame.revalidate();
    frame.repaint();
    
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(null);
    frame.setVisible(true);   
  }

  private static void openMenu() {
    try {
      canFinish.acquire(2);
    } catch (InterruptedException e) {      
      e.printStackTrace();
    }
    JButton reportButton = new JButton("Report status");  
    JButton cancelButton = new JButton("Cancel computation");    
    JPanel panel = new JPanel(null);

    reportButton.addActionListener(e -> {
      getReport();
      frame.setContentPane(mainPanel);
      frame.revalidate();
      frame.repaint();
    });

    cancelButton.addActionListener(e -> {
      isUserCanceled = true;
      stopCalculations();
      frame.setContentPane(mainPanel);
      frame.revalidate();
      frame.repaint();
    });

    reportButton.setBounds(100, 150, 300, 30);   
    cancelButton.setBounds(100, 220, 300, 30); 

    panel.add(reportButton);
    panel.add(cancelButton);
    frame.setContentPane(panel);
    frame.revalidate();
    frame.repaint();
  }

  private static void finishCalculations() {
    if (isDone) {
      return;
    }
    isDone = true;

    menuButton.setEnabled(false);

    if (isUserCanceled) {
      output.append("User has canceled computations. Report:" + result1 + result2 + "\n\n");      
    } else if (isTimeExceeded) {
      output.append("Time exceeded. Report:" + result1 + result2 + "\n\n");      
    } else if (!hasResult) {
      output.append("Cannot get result. Report:" + result1 + result2 + "\n\n");      
    } else {
      output.append("Result:\nf(" + n + ") = " + result1.getResult() + "\n");
      output.append("g(" + n + ") = " + result1.getResult() + "\n");
      int res = (int) result1.getResult() ^ (int) result2.getResult();
      output.append("f(" + n + ")XORg(" + n + ") = " + res + "\n\n");
    }
  }
}
