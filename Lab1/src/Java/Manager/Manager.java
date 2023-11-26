package Java.Manager;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import Java.Functions.FunctionResult;
import Java.Functions.Function1.Function1Computation;
import Java.Functions.Function2.Function2Computation;

public class Manager {
  public static void compute(int n) throws Exception {
    AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
    server.bind(new InetSocketAddress("127.0.0.1", 1234));
    Future<AsynchronousSocketChannel> acceptCon = server.accept();
    AsynchronousSocketChannel client = acceptCon.get();
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    Future<Integer> readVal = client.read(buffer);
    byte[] rb = buffer.array();
    System.out.println("Received from client: " + new String(rb).trim());
    System.out.println(readVal.get());
    // System.out.println(readVal.get());
    // System.out.println("Received from client: " + new String(buffer.array()).trim());
    // byte[] data = new byte[buffer.remaining()];
    // System.out.println(readVal.get());
    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(rb));
    FunctionResult res = (FunctionResult) ois.readObject();
    System.out.println(res.getResult());
  }
}
