package lab1;

import java.util.Optional;

import lab1.functions.Function;
import lab1.functions.function1.FunctionF;
import lab1.manager.Manager;
import lab1.manager.TestManager;

public class Main {
  public static void main(String[] args) {
    try {
      // Manager.compute(0);
      TestManager.compute(1);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}