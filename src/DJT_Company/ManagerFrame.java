package DJT_Company;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ManagerFrame extends JFrame {

    // --- 1. Centralized constants for a professional look and feel ---
    private static final Color C_BACKGROUND = new Color(30, 30, 30);
    private static final Color C_PANEL = new Color(45, 45, 45);
    private static final Color C_TEXT = new Color(230, 230, 230);
    private static final Color C_ACCENT = new Color(20, 140, 255);
    private static final Color C_ACCENT_HOVER = new Color(100, 180, 255);
    private static final Color C_TABLE_GRID = new Color(60, 60, 60);

    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 28);
    private static final Font F_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font F_BODY = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font F_EMOJI = new Font("Segoe UI Emoji", Font.PLAIN, 15);

    private DefaultTableModel tableModel;
    private JTable productionLinesTable;
    private LoginFrame loginFrame;

    public ManagerFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;

        setTitle("Manager Dashboard - DJT Factory");
        setIconImage(new ImageIcon("images/Logo2.jpg").getImage());
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(C_BACKGROUND);
        setLayout(new BorderLayout());

        // --- Main container with professional padding ---
        JPanel rootPanel = new JPanel(new BorderLayout(20, 20));
        rootPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        rootPanel.setOpaque(false);
        add(rootPanel, BorderLayout.CENTER);


        // --- A modern title bar ---
        JLabel lblTitle = new JLabel("Manager Dashboard");
        lblTitle.setFont(F_TITLE);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBorder(new MatteBorder(0, 0, 2, 0, C_PANEL)); // Underline effect
        rootPanel.add(lblTitle, BorderLayout.NORTH);


        // --- A stylish sidebar for actions ---
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setOpaque(false);
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        JButton btnAddLine = createActionButton("➕ Add New Line");
        JButton btnUpdateStatus = createActionButton("⚙️ Update Status");
        JButton btnAddFeedback = createActionButton("⭐ View/Add Feedback");
        JButton btnRefresh = createActionButton("🔄 Refresh Data");
        JButton btnLogout = createActionButton("🚪 Logout");

        sidebarPanel.add(btnAddLine);
        sidebarPanel.add(Box.createVerticalStrut(15));
        sidebarPanel.add(btnUpdateStatus);
        sidebarPanel.add(Box.createVerticalStrut(15));
        sidebarPanel.add(btnAddFeedback);
        sidebarPanel.add(Box.createVerticalStrut(15));
        sidebarPanel.add(btnRefresh);
        sidebarPanel.add(Box.createVerticalGlue()); // Pushes logout to the bottom
        sidebarPanel.add(btnLogout);
        rootPanel.add(sidebarPanel, BorderLayout.WEST);


        // --- Table Setup with new styles ---
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
        scrollPane.setBorder(BorderFactory.createLineBorder(C_TABLE_GRID, 2));
        rootPanel.add(scrollPane, BorderLayout.CENTER);


        // --- Event Listeners (Logic is 100% UNCHANGED) ---
        btnAddLine.addActionListener(e -> handleAddNewLine());
        btnUpdateStatus.addActionListener(e -> handleUpdateStatus());
        btnAddFeedback.addActionListener(e -> handleAddFeedback());
        btnRefresh.addActionListener(e -> refreshTableData());
        btnLogout.addActionListener(e -> handleLogout());

        refreshTableData();
        setVisible(true);
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(F_EMOJI);
        button.setForeground(C_TEXT);
        button.setBackground(C_PANEL);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(15);

        Border padding = BorderFactory.createEmptyBorder(12, 20, 12, 20);
        Border line = new MatteBorder(0, 5, 0, 0, C_PANEL);
        button.setBorder(BorderFactory.createCompoundBorder(line, padding));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(65, 65, 65));
                ((JComponent)evt.getSource()).setBorder(
                        BorderFactory.createCompoundBorder(new MatteBorder(0, 5, 0, 0, C_ACCENT), padding)
                );
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(C_PANEL);
                ((JComponent)evt.getSource()).setBorder(
                        BorderFactory.createCompoundBorder(new MatteBorder(0, 5, 0, 0, C_PANEL), padding)
                );
            }
        });
        return button;
    }

    private void styleTable(JTable table) {
        table.setBackground(C_PANEL);
        table.setForeground(C_TEXT);
        table.setGridColor(C_TABLE_GRID);
        table.setRowHeight(40);
        table.setFont(F_BODY);
        table.setSelectionBackground(C_ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        // --- NEW: Center all cell content ---
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // Apply the centered renderer to all columns
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(25, 25, 25));
        header.setForeground(C_ACCENT_HOVER);
        header.setFont(F_HEADER);
        header.setPreferredSize(new Dimension(100, 45));
        header.setBorder(BorderFactory.createLineBorder(C_TABLE_GRID));

        // --- NEW: Center all header content ---
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
    }


    // --- ALL METHODS BELOW THIS LINE HAVE 100% UNCHANGED LOGIC ---

    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            this.dispose();
            if(loginFrame != null) {
                loginFrame.setVisible(true);
            }
        }
    }

    private void refreshTableData() {
        tableModel.setRowCount(0);
        Task.loadLinesFromDisk();
        var productLines = Task.ProductLines.values();
        if (productLines.isEmpty()) { return; }
        for (ProductLine line : productLines) {
            tableModel.addRow(new Object[]{line.getLineId(), line.getLineName(), line.getStatus(), line.getCompletion()});
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
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Production Line", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String name = nameField.getText().trim();
                if (name.isEmpty()) throw new IllegalArgumentException("Line name cannot be empty.");
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
            int result = JOptionPane.showConfirmDialog(this, statusComboBox, "Update Status for " + line.getLineName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                line.setStatus((String) statusComboBox.getSelectedItem());
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
        String history = allFeedbacks.stream().filter(f -> f.getLineId() == lineId).map(f -> "Date: " + f.getDate() + " | Rating: " + "⭐".repeat(f.getRating()) + "\nFeedback: " + f.getFeedback() + "\n--------------------").collect(Collectors.joining("\n"));
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
                new ManagerFeedback(lineId, lineName, feedbackText, rating, LocalDate.now()).saveFeedback();
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ManagerFrame(null));
    }
}
