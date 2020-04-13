import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main {
    public static void main(String[] args) {
        File urlDesktop = new File("abc.html");
                try {
                    Desktop.getDesktop().open(urlDesktop);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
    }
}