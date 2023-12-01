package lab1.manager;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.Future;

import lab1.functions.functionResult.FunctionResult;

public class Manager {
  public static void compute(int n) throws Exception { 
    ProcessBuilder pb1 = new ProcessBuilder("java.exe", "-cp", "labs/target/classes", "lab1.functions.function1.FunctionComputation", String.valueOf(n));        
    ProcessBuilder pb2 = new ProcessBuilder("java.exe", "-cp", "labs/target/classes", "lab1.functions.function2.FunctionComputation", String.valueOf(n));            
    pb1.start();
    pb2.start();
          
    AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
    server.bind(new InetSocketAddress("127.0.0.1", 1234));          
    Future<AsynchronousSocketChannel> acceptCon = server.accept();
    AsynchronousSocketChannel client1 = acceptCon.get();
    acceptCon = server.accept();
    AsynchronousSocketChannel client2 = acceptCon.get();
    ByteBuffer buffer = ByteBuffer.allocate(1024);

    System.out.println("get1");
    Future<Integer> readResult = client1.read(buffer);
    readResult.get();
    // byte[] rb = buffer.array();
    // ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(rb));
    // FunctionResult res1 = (FunctionResult) ois.readObject();    
    System.out.println("get1");
    
    buffer.flip();

    System.out.println("get2");
    readResult = client2.read(buffer);
    readResult.get();
    // rb = buffer.array();
    // ois = new ObjectInputStream(new ByteArrayInputStream(rb));
    // FunctionResult res2 = (FunctionResult) ois.readObject();    
    System.out.println("get2");
         
    buffer.clear();
    client1.close();
    client2.close();

    // if (!res1.getIsResult()) {
    //   res1.getError().print();
    //   return;
    // }
    // if (!res2.getIsResult()) {
    //   res2.getError().print();
    //   return;
    // }

    // int f1 = (int)res1.getResult();
    // int f2 = (int)res2.getResult();
    // int res = f1 ^ f2;
    // System.out.println(res);
    System.out.println("finish");
  }  
}