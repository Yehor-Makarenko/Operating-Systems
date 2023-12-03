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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

import lab1.Main;
import lab1.functions.functionResult.FunctionResult;

public class Manager {
  private int timeLimit;    
  private int n;
  private Map<Integer, FunctionResult> errorsCache;
  private Semaphore canFinish;
  private FunctionResult result1;
  private FunctionResult result2;
  private AsynchronousServerSocketChannel server;
  private AsynchronousSocketChannel client1;
  private AsynchronousSocketChannel client2;
  private Process p1;
  private Process p2;
  private CompletableFuture<FunctionResult> futureClient1;
  private CompletableFuture<FunctionResult> futureClient2;
  private boolean hasResult;
  private boolean isTimeExceeded;
  private boolean isUserCanceled;
  private boolean isCanceled;
  private boolean isDone;
  private JFrame frame;
  private JButton menuButton;
  private JPanel mainPanel;
  private JTextArea output;

  public Manager(JFrame frame) {
    Properties props = new Properties();
    try {
      props.load(new FileInputStream("labs/src/main/resources/config.properties"));
    } catch (IOException e) {      
      e.printStackTrace();
    }
    timeLimit = Integer.parseInt(props.getProperty("time_limit"));    
    errorsCache = new HashMap<>();   
    this.frame = frame;  
    client1 = null;
    client2 = null;
    init();
  }

  private void init() {
    canFinish = new Semaphore(2);
    result1 = null;
    result2 = null;
    hasResult = true;
    isTimeExceeded = false;
    isUserCanceled = false;
    isCanceled = false;
    isDone = false;
  }

  public void open() {
    ProcessBuilder pb1 = new ProcessBuilder("java.exe", "-cp", "labs/target/classes", "lab1.functions.function1.FunctionFComputation", "f");        
    ProcessBuilder pb2 = new ProcessBuilder("java.exe", "-cp", "labs/target/classes", "lab1.functions.function2.FunctionGComputation", "g");            
    
    try {
      server = AsynchronousServerSocketChannel.open();
      server.bind(new InetSocketAddress("127.0.0.1", 1234));        
      p1 = pb1.start();          
    } catch (IOException e) {      
      e.printStackTrace();
    }            

    Future<AsynchronousSocketChannel> accept = server.accept();
    try {
      client1 = accept.get();
    } catch (InterruptedException | ExecutionException e) {      
      e.printStackTrace();
    }
    try {
      p2 = pb2.start();
    } catch (IOException e) {      
      e.printStackTrace();
    }  
    accept = server.accept();
    try {
      client2 = accept.get();
    } catch (InterruptedException | ExecutionException e) {      
      e.printStackTrace();
    }

    readMessage(client1, ByteBuffer.allocate(1024));
    readMessage(client2, ByteBuffer.allocate(1024));
  }

  public void compute(int n) {  
    this.n = n; 
    init();

    if (errorsCache.containsKey(n)) {
      createUI();
      menuButton.setEnabled(false);
      output.append("Get from cache: cannot get result. Report:" + errorsCache.get(n) + "\n\n"); 
      addReturnButton();
      return;
    }                
    
    futureClient1 = setTimeLimit(client1);
    futureClient2 = setTimeLimit(client2);
    
    sendN();
    
    createUI();
  }  

