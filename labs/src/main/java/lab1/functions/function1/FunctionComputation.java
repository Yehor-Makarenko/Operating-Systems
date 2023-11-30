package lab1.functions.function1;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
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
  private static int n = 2;
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
    
    FunctionResult res = getFunctionResult();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos;
    try {
      oos = new ObjectOutputStream(bos);
      oos.writeObject(res);
    } catch (IOException e) {        
      e.printStackTrace();
    }
    Future<Integer> writeResult = client.write(ByteBuffer.wrap(bos.toByteArray()));
    try {
      writeResult.get();
    } catch (InterruptedException | ExecutionException e) {        
      e.printStackTrace();
    }

    while (client.isOpen()) {
      Thread.sleep(100);
      n--;
    }
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

  private static void readMessage() throws Exception {
    client.read(buffer, null, new CompletionHandler<Integer, Void>() {
      @Override
      public void completed(Integer result, Void attachment) {        
        if (result == -1) {
          try {
            client.close();
          } catch (IOException e) {            
            e.printStackTrace();
          }
          return;
        }        

        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        buffer.clear();
        String message = new String(data);
        if (message.equals("Report")) {
          try {
            handleReport();
          } catch (Exception e) {            
            e.printStackTrace();
          }
        } else if (message.equals("Close")) {
          try {
            handleClose();
          } catch (Exception e) {            
            e.printStackTrace();
          }
        }
      }

      @Override
      public void failed(Throwable arg0, Void arg1) {        
        throw new UnsupportedOperationException("Unimplemented method 'failed'");
      }
      
    });
  }

  private static void handleReport() throws Exception {    
    ByteBuffer buffer = serializeFunctionResult();
    client.write(buffer, null, new CompletionHandler<Integer, Void>() {
      @Override
      public void completed(Integer result, Void arg1) {        
        if (result == -1) {
          try {
            client.close();
          } catch (IOException e) {            
            e.printStackTrace();
          }
          return;
        }

        try {          
          readMessage();
        } catch (Exception e) {          
          e.printStackTrace();
        }
      }

      @Override
      public void failed(Throwable arg0, Void arg1) {        
        throw new UnsupportedOperationException("Unimplemented method 'failed'");
      }      
    });
  }

  private static void handleClose() throws Exception {
    ByteBuffer buffer = serializeFunctionResult();
    client.write(buffer, null, new CompletionHandler<Integer, Void>() {
      @Override
      public void completed(Integer result, Void arg1) {        
        if (result == -1) {
          try {
            client.close();
          } catch (IOException e) {            
            e.printStackTrace();
          }
          return;
        }

        buffer.clear();
        try {
          client.close();
        } catch (IOException e) {          
          e.printStackTrace();
        }
      }

      @Override
      public void failed(Throwable arg0, Void arg1) {        
        throw new UnsupportedOperationException("Unimplemented method 'failed'");
      }      
    });
  }

  private static ByteBuffer serializeFunctionResult() throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(new FunctionResult(error, false));   
    ByteBuffer buffer = ByteBuffer.wrap(bos.toByteArray());
    return buffer;
  }
}