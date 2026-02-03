package DJT_Company;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import javax.swing.*;
public class RegisterFrame extends JFrame {
    JTextField txtName;
    JTextField txtEmail;
    JTextField txtUsername;
    JPasswordField txtPass;
    JPasswordField txtConfirm;
    JComboBox<String> roleBox;
    JButton btnCreate;
    private final String filePath = "users.txt";
    private HashMap<String, String[]> usersMap = new HashMap<>();
    public RegisterFrame() {
        this.setTitle("Register - DJT");
        this.setIconImage((new ImageIcon("images/Logo2.jpg")).getImage());
        this.setSize(450, 550);
        this.setLocationRelativeTo(null);//في منتصف الشاشة
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setResizable(false);
        this.loadUsers();

        JPanel panel = new JPanel();
        panel.setBackground(new Color(36, 37, 42));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel lblTitle = new JLabel("Create Account"); lblTitle.setForeground(Color.WHITE); lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28)); lblTitle.setAlignmentX(0.5F);
        JLabel lblSubtitle = new JLabel("Fill in your details below"); lblSubtitle.setForeground(Color.LIGHT_GRAY); lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16)); lblSubtitle.setAlignmentX(0.5F);
        panel.add(Box.createVerticalStrut(30)); panel.add(lblTitle); panel.add(lblSubtitle); panel.add(Box.createVerticalStrut(30));
        JPanel fieldsPanel = new JPanel(new GridBagLayout()); fieldsPanel.setBackground(new Color(36, 37, 42));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(10, 25, 10, 10); gbc.fill = GridBagConstraints.HORIZONTAL;
        int fieldWidth = 200; int fieldHeight = 35;
        gbc.gridx = 0; gbc.gridy = 0; fieldsPanel.add(new JLabel("Name :"){{setForeground(Color.WHITE); setFont(new Font("Segoe UI", 0, 18));}}, gbc);
        gbc.gridx = 1; this.txtName = new JTextField(); this.txtName.setPreferredSize(new Dimension(fieldWidth, fieldHeight)); this.styleField(this.txtName); fieldsPanel.add(this.txtName, gbc);
        gbc.gridx = 0; gbc.gridy = 1; fieldsPanel.add(new JLabel("Email :"){{setForeground(Color.WHITE); setFont(new Font("Segoe UI", 0, 18));}}, gbc);
        gbc.gridx = 1; this.txtEmail = new JTextField(); this.txtEmail.setPreferredSize(new Dimension(fieldWidth, fieldHeight)); this.styleField(this.txtEmail); fieldsPanel.add(this.txtEmail, gbc);
        gbc.gridx = 0; gbc.gridy = 2; fieldsPanel.add(new JLabel("Username :"){{setForeground(Color.WHITE); setFont(new Font("Segoe UI", 0, 18));}}, gbc);
        gbc.gridx = 1; this.txtUsername = new JTextField(); this.txtUsername.setPreferredSize(new Dimension(fieldWidth, fieldHeight)); this.styleField(this.txtUsername); fieldsPanel.add(this.txtUsername, gbc);
        gbc.gridx = 0; gbc.gridy = 3; fieldsPanel.add(new JLabel("Password :"){{setForeground(Color.WHITE); setFont(new Font("Segoe UI", 0, 18));}}, gbc);
        gbc.gridx = 1; this.txtPass = new JPasswordField(); this.txtPass.setPreferredSize(new Dimension(fieldWidth, fieldHeight)); this.styleField(this.txtPass); fieldsPanel.add(this.txtPass, gbc);
        gbc.gridx = 0; gbc.gridy = 4; fieldsPanel.add(new JLabel("Confirm :"){{setForeground(Color.WHITE); setFont(new Font("Segoe UI", 0, 18));}}, gbc);
        gbc.gridx = 1; this.txtConfirm = new JPasswordField(); this.txtConfirm.setPreferredSize(new Dimension(fieldWidth, fieldHeight)); this.styleField(this.txtConfirm); fieldsPanel.add(this.txtConfirm, gbc);
        gbc.gridx = 0; gbc.gridy = 5; fieldsPanel.add(new JLabel("Role :"){{setForeground(Color.WHITE); setFont(new Font("Segoe UI", 0, 18));}}, gbc);
        gbc.gridx = 1; this.roleBox = new JComboBox<>(new String[] { "Manager", "Product Supervisor" }); this.roleBox.setPreferredSize(new Dimension(fieldWidth, fieldHeight)); this.roleBox.setFont(new Font("Segoe UI", 0, 16)); this.roleBox.setBackground(new Color(50, 50, 55)); this.roleBox.setForeground(Color.WHITE); fieldsPanel.add(this.roleBox, gbc);
        panel.add(fieldsPanel); panel.add(Box.createVerticalStrut(15));

        this.btnCreate = this.createMainButton("Create Account");
        this.btnCreate.addActionListener((e) -> this.handleCreate());
        panel.add(this.btnCreate);

        this.add(panel);
        this.setVisible(true);
    }


    private void handleCreate() {
        String name = this.txtName.getText().trim();
        String email = this.txtEmail.getText().trim();
        String username = this.txtUsername.getText().trim();
        String pass = new String(this.txtPass.getPassword());
        String confirm = new String(this.txtConfirm.getPassword());
        String role = (String) this.roleBox.getSelectedItem();


        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields!");
            return;
        }


        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match!");
            return;
        }


        if (this.usersMap.containsKey(username)) {
            JOptionPane.showMessageDialog(this, "Username already exists! Please choose another one.");
            return;
        }

        for (String[] userInfo : usersMap.values()) {
            if (userInfo[3].equals(email)) {
                JOptionPane.showMessageDialog(this, "Email already exists! Please use another one.");
                return;
            }
        }



        this.usersMap.put(username, new String[] { pass, role, name, email });
        this.saveUser(username, pass, role, name, email);
        JOptionPane.showMessageDialog(this, "Account Created Successfully!");
        this.dispose();
        new LoginFrame();
    }


    private void saveUser(String username, String pass, String role, String name, String email) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {

            bw.write(username + "," + pass + "," + role + "," + name + "," + email);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    usersMap.put(parts[0], new String[] { parts[1], parts[2], parts[3], parts[4] });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void styleField(JTextField field) { field.setBackground(new Color(50, 50, 55)); field.setForeground(Color.WHITE); field.setFont(new Font("Segoe UI", 0, 16)); field.setCaretColor(Color.WHITE); field.setBorder(BorderFactory.createLineBorder(new Color(90, 90, 90))); }
    private JButton createMainButton(String text) { JButton btn = new JButton(text); btn.setAlignmentX(0.5F); btn.setPreferredSize(new Dimension(250, 45));
        btn.setMaximumSize(new Dimension(250, 45)); btn.setFont(new Font("Segoe UI", 1, 18)); btn.setForeground(Color.WHITE); btn.setBackground(new Color(0, 150, 240)); btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); return btn; }
}