  private CompletableFuture<FunctionResult> setTimeLimit(AsynchronousSocketChannel client) {
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

  private void sendN() {
    try {
      client1.write(ByteBuffer.wrap("Start".getBytes())).get();
      client2.write(ByteBuffer.wrap("Start".getBytes())).get();
      client1.write(ByteBuffer.wrap(String.valueOf(n).getBytes())).get();
      client2.write(ByteBuffer.wrap(String.valueOf(n).getBytes())).get();
    } catch (InterruptedException | ExecutionException e) {      
      e.printStackTrace();
    }    
  }

  private void readMessage(AsynchronousSocketChannel client, ByteBuffer buffer) {
    client.read(buffer, null, new CompletionHandler<Integer, Void>() {
      @Override
      public void completed(Integer result, Void attachment) {
        if (!client.isOpen()) return;
        FunctionResult res = getFunctionResult(buffer);
        readMessage(client, buffer); 

        if (res.getIsComputed()) {              
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
        throw new UnsupportedOperationException("Unimplemented method 'failed'");
      }
    });
  }

  private void handleResult(FunctionResult result) {  
    if (!result.hasResult()) {
      hasResult = false;
    }

    if (result.getFunctionName().equals("f") && !isTimeExceeded) {
      futureClient1.complete(result);
    } else if (result.getFunctionName().equals("g") && !isTimeExceeded) {
      futureClient2.complete(result);
    }

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
      } else if (!result.hasResult()) {
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
      } else if (!result.hasResult()) {
        stopCalculations();
      }
    }
    canFinish.release(2);
  }  

  private void getReport() {
    String message = "Report";
    client1.write(ByteBuffer.wrap(message.getBytes()));
    client2.write(ByteBuffer.wrap(message.getBytes()));
  }

  private void stopCalculations() {  
    if (isCanceled) {
      canFinish.release(2);
      return;
    }  
    isCanceled = true;
    String message = "Close";
    client1.write(ByteBuffer.wrap(message.getBytes()));
    client2.write(ByteBuffer.wrap(message.getBytes()));
  }

  public void close() {
    try {
      server.close();
      client1.close();
      client2.close();
    } catch (IOException e) {      
      e.printStackTrace();
    }    
    p1.destroy();
    p2.destroy();
  }

  private FunctionResult getFunctionResult(ByteBuffer buffer) {
    buffer.flip();
    byte[] rb = new byte[buffer.remaining()];
    buffer.get(rb);
    buffer.clear();
    ObjectInputStream ois = null;
    try {
      ois = new ObjectInputStream(new ByteArrayInputStream(rb));
    } catch (IOException e) {
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

  private void createUI() {        
    output = new JTextArea();   
    output.setEditable(false);
    menuButton = new JButton("Open menu");    
    JLabel label = new JLabel("Output:");
    JScrollPane scrollPane = new JScrollPane(output);
    mainPanel = new JPanel(null);
    
    menuButton.addActionListener(e -> {
      openMenu();
    });
        
    menuButton.setBounds(200, 150, 100, 30);   
    label.setBounds(50, 270, 400, 30);
    scrollPane.setBounds(50, 300, 400, 400);
  
    mainPanel.add(menuButton);  
    mainPanel.add(label);
    mainPanel.add(scrollPane);
    frame.setContentPane(mainPanel);
    frame.revalidate();
    frame.repaint();
  }

  private void openMenu() {
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

  private void finishCalculations() {
    menuButton.setEnabled(false);

    if (isDone) {
      return;
    }
    isDone = true;

    if (result1.getError().getIsCritical()) {
      errorsCache.put(n, result1);
    } else if (result2.getError().getIsCritical()) {
      errorsCache.put(n, result2);
    }

    if (isUserCanceled) {
      output.append("User has canceled computations. Report:" + result1 + result2 + "\n\n");      
    } else if (isTimeExceeded) {
      output.append("Time exceeded. Report:" + result1 + result2 + "\n\n");      
    } else if (!hasResult) {
      output.append("Cannot get result. Report:" + result1 + result2 + "\n\n");      
    } else {
      output.append("Result:\nf(" + n + ") = " + result1.getResult() + "\n");
      output.append("g(" + n + ") = " + result2.getResult() + "\n");
      int res = (int) result1.getResult() ^ (int) result2.getResult();
      output.append("f(" + n + ")XORg(" + n + ") = " + res + "\n\n");
    }

    addReturnButton();
  }

  private void addReturnButton() {
    JButton returnButton = new JButton("Return");        

    returnButton.addActionListener(e -> {
      Main.start();
    });

    returnButton.setBounds(200, 220, 100, 30);
    mainPanel.add(returnButton);
    frame.revalidate();
    frame.repaint();
  }
}
