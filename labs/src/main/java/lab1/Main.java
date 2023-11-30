package lab1;

import java.util.Optional;

import lab1.manager.Manager;

public class Main {
  public static void main(String[] args) {
    try {
      Manager.compute(0);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}