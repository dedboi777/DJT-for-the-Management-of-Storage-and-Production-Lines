package DJT_Company;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.Border;
public class LoginFrame extends JFrame {
    JTextField txtEmail;
    JPasswordField txtPass;
    JButton btnLogin;
    JButton btnRegister;
    JButton btnBack;
    private final String filePath = "users.txt";
    private HashMap<String, String[]> usersMap = new HashMap<>();
    public LoginFrame() {
        this.setTitle("Login - DJT");
        this.setIconImage((new ImageIcon("images/Logo2.jpg")).getImage());
        this.setSize(450, 400);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        this.loadUsers();
        JPanel panel = new JPanel();
        panel.setBackground(new Color(36, 37, 42));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel("Welcome Back");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setAlignmentX(0.5f);
        JLabel lblSubtitle = new JLabel("Login to your account");
        lblSubtitle.setForeground(Color.LIGHT_GRAY);
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSubtitle.setAlignmentX(0.5f);
        panel.add(Box.createVerticalStrut(30));
        panel.add(lblTitle);
        panel.add(lblSubtitle);
        panel.add(Box.createVerticalStrut(30));
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(new Color(36, 37, 42));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 25, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int fieldWidth = 200;
        int fieldHeight = 35;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lblEmail = new JLabel("Email :");
        lblEmail.setForeground(Color.WHITE);
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        fieldsPanel.add(lblEmail, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        txtEmail = new JTextField();
        txtEmail.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        styleField(txtEmail);
        fieldsPanel.add(txtEmail, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel lblPass = new JLabel("Password :");
        lblPass.setForeground(Color.WHITE);
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        fieldsPanel.add(lblPass, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        txtPass = new JPasswordField();
        txtPass.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
        styleField(txtPass);
        fieldsPanel.add(txtPass, gbc);

        panel.add(fieldsPanel);
        panel.add(Box.createVerticalStrut(20));

        btnLogin = createMainButton("Login");
        btnRegister = createSecondButton("Create Account");
        btnLogin.addActionListener(e -> handleLogin());
        btnRegister.addActionListener(e -> {
            this.dispose();
            new RegisterFrame();
        });

        panel.add(btnLogin);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnRegister);


        btnBack = createSecondButton("Back");
        btnBack.addActionListener(e -> {
            this.dispose();
            new LoginFrame();
        });
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnBack);

        JPanel graySpace = new JPanel();
        graySpace.setBackground(Color.DARK_GRAY);
        graySpace.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(Box.createVerticalStrut(15));
        panel.add(graySpace);

        this.add(panel);
        this.setVisible(true);
    }

    private void handleLogin() {
        String email = txtEmail.getText().trim();
        String password = new String(txtPass.getPassword());
        if (usersMap.containsKey(email)) {
            String[] info = usersMap.get(email);
            if (info[0].equals(password)) {
                String role = info[1];
                JOptionPane.showMessageDialog(this, "Login successful!");
                this.dispose();
                if (role.equals("Manager")) {
                    new ManagerFrame(this);
                } else if (role.equals("Product Supervisor")) {
                    new ProductSupervisorFrame(this);
                }
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Invalid email or password. Please try again!");
        Task.WriteErrMsgs("Invalid email or password. Please try again!");
    }



    private void loadUsers() {
        File file = new File(filePath);
        if (!file.exists()) return;
        usersMap.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String email = parts[4];
                    String password = parts[1];
                    String role = parts[2];
                    String name = parts[3];
                    String username = parts[0];
                    usersMap.put(email, new String[] { password, role, name, username });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void styleField(JTextField field) {
        field.setBackground(new Color(50, 50, 55));
        field.setForeground(Color.WHITE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90)));
    }

    private JButton createMainButton(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(0.5f);
        btn.setPreferredSize(new Dimension(250, 45));
        btn.setMaximumSize(new Dimension(250, 45));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0, 150, 240));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }

    private JButton createSecondButton(String text) {
        JButton btn = new JButton(text);
        btn.setAlignmentX(0.5f);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(Color.LIGHT_GRAY);
        btn.setBackground(new Color(36, 37, 42));
        btn.setBorder(null);
        btn.setFocusPainted(false);
        return btn;
    }

}
