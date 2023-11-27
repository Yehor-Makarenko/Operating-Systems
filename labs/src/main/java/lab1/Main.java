package lab1;

import lab1.manager.Manager;

public class Main {
  public static void main(String[] args) {
    System.out.println(5);
    try {
      Manager.compute(5);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}