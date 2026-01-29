package DJT_Company;
import java.awt.*;
import javax.swing.*;
public class SplashScreen extends JFrame {
    public SplashScreen() {
        this.setIconImage((new ImageIcon("images/Logo2.jpg")).getImage());
        ImageIcon image = new ImageIcon("images/Logo2.jpg");
        Image img = image.getImage().getScaledInstance(300, 300, 4);
        image = new ImageIcon(img);
        JLabel label = new JLabel(image);
        label.setHorizontalAlignment(0);
        this.setTitle("DJT Company");
        this.setSize(600, 500);
        this.setLocationRelativeTo((Component) null);
        this.setUndecorated(true);
        this.getContentPane().setBackground(new Color(90, 92, 255));
        this.setLayout(new BorderLayout());
        this.add(label, "Center");
        this.setVisible(true);
        Timer timer = new Timer(3000, (e) -> {
            this.dispose();
            new LoginFrame();
        });
        timer.setRepeats(false);
        timer.start();
    }
}
