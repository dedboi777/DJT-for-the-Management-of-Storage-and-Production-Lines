
package DJT_Company;

import javax.swing.*;
import java.awt.*;

public class LockConfirmationDialog extends JDialog {
    private boolean useLocking = false;

    public LockConfirmationDialog(Frame owner) {
        super(owner, "Confirm Production Lock", true); 
        setSize(400, 150);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JLabel messageLabel = new JLabel("Lock materials before production to ensure data safety?");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton yesButton = new JButton("Yes (Recommended)");
        JButton noButton = new JButton("No");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);

        add(messageLabel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        yesButton.addActionListener(e -> {
            useLocking = true;
            dispose(); 
        });

        noButton.addActionListener(e -> {
            useLocking = false;
            dispose(); 
        });
    }

    
    public boolean shouldUseLocking() {
        return useLocking;
    }
}
