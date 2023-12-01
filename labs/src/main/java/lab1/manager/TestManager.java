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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import lab1.functions.functionResult.FunctionResult;

public class TestManager {
  private static int timeLimit;    
  private static boolean canFinish = true;
  private static FunctionResult result1;
  private static FunctionResult result2;
  private static AsynchronousSocketChannel client1;
  private static AsynchronousSocketChannel client2;
  private static Process p1;
  private static Process p2;

  public static void compute(int n) throws Exception {     
    Properties props = new Properties();
    try {
      props.load(new FileInputStream("labs/src/main/resources/config.properties"));
    } catch (IOException e) {      
      e.printStackTrace();
    }
    timeLimit = Integer.parseInt(props.getProperty("time_limit"));
    ProcessBuilder pb1 = new ProcessBuilder("java.exe", "-cp", "labs/target/classes", "lab1.functions.function1.FunctionFComputation", String.valueOf(n));        
    // ProcessBuilder pb2 = new ProcessBuilder("java.exe", "-cp", "labs/target/classes", "lab1.functions.function2.FunctionComputation", String.valueOf(n));            
    // pb2.start();
          
    AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
    server.bind(new InetSocketAddress("127.0.0.1", 1234));          

    p1 = pb1.start();    

    server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
      @Override
      public void completed(AsynchronousSocketChannel client, Void attachment) {
        client1 = client;
        CompletableFuture<FunctionResult> clientFuture = setTimeLimit(client, p1);        
        readMessage(client, ByteBuffer.allocate(1024), clientFuture);
      }

      @Override
      public void failed(Throwable exc, Void attachment) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'failed'");
      }
      
    });

    while (p1.isAlive()) {
      
    }

    if (result1.hasResult()) {
      System.out.println("Result: " + result1.getResult());          
    }
    result1.getError().print();

    // server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
    //   @Override
    //   public void completed(AsynchronousSocketChannel client, Void attachment) {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'completed'");
    //   }

    //   @Override
    //   public void failed(Throwable exc, Void attachment) {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'failed'");
    //   }
      
    // });  
    
    
  }  

  private static CompletableFuture<FunctionResult> setTimeLimit(AsynchronousSocketChannel client, Process process) {
    CompletableFuture<FunctionResult> future = new CompletableFuture<>();
    future.orTimeout(timeLimit, TimeUnit.HOURS);
    future.whenComplete((result, throwable) -> {
      if (throwable == null) {
        handleResult(result);
      } else {
        System.out.println("Time exceeded");
      }
      try {
        client.close();
        process.destroy();
      } catch (IOException e) {          
        e.printStackTrace();
      }
    });

    return future;
  }

  private static void readMessage(AsynchronousSocketChannel client, ByteBuffer buffer, CompletableFuture<FunctionResult> futureClient) {
    client.read(buffer, null, new CompletionHandler<Integer, Void>() {
      @Override
      public void completed(Integer result, Void attachment) {
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
        futureClient.complete(res);             
        readMessage(client, buffer, futureClient); 
      }

      @Override
      public void failed(Throwable exc, Void attachment) {
        // TODO Auto-generated method stub
      }
      
    });
  }

  private static void handleResult(FunctionResult result) {
    if (result.getFunctionName().equals("f")) {
      result1 = result;
      if (!canFinish) {
        try {
          TestManager.class.wait();
        } catch (InterruptedException e) {          
          e.printStackTrace();
        }
      }
      try {
        client1.close();
        p1.destroy();
      } catch (IOException e) {        
        e.printStackTrace();
      }
    }
  }
}
