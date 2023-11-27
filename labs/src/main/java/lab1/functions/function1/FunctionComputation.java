package lab1.functions.function1;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;

import lab1.functions.FunctionResult;

public class FunctionComputation {
  public static void main(String[] args) throws Exception {    
    System.out.println(args[0]);
    AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
    Future<Void> result = client.connect(new InetSocketAddress("127.0.0.1", 1234));    
    result.get();
    FunctionResult res = new FunctionResult(Integer.parseInt(args[0])+1);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(res);
    ByteBuffer buffer = ByteBuffer.wrap(bos.toByteArray());
    client.write(buffer);
  }
}