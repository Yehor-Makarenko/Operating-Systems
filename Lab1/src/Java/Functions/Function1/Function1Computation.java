package Java.Functions.Function1;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;

import Java.Functions.FunctionResult;

public class Function1Computation {
  public static void main(String[] args) throws Exception {
    AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
    Future<Void> result = client.connect(new InetSocketAddress("127.0.0.1", 1234));
    result.get();
    FunctionResult res = new FunctionResult(10+1);
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(res);
    ByteBuffer buffer = ByteBuffer.wrap(bos.toByteArray());
    Future<Integer> writeval = client.write(buffer);
    writeval.get();
  }

  public static void main(int n) throws Exception {
    
    // return new FunctionResult(n + 1);
  }
}
