package lab1;

import java.io.ByteArrayInputStream;
import java.io.Console;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import lab1.functions.Function;
import lab1.functions.function1.FunctionF;
import lab1.functions.functionResult.FunctionError;
import lab1.manager.Manager;
import lab1.manager.TestManager;

public class Main {
  private static JFrame frame = new JFrame("Lab1");  
  private static TestManager manager;

  public static void main(String[] args) {    
    frame.setSize(500, 800);   
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(null);
    frame.setVisible(true);   
    manager = new TestManager(frame);
    start();
  }

  public static void start() {
    JTextArea input = new JTextArea();
    JScrollPane scrollPane = new JScrollPane(input);
    JButton startButton = new JButton("Start");
    JPanel panel = new JPanel(null);

    startButton.addActionListener(e -> {
      int n = Integer.parseInt(input.getText());
      try {        
        manager.compute(n);
      } catch (Exception e1) {        
        e1.printStackTrace();
      }
    });

    startButton.setBounds(200, 150, 100, 30);    
    scrollPane.setBounds(50, 300, 400, 100);

    panel.add(startButton);
    panel.add(scrollPane);

    frame.setContentPane(panel);
    frame.revalidate();
    frame.repaint();
  }
}