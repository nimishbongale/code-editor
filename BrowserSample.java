import javax.swing.*;
import java.awt.*;

public class BrowserSample {
   public static void main(String[] args) {
        JEditorPane jep = new JEditorPane();
jep.setEditable(false);   

try {
  jep.setPage("file:abc.html");
}catch (Exception e) {
  jep.setContentType("text/html");
  jep.setText("<html>Could not load</html>");
} 

JScrollPane scrollPane = new JScrollPane(jep);     
JFrame f = new JFrame("Test HTML");
f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
f.getContentPane().add(scrollPane);
f.setPreferredSize(new Dimension(800,600));
f.setVisible(true);
   }
}