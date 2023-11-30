package lab1.functions.function1;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import lab1.functions.FunctionError;
import lab1.functions.FunctionResult;

public class FunctionComputation {
  private static int n;
  private static FunctionError error = new FunctionError("f");
  private static ByteBuffer buffer;
  private static AsynchronousSocketChannel client;

  public static void main(String[] args) throws Exception {        
    n = Integer.parseInt(args[0]);
    client = AsynchronousSocketChannel.open();
    Future<Void> result = client.connect(new InetSocketAddress("127.0.0.1", 1234));    
    result.get();
    buffer = ByteBuffer.allocate(1024);

    readMessage();
    
    // FunctionResult res = getFunctionResult();
    // ByteArrayOutputStream bos = new ByteArrayOutputStream();
    // ObjectOutputStream oos;
    // try {
    //   oos = new ObjectOutputStream(bos);
    //   oos.writeObject(res);
    // } catch (IOException e) {        
    //   e.printStackTrace();
    // }
    // buffer = ByteBuffer.wrap(bos.toByteArray());
    // Future<Integer> writeResult = client.write(buffer);
    // try {
    //   writeResult.get();
    // } catch (InterruptedException | ExecutionException e) {        
    //   e.printStackTrace();
    // }

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
          error.setNonCriticalLimit();
          return new FunctionResult(error);
        }
        error.addNonCritical();
      } else if (result.get().isEmpty()) {
        error.setCritical();
        return new FunctionResult(error);
      } else {
        return new FunctionResult(result.get().get());
      }
    }        
  }

  private static void readMessage() {
    client.read(buffer, null, new CompletionHandler<Integer, Void>() {
      @Override
      public void completed(Integer result, Void attachment) {        
        if (result == -1) {
          client.close();
          return;
        }        

        buffer.flip();
        String data = new String(buffer.array());
        if (data == "Report") {
          handleReport();
        }
      }

      @Override
      public void failed(Throwable arg0, Void arg1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'failed'");
      }
      
    })
  }

  private static void handleReport() throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(new FunctionResult(error));   
    ByteBuffer buffer = ByteBuffer.wrap(bos.toByteArray());
    client.write(buffer, null, new CompletionHandler<Integer, Void>() {
      @Override
      public void completed(Integer result, Void arg1) {        
        if (result == -1) {
          client.close();
          return;
        }

        readMessage();
      }

      @Override
      public void failed(Throwable arg0, Void arg1) {        
        throw new UnsupportedOperationException("Unimplemented method 'failed'");
      }
      
    })

  }
}