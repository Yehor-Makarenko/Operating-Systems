package lab1.manager;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Selector;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import lab1.functions.FunctionResult;

public class Manager {
  public static void compute(int n) throws Exception {
    Properties props = new Properties();
    props.load(new FileInputStream(new File("labs/src/main/java/lab1/functions/function1/max_errors.properties")));
    System.out.println(props.getProperty("aa"));        
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

    client1.read(buffer);
    byte[] rb = buffer.array();
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(rb));
    FunctionResult res = (FunctionResult) ois.readObject();
    System.out.println(res.getResult());

    buffer.flip();

    client2.read(buffer);
    rb = buffer.array();
    ois = new ObjectInputStream(new ByteArrayInputStream(rb));
    res = (FunctionResult) ois.readObject();
    System.out.println(res.getResult());
     
    buffer.clear();
    client1.close();
    client2.close();
  }
}