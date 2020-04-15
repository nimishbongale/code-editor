import java.awt.Desktop;
import java.net.URI;

public class Main {
    public static void main(String[] args) {
                try {
                   if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
    Desktop.getDesktop().browse(new URI("http://n45editor.surge.sh"));
}
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
    }
}