package DJT_Company;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
public class Main {
    public static void main(String[] args) {
        
        applyGlobalDarkTheme();

        SwingUtilities.invokeLater(() -> {
            
            new SplashScreen();
        });
    }
        private static void applyGlobalDarkTheme() {
        try {
            
            Color bgColor = new Color(36, 37, 42);
            Color panelColor = new Color(45, 45, 50);
            Color fieldColor = new Color(50, 50, 55);
            Color textColor = Color.WHITE;
            Color accentColor = new Color(0, 120, 215);

            
            UIManager.put("Panel.background", bgColor);
            UIManager.put("OptionPane.background", panelColor);
            UIManager.put("OptionPane.messageForeground", textColor);

            UIManager.put("Label.foreground", textColor);

            UIManager.put("Button.background", new Color(70, 70, 75));
            UIManager.put("Button.foreground", textColor);
            UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
            UIManager.put("Button.border", BorderFactory.createEmptyBorder(8, 15, 8, 15));
            UIManager.put("Button.focus", new Insets(0, 0, 0, 0)); 

            UIManager.put("TextField.background", fieldColor);
            UIManager.put("TextField.foreground", textColor);
            UIManager.put("TextField.caretForeground", textColor);
            UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(90, 90, 90)),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ));

            UIManager.put("PasswordField.background", fieldColor);
            UIManager.put("PasswordField.foreground", textColor);
            UIManager.put("PasswordField.caretForeground", textColor);
            UIManager.put("PasswordField.border", UIManager.get("TextField.border"));

            UIManager.put("ComboBox.background", fieldColor);
            UIManager.put("ComboBox.foreground", textColor);
            UIManager.put("ComboBox.border", BorderFactory.createLineBorder(new Color(90, 90, 90)));
            
            UIManager.put("ComboBox.buttonBackground", new Color(70, 70, 75));
            UIManager.put("ComboBox.selectionBackground", accentColor);
            UIManager.put("ComboBox.selectionForeground", textColor);

            UIManager.put("Table.background", fieldColor);
            UIManager.put("Table.foreground", textColor);
            UIManager.put("Table.gridColor", new Color(80, 80, 80));
            UIManager.put("Table.selectionBackground", accentColor);
            UIManager.put("Table.selectionForeground", textColor);
            UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 14));
            UIManager.put("Table.rowHeight", 30);

            UIManager.put("TableHeader.background", new Color(70, 70, 75));
            UIManager.put("TableHeader.foreground", textColor);
            UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 16));

            UIManager.put("ScrollPane.background", bgColor);
            UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());

            UIManager.put("TabbedPane.background", bgColor);
            UIManager.put("TabbedPane.foreground", textColor);
            UIManager.put("TabbedPane.contentAreaColor", panelColor);
            UIManager.put("TabbedPane.selected", accentColor);
            UIManager.put("TabbedPane.unselectedTabForeground", Color.LIGHT_GRAY);
            UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.BOLD, 16));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
