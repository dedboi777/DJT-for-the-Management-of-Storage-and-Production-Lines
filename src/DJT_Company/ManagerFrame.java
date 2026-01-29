package DJT_Company;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
public class ManagerFrame extends JFrame {

    private DefaultTableModel tableModel;
    private JTable productionLinesTable;
    private LoginFrame loginFrame; 

    public ManagerFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;

        
        setTitle("Manager Dashboard - DJT Factory");
        setIconImage(new ImageIcon("images/Logo2.jpg").getImage());
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(36, 37, 42));

        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(new Color(45, 45, 50));
        JLabel lblTitle = new JLabel("Manager Page"); 
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        titlePanel.add(lblTitle);
        add(titlePanel, BorderLayout.NORTH);

        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(36, 37, 42));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        
        JButton btnAddLine = createActionButton("➕ Add New Line");
        JButton btnUpdateStatus = createActionButton("⚙️ Update Status");
        JButton btnAddFeedback = createActionButton("⭐ View/Add Feedback");
        JButton btnRefresh = createActionButton("🔄 Refresh Data");
        JButton btnLogout = createActionButton("🚪 Logout");

        buttonPanel.add(btnAddLine);
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(btnUpdateStatus);
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(btnAddFeedback);
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(btnRefresh);
        buttonPanel.add(Box.createVerticalGlue()); 
        buttonPanel.add(btnLogout);

        add(buttonPanel, BorderLayout.EAST);

        
        String[] columnNames = {"Line ID", "Line Name", "Status", "Completion %"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        productionLinesTable = new JTable(tableModel);
        styleTable(productionLinesTable);

        
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        productionLinesTable.setRowSorter(sorter);

        
        sorter.setComparator(0, (Comparator<Integer>) Comparator.naturalOrder());
        sorter.setComparator(3, (Comparator<Double>) Comparator.naturalOrder());

        JScrollPane scrollPane = new JScrollPane(productionLinesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 55)));
        add(scrollPane, BorderLayout.CENTER);


        
        btnAddLine.addActionListener(e -> handleAddNewLine());
        btnUpdateStatus.addActionListener(e -> handleUpdateStatus());
        btnAddFeedback.addActionListener(e -> handleAddFeedback());
        btnRefresh.addActionListener(e -> refreshTableData());
        btnLogout.addActionListener(e -> handleLogout());

        
        refreshTableData();
        setVisible(true);
    }

        private void refreshTableData() {
        tableModel.setRowCount(0);
        Task.loadLinesFromDisk();
        var productLines = Task.ProductLines.values();

        if (productLines.isEmpty()) {
            System.out.println("No production lines found.");
            return;
        }

        for (ProductLine line : productLines) {
            Object[] row = {
                    line.getLineId(),
                    line.getLineName(),
                    line.getStatus(),
                    line.getCompletion()
            };
            tableModel.addRow(row);
        }
    }

        private void handleAddNewLine() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Enter Line ID:"));
        panel.add(idField);
        panel.add(new JLabel("Enter Line Name:"));
        panel.add(nameField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Production Line",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    throw new IllegalArgumentException("Line name cannot be empty.");
                }

                if (Task.ProductLines.containsKey(id)) {
                    JOptionPane.showMessageDialog(this, "A production line with this ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Task.addNewProdLine(id, name);
                Task.saveLinesToDisk();

                JOptionPane.showMessageDialog(this, "Production line added successfully!");
                refreshTableData();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid ID. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

        private void handleUpdateStatus() {
        int selectedRow = productionLinesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a production line from the table first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = productionLinesTable.convertRowIndexToModel(selectedRow);
        int lineId = (int) tableModel.getValueAt(modelRow, 0);
        ProductLine line = Task.ProductLines.get(lineId);

        if (line != null) {
            String[] statuses = {"Active", "Stopped", "Maintenance"};
            JComboBox<String> statusComboBox = new JComboBox<>(statuses);
            statusComboBox.setSelectedItem(line.getStatus());

            int result = JOptionPane.showConfirmDialog(this, statusComboBox, "Update Status for " + line.getLineName(),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String newStatus = (String) statusComboBox.getSelectedItem();

                line.setStatus(newStatus);
                Task.saveLinesToDisk();

                JOptionPane.showMessageDialog(this, "Status updated successfully!");
                refreshTableData();
            }
        }
    }

        private void handleAddFeedback() {
        int selectedRow = productionLinesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a production line to view or add feedback.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = productionLinesTable.convertRowIndexToModel(selectedRow);
        int lineId = (int) tableModel.getValueAt(modelRow, 0);
        String lineName = (String) tableModel.getValueAt(modelRow, 1);

        JDialog feedbackDialog = new JDialog(this, "Feedback for " + lineName, true);
        feedbackDialog.setSize(500, 400);
        feedbackDialog.setLocationRelativeTo(this);
        feedbackDialog.setLayout(new BorderLayout(10, 10));

        JTextArea historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        historyArea.setMargin(new Insets(5, 5, 5, 5));
        List<ManagerFeedback> allFeedbacks = ManagerFeedback.loadAllFeedbacks();
        String history = allFeedbacks.stream()
                .filter(f -> f.getLineId() == lineId)
                .map(f -> "Date: " + f.getDate() + " | Rating: " + "⭐".repeat(f.getRating()) + "\nFeedback: " + f.getFeedback() + "\n--------------------")
                .collect(Collectors.joining("\n"));
        historyArea.setText(history.isEmpty() ? "No feedback history for this line." : history);

        JPanel newFeedbackPanel = new JPanel(new BorderLayout(5, 5));
        newFeedbackPanel.setBorder(BorderFactory.createTitledBorder("Add New Feedback"));

        JTextField feedbackField = new JTextField();
        JComboBox<String> ratingBox = new JComboBox<>(new String[]{"⭐", "⭐⭐", "⭐⭐⭐", "⭐⭐⭐⭐", "⭐⭐⭐⭐⭐"});
        JButton saveButton = new JButton("Save Feedback");

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(new JLabel("Rating: "), BorderLayout.WEST);
        topPanel.add(ratingBox, BorderLayout.CENTER);

        newFeedbackPanel.add(topPanel, BorderLayout.NORTH);
        newFeedbackPanel.add(feedbackField, BorderLayout.CENTER);
        newFeedbackPanel.add(saveButton, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> {
            String feedbackText = feedbackField.getText();
            int rating = ((String) ratingBox.getSelectedItem()).length();
            if (!feedbackText.trim().isEmpty()) {
                ManagerFeedback newFeedback = new ManagerFeedback(lineId, lineName, feedbackText, rating, LocalDate.now());
                newFeedback.saveFeedback();
                JOptionPane.showMessageDialog(feedbackDialog, "Feedback saved!");
                feedbackDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(feedbackDialog, "Feedback text cannot be empty.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        feedbackDialog.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        feedbackDialog.add(newFeedbackPanel, BorderLayout.SOUTH);
        feedbackDialog.setVisible(true);
    }

        private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            this.dispose();
            loginFrame.setVisible(true);

        }
    }

    

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);

        
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(70, 70, 75));
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(90, 90, 95));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 70, 75));
            }
        });

        return button;
    }

    private void styleTable(JTable table) {
        table.setBackground(new Color(50, 50, 55));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(80, 80, 80));
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionBackground(new Color(0, 120, 215));
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(70, 70, 75));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManagerFrame(null));
    }
}
