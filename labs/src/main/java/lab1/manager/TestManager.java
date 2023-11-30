package lab1.manager;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import lab1.functions.FunctionResult;

public class TestManager {
  public static void compute(int n) throws Exception { 
    Properties props = new Properties();
    try {
      props.load(new FileInputStream("labs/src/main/resources/config.properties"));
    } catch (IOException e) {      
      e.printStackTrace();
    }
    int timeLimit = Integer.parseInt(props.getProperty("time_limit"));
    ProcessBuilder pb1 = new ProcessBuilder("java.exe", "-cp", "labs/target/classes", "lab1.functions.function1.FunctionComputation", String.valueOf(n));        
    // ProcessBuilder pb2 = new ProcessBuilder("java.exe", "-cp", "labs/target/classes", "lab1.functions.function2.FunctionComputation", String.valueOf(n));            
    // pb2.start();
          
    AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
    server.bind(new InetSocketAddress("127.0.0.1", 1234));          

    Process p1 = pb1.start();    

    Future<AsynchronousSocketChannel> acceptCon = server.accept();
    AsynchronousSocketChannel client1 = acceptCon.get();
    
    CompletableFuture<FunctionResult> future = new CompletableFuture<>();
    future.orTimeout(timeLimit, TimeUnit.MILLISECONDS);
    future.whenComplete((result, throwable) -> {
      if (throwable == null) {
        if (result.getIsResult()) {
          System.out.println("Result: " + result.getResult());
        } else {
          result.getError().print();
        }
      } else {
        System.out.println("Time exceeded");
        try {
          client1.close();
          p1.destroy();
        } catch (IOException e) {          
          e.printStackTrace();
        }
      }
    });

    ByteBuffer buffer = ByteBuffer.allocate(1024);
    
    String message = "Report";    
    Thread.sleep(150);
    Future<Integer> result = client1.write(ByteBuffer.wrap(message.getBytes()));
    result.get();
    
    result = client1.read(buffer);
    result.get();

    buffer.flip();
    byte[] rb = new byte[buffer.remaining()];
    buffer.get(rb);
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(rb));
    FunctionResult res = (FunctionResult) ois.readObject();
    res.getError().print();

    // message = "Close";    
    // Thread.sleep(150);
    // result = client1.write(ByteBuffer.wrap(message.getBytes()));  
    // result.get();
    
    // buffer.clear();
    // result = client1.read(buffer);
    // result.get();

    // buffer.flip();
    // rb = new byte[buffer.remaining()];
    // buffer.get(rb);
    // ois = new ObjectInputStream(new ByteArrayInputStream(rb));
    // res = (FunctionResult) ois.readObject();
    // res.getError().print();
         
    buffer.clear();
    result = client1.read(buffer);
    try {
      result.get();
      buffer.flip();
      rb = new byte[buffer.remaining()];
      buffer.get(rb);
      ois = new ObjectInputStream(new ByteArrayInputStream(rb));
      res = (FunctionResult) ois.readObject();
      future.complete(res);
      client1.close();
      p1.destroy();
    } catch (Exception e) {
    }
    buffer.clear();
    System.out.println("finish");
  }  
}
