package lab1.functions.function2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import lab1.functions.FunctionError;
import lab1.functions.FunctionResult;

public class FunctionComputation {
  private static int n;
  private static FunctionError error = new FunctionError("g");
  private static ByteBuffer buffer;
  private static AsynchronousSocketChannel client;

  public static void main(String[] args) throws Exception {        
    n = Integer.parseInt(args[0]);
    client = AsynchronousSocketChannel.open();
    Future<Void> result = client.connect(new InetSocketAddress("127.0.0.1", 1234));    
    result.get();
    buffer = ByteBuffer.allocate(1024);
    Thread.sleep(2000);
    client.write(buffer);

    // new Thread(() -> {
    //   FunctionResult res = getFunctionResult();
    //   ByteArrayOutputStream bos = new ByteArrayOutputStream();
    //   ObjectOutputStream oos;
    //   try {
    //     oos = new ObjectOutputStream(bos);
    //     oos.writeObject(res);
    //   } catch (IOException e) {        
    //     e.printStackTrace();
    //   }      
    //   synchronized (buffer) {
    //     buffer = ByteBuffer.wrap(bos.toByteArray());
    //     Future<Integer> writeResult = client.write(buffer);
    //     try {
    //       writeResult.get();
    //     } catch (InterruptedException | ExecutionException e) {        
    //       e.printStackTrace();
    //     }
    //   }      
    // }).start();

    // while (true) {
    //   Future<Integer> readResult = client.read(buffer);
    //   String command = new String(buffer.array()).trim();
    //   readResult.get();
    //   buffer.flip();
    //   if (command == "Report") {        
    //     ByteArrayOutputStream bos = new ByteArrayOutputStream();
    //     ObjectOutputStream oos = new ObjectOutputStream(bos);
    //     oos.writeObject(new FunctionResult(error));        
    //     synchronized (buffer) {
    //       buffer = ByteBuffer.wrap(bos.toByteArray());
    //       Future<Integer> writeResult = client.write(buffer);
    //       writeResult.get();
    //     }        
    //   } else if (command == "Exit") {
    //     ByteArrayOutputStream bos = new ByteArrayOutputStream();
    //     ObjectOutputStream oos = new ObjectOutputStream(bos);
    //     oos.writeObject(new FunctionResult(error));        
    //     synchronized (buffer) {
    //       buffer = ByteBuffer.wrap(bos.toByteArray());
    //       Future<Integer> writeResult = client.write(buffer);
    //       writeResult.get();
    //     } 
    //     buffer.clear();
    //     break;
    //   }
      
    //   buffer.flip();
    // }
  }

  private static FunctionResult getFunctionResult() {
    Properties props = new Properties();
    try {
      props.load(new FileInputStream("labs/src/main/resources/config.properties"));
    } catch (IOException e) {      
      e.printStackTrace();
    }
    int maxErrors = Integer.parseInt(props.getProperty("max_errors"));

    Optional<Optional<Double>> result;
    while (true) {
      result = Function.compfunc(n);
      if (result.isEmpty()) {
        if (error.getNonCriticalCounter() == maxErrors) {
          error.setIsNonCriticalLimit();
          return new FunctionResult(error, true);
        }
        error.addNonCritical();
      } else if (result.get().isEmpty()) {
        error.setIsCritical();
        return new FunctionResult(error, true);
      } else {
        return new FunctionResult(result.get().get(), true);
      }
    }        
  }
}