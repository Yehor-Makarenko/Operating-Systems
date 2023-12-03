package lab1;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import lab1.manager.Manager;

public class Main {  
  private static JFrame frame = new JFrame("Lab1");  
  private static Manager manager;

  public static void main(String[] args) {      
    frame.setSize(500, 800);   
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(null);
    frame.setVisible(true);   
    manager = new Manager(frame);
    manager.open();
    start();
  }

  public static void start() {
    JTextArea input = new JTextArea();
    JScrollPane scrollPane = new JScrollPane(input);
    JButton startButton = new JButton("Start");
    JLabel label = new JLabel("Enter x:");
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
    label.setBounds(50, 270, 400, 30);
    scrollPane.setBounds(50, 300, 400, 100);

    panel.add(startButton);
    panel.add(label);
    panel.add(scrollPane);

    frame.setContentPane(panel);
    frame.revalidate();
    frame.repaint();
  }
}