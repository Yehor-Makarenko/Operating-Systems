package lab1;

import java.io.ByteArrayInputStream;
import java.io.Console;
import java.util.Optional;
import java.util.Scanner;

import javax.swing.JFrame;

import lab1.functions.Function;
import lab1.functions.function1.FunctionF;
import lab1.manager.Manager;
import lab1.manager.TestManager;

public class Main {
  public static void main(String[] args) {
    try {
      JFrame frame = new JFrame("Lab1");
      // Manager.compute(0);
      TestManager.compute(frame, 2);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}