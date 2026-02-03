package DJT_Company;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductSupervisorFrame extends JFrame {

    // --- 1. Centralized constants for a consistent look and feel ---
    private static final Color C_BACKGROUND = new Color(30, 30, 30);
    private static final Color C_PANEL = new Color(45, 45, 45);
    private static final Color C_TEXT = new Color(230, 230, 230);
    private static final Color C_ACCENT = new Color(20, 140, 255);
    private static final Color C_TABLE_GRID = new Color(60, 60, 60);

    private static final Font F_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font F_BODY = new Font("Segoe UI", Font.PLAIN, 15);
    private static final Font F_EMOJI = new Font("Segoe UI Emoji", Font.PLAIN, 15);

    private final LoginFrame loginFrame;
    private DefaultTableModel itemsTableModel, tasksTableModel, productsDefTableModel, reqsTableModel;
    private JTable itemsTable, tasksTable, productsDefTable, reqsTable;
    private JTextField itemSearchName, itemSearchCategory;
    private JComboBox<String> itemSearchStatus;
    private JTextArea logArea;

    public ProductSupervisorFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;

        setTitle("Production Supervisor Dashboard - DJT Factory");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        getContentPane().setBackground(C_BACKGROUND);

        // --- 2. Custom styling for JTabbedPane ---
        UIManager.put("TabbedPane.contentAreaColor", C_PANEL);
        UIManager.put("TabbedPane.background", C_BACKGROUND);
        UIManager.put("TabbedPane.foreground", C_TEXT);
        UIManager.put("TabbedPane.selected", C_ACCENT);
        UIManager.put("TabbedPane.borderHighlightColor", C_TABLE_GRID);
        UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
        UIManager.put("TabbedPane.tabInsets", new Insets(10, 15, 10, 15));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabbedPane.addTab("📦 Inventory Management", createInventoryPanel());
        tabbedPane.addTab("📋 Task Management", createTasksPanel());
        tabbedPane.addTab("🔩 Product Definitions", createProductDefsPanel());
        tabbedPane.addTab("📊 Production Analytics", createAnalyticsPanel());
        tabbedPane.addTab("📜 System Logs", createLogPanel());

        add(tabbedPane);

        redirectSystemStreams();

        refreshItemsTable();
        refreshTasksTable();
        Product.loadDefinitionsFromFile();
        refreshProductsDefTable();

        setVisible(true);
    }

    // --- Reusable Styling Methods ---
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

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(25, 25, 25));
        header.setForeground(new Color(100, 180, 255));
        header.setFont(F_HEADER);
        header.setPreferredSize(new Dimension(100, 45));
        header.setBorder(BorderFactory.createLineBorder(C_TABLE_GRID));

        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);

        table.setRowSorter(new TableRowSorter<>((DefaultTableModel) table.getModel()));
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(F_EMOJI);
        button.setForeground(C_TEXT);
        button.setBackground(new Color(65, 65, 65));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(1, 1, 1, 1, C_TABLE_GRID),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { button.setBackground(new Color(80, 80, 80)); }
            public void mouseExited(java.awt.event.MouseEvent evt) { button.setBackground(new Color(65, 65, 65)); }
        });
        return button;
    }

    private TitledBorder createTitledBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(C_TABLE_GRID, 1),
                " " + title + " ", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 15), C_ACCENT);
    }

    private void setupPanel(JPanel panel) {
        panel.setBackground(C_PANEL);
        panel.setForeground(C_TEXT);
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(C_TEXT);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return label;
    }

    private JLabel createDialogLabel(String text) {
        return new JLabel(text);
    }

    private JTextArea styleTextArea(JTextArea textArea) {
        textArea.setBackground(new Color(35,35,35));
        textArea.setForeground(C_TEXT);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        textArea.setEditable(false);
        textArea.setMargin(new Insets(10,10,10,10));
        return textArea;
    }

    // --- Panel Creation Methods (Logic Unchanged, Styles Applied) ---

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        setupPanel(panel);
        panel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        logArea = new JTextArea();
        styleTextArea(logArea);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(C_TABLE_GRID));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createInventoryPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        setupPanel(mainPanel);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(createTitledBorder("Search & Filter Items"));

        itemSearchName = new JTextField(15);
        itemSearchCategory = new JTextField(15);
        itemSearchStatus = new JComboBox<>(new String[]{"All", "Available", "Finished", "Below Minimum"});
        JButton btnSearchItems = createActionButton("🔍 Search");

        searchPanel.add(createStyledLabel("Name:"));
        searchPanel.add(itemSearchName);
        searchPanel.add(createStyledLabel("Category:"));
        searchPanel.add(itemSearchCategory);
        searchPanel.add(createStyledLabel("Status:"));
        searchPanel.add(itemSearchStatus);
        searchPanel.add(btnSearchItems);
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        itemsTableModel = new DefaultTableModel(new String[]{"Item Name", "Quantity", "Category"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        itemsTable = new JTable(itemsTableModel);
        styleTable(itemsTable);
        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(C_TABLE_GRID));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        JButton btnAddItem = createActionButton("➕ Add Item");
        JButton btnEditItem = createActionButton("✏️ Edit Item");
        JButton btnDeleteItem = createActionButton("❌ Delete Item");
        JButton btnRefreshItems = createActionButton("🔄 Refresh");
        JButton btnSaveInventory = createActionButton("💾 Save to File");

        buttonPanel.add(btnAddItem);
        buttonPanel.add(btnEditItem);
        buttonPanel.add(btnDeleteItem);
        buttonPanel.add(btnRefreshItems);
        buttonPanel.add(btnSaveInventory);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        btnSearchItems.addActionListener(e -> searchItems());
        btnAddItem.addActionListener(e -> handleAddItem());
        btnEditItem.addActionListener(e -> handleEditItem());
        btnDeleteItem.addActionListener(e -> handleDeleteItem());
        btnRefreshItems.addActionListener(e -> refreshItemsTable());
        btnSaveInventory.addActionListener(e -> JOptionPane.showMessageDialog(this, "Inventory is saved automatically on each change.", "Info", JOptionPane.INFORMATION_MESSAGE));

        return mainPanel;
    }

    private JPanel createTasksPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        setupPanel(mainPanel);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        tasksTableModel = new DefaultTableModel(new String[]{"Task ID", "Client", "Product", "Quantity", "Line ID", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tasksTable = new JTable(tasksTableModel);
        styleTable(tasksTable);
        JScrollPane scrollPane = new JScrollPane(tasksTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(C_TABLE_GRID));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

        JButton btnAddTask = createActionButton("➕ Add New Task");
        JButton btnRunTask = createActionButton("▶️ Run Production");
        JButton btnCancelTask = createActionButton("❌ Cancel Task");
        JButton btnUpdateStatus = createActionButton("🔄 Update Status");
        JButton btnRefreshTasks = createActionButton("🔄 Refresh Tasks");
        JButton btnLogout = createActionButton("🚪 Logout");

        buttonPanel.add(btnAddTask);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(btnRunTask);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(btnCancelTask);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(btnUpdateStatus);
        buttonPanel.add(Box.createVerticalStrut(20));
        buttonPanel.add(btnRefreshTasks);
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(btnLogout);
        mainPanel.add(buttonPanel, BorderLayout.EAST);

        btnAddTask.addActionListener(e -> handleAddTask());
        btnRunTask.addActionListener(e -> handleRunTask());
        btnCancelTask.addActionListener(e -> handleCancelTask());
        btnUpdateStatus.addActionListener(e -> handleUpdateTaskStatus());
        btnRefreshTasks.addActionListener(e -> refreshTasksTable());
        btnLogout.addActionListener(e -> handleLogout());

        return mainPanel;
    }

    private JPanel createProductDefsPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(15,15));
        setupPanel(mainPanel);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.4);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        splitPane.setDividerSize(10);

        splitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                javax.swing.plaf.basic.BasicSplitPaneDivider d = new javax.swing.plaf.basic.BasicSplitPaneDivider(this);
                d.setBorder(BorderFactory.createEmptyBorder());
                return d;
            }
        });

        JPanel productsPanel = new JPanel(new BorderLayout(10,10));
        productsPanel.setOpaque(false);
        productsPanel.setBorder(createTitledBorder("Defined Products"));

        productsDefTableModel = new DefaultTableModel(new String[]{"ID", "Product Name"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        productsDefTable = new JTable(productsDefTableModel);
        styleTable(productsDefTable);
        JScrollPane pdtScrollPane = new JScrollPane(productsDefTable);
        pdtScrollPane.setBorder(BorderFactory.createLineBorder(C_TABLE_GRID));
        productsPanel.add(pdtScrollPane, BorderLayout.CENTER);

        JPanel productButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        productButtons.setOpaque(false);
        JButton btnAddProd = createActionButton("➕ Add Product");
        JButton btnDelProd = createActionButton("❌ Delete Product");
        productButtons.add(btnAddProd);
        productButtons.add(btnDelProd);
        productsPanel.add(productButtons, BorderLayout.SOUTH);

        JPanel reqsPanel = new JPanel(new BorderLayout(10,10));
        reqsPanel.setOpaque(false);
        reqsPanel.setBorder(createTitledBorder("Required Materials"));

        reqsTableModel = new DefaultTableModel(new String[]{"Item ID", "Item Name", "Quantity"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        reqsTable = new JTable(reqsTableModel);
        styleTable(reqsTable);
        JScrollPane reqsScrollPane = new JScrollPane(reqsTable);
        reqsScrollPane.setBorder(BorderFactory.createLineBorder(C_TABLE_GRID));
        reqsPanel.add(reqsScrollPane, BorderLayout.CENTER);

        JPanel reqsButtons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        reqsButtons.setOpaque(false);
        JButton btnAddReq = createActionButton("➕ Add Material");
        JButton btnEditReq = createActionButton("✏️ Edit Quantity");
        JButton btnDelReq = createActionButton("❌ Remove Material");
        reqsButtons.add(btnAddReq);
        reqsButtons.add(btnEditReq);
        reqsButtons.add(btnDelReq);
        reqsPanel.add(reqsButtons, BorderLayout.SOUTH);

        splitPane.setLeftComponent(productsPanel);
        splitPane.setRightComponent(reqsPanel);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        productsDefTable.getSelectionModel().addListSelectionListener(this::handleProductSelection);
        btnAddProd.addActionListener(e -> handleAddProduct());
        btnDelProd.addActionListener(e -> handleDeleteProduct());
        btnAddReq.addActionListener(e -> handleAddRequirement());
        btnEditReq.addActionListener(e -> handleEditRequirement());
        btnDelReq.addActionListener(e -> handleDeleteRequirement());

        return mainPanel;
    }
    private JPanel createAnalyticsPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(0, 2, 20, 20));
        setupPanel(mainPanel);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.add(createReportPanel("View Tasks by Line", "Line:", true, this::runTasksByLineReport));
        mainPanel.add(createReportPanel("View Tasks by Product", "Product:", false, this::runTasksByProductReport));
        mainPanel.add(createReportPanel("Lines per Product", "Product:", false, this::runLinesPerProductReport));
        mainPanel.add(createReportPanel("Products per Line", "Line:", true, this::runProductsPerLineReport));
        mainPanel.add(createReportPanel("Most Requested Product", "Start Date (YYYY-MM-DD):", false, this::runMostRequestedReport));
        JPanel allProductsPanel = new JPanel(new BorderLayout(10,10));
        allProductsPanel.setOpaque(false);
        allProductsPanel.setBorder(createTitledBorder("All Manufactured Products"));
        JButton runAllProducts = createActionButton("Show All Products");
        JTextArea allProductsArea = new JTextArea();
        allProductsPanel.add(runAllProducts, BorderLayout.NORTH);
        JScrollPane apScrollPane = new JScrollPane(styleTextArea(allProductsArea));
        apScrollPane.setBorder(BorderFactory.createLineBorder(C_TABLE_GRID));
        allProductsPanel.add(apScrollPane, BorderLayout.CENTER);
        runAllProducts.addActionListener(e -> {
            Task.loadProductionRecordsFromDisk();
            String result = Task.allProductions.stream().map(record -> record.productToProduce).distinct().collect(Collectors.joining("\n"));
            allProductsArea.setText(result.isEmpty() ? "No production records found." : result);
        });

        mainPanel.add(allProductsPanel);

        return mainPanel;
    }


    private JPanel createReportPanel(String title, String label, boolean useComboBox, java.util.function.BiConsumer<String, JTextArea> action) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(createTitledBorder(title));

        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setOpaque(false);
        inputPanel.add(createStyledLabel(label), BorderLayout.WEST);

        Component inputComponent;
        if (useComboBox) {
            Task.loadLinesFromDisk();
            String[] lines = Task.ProductLines.values().stream().map(l -> l.getLineId() + " - " + l.getLineName()).toArray(String[]::new);
            inputComponent = new JComboBox<>(lines);
        } else {
            inputComponent = new JTextField();
        }
        inputPanel.add(inputComponent, BorderLayout.CENTER);

        JButton runButton = new JButton("▶️ Run");
        runButton.setFont(F_EMOJI);
        inputPanel.add(runButton, BorderLayout.EAST);

        JTextArea resultArea = new JTextArea(5, 20);
        JScrollPane resScrollPane = new JScrollPane(styleTextArea(resultArea));
        resScrollPane.setBorder(BorderFactory.createLineBorder(C_TABLE_GRID));
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(resScrollPane, BorderLayout.CENTER);

        runButton.addActionListener(e -> {
            String value = useComboBox ? (String)((JComboBox)inputComponent).getSelectedItem() : ((JTextField)inputComponent).getText();
            action.accept(value, resultArea);
        });

        return panel;
    }

    // --- ALL LOGIC METHODS BELOW ARE 100% UNCHANGED ---

    private void redirectSystemStreams() {
        try {
            OutputStream out = new CustomOutputStream(logArea);
            System.setOut(new PrintStream(out, true, "UTF-8"));
            System.setErr(new PrintStream(out, true, "UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void refreshItemsTable() {
        itemsTableModel.setRowCount(0);
        InventoryManager.getAllItems().forEach(itemsTableModel::addRow);
    }

    private void searchItems() {
        itemsTableModel.setRowCount(0);
        List<String[]> results = InventoryManager.searchItems(
                itemSearchName.getText(),
                itemSearchCategory.getText(),
                (String) itemSearchStatus.getSelectedItem()
        );
        results.forEach(itemsTableModel::addRow);
    }

    private void handleAddItem() {
        JTextField nameField = new JTextField();
        JTextField qtyField = new JTextField();
        JTextField categoryField = new JTextField();
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(createDialogLabel("Name:"));
        panel.add(nameField);
        panel.add(createDialogLabel("Quantity:"));
        panel.add(qtyField);
        panel.add(createDialogLabel("Category:"));
        panel.add(categoryField);
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Item", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                Integer.parseInt(qtyField.getText());
                InventoryManager.addItem(name, qtyField.getText(), categoryField.getText());
                refreshItemsTable();
            } catch (NumberFormatException ex) {
                Task.WriteErrMsgs("Quantity isn't a number");
                JOptionPane.showMessageDialog(this, "Quantity must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleEditItem() {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow == -1) {
            Task.WriteErrMsgs("No item selected for editing");
            JOptionPane.showMessageDialog(this, "Please select an item to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String oldName = (String) itemsTableModel.getValueAt(selectedRow, 0);
        String oldQty = (String) itemsTableModel.getValueAt(selectedRow, 1);
        String oldCategory = (String) itemsTableModel.getValueAt(selectedRow, 2);
        JTextField nameField = new JTextField(oldName);
        JTextField qtyField = new JTextField(oldQty);
        JTextField categoryField = new JTextField(oldCategory);
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(createDialogLabel("Name:"));
        panel.add(nameField);
        panel.add(createDialogLabel("Quantity:"));
        panel.add(qtyField);
        panel.add(createDialogLabel("Category:"));
        panel.add(categoryField);
        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Item", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            InventoryManager.updateItem(oldName, nameField.getText(), qtyField.getText(), categoryField.getText());
            refreshItemsTable();
        }
    }

    private void handleDeleteItem() {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow == -1) {
            Task.WriteErrMsgs("No item selected for deletion");
            JOptionPane.showMessageDialog(this, "Please select an item to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String itemName = (String) itemsTableModel.getValueAt(selectedRow, 0);
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete '" + itemName + "'?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            InventoryManager.deleteItem(itemName);
            refreshItemsTable();
        }
    }

    private void refreshTasksTable() {
        SwingUtilities.invokeLater(() -> {
            tasksTableModel.setRowCount(0);
            for (Task task : Task.activeTasks.values()) {
                tasksTableModel.addRow(new Object[]{
                        task.getTaskId(),
                        task.getClientNameForTable(),
                        task.getProductNameForTable(),
                        task.getQuantityForTable(),
                        task.getAssignedProductionLineId(),
                        task.getStatus()
                });
            }
        });
    }

    private void handleAddTask() {
        JTextField clientField = new JTextField();
        Product.loadDefinitionsFromFile();
        String[] productNames = Product.productDefinitions.values().stream()
                .map(Product::getProductName)
                .toArray(String[]::new);
        if(productNames.length == 0){
            Task.WriteErrMsgs("No products defined");
            JOptionPane.showMessageDialog(this, "No products defined. Please define a product first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JComboBox<String> productComboBox = new JComboBox<>(productNames);
        JTextField quantityField = new JTextField();
        Task.loadLinesFromDisk();
        List<String> activeLines = Task.ProductLines.values().stream()
                .filter(line -> line.getStatus().equals("Active"))
                .map(line -> line.getLineId() + " - " + line.getLineName())
                .collect(Collectors.toList());
        if (activeLines.isEmpty()) {
            Task.WriteErrMsgs("NO active prodution lines available");
            JOptionPane.showMessageDialog(this, "No active production lines available.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JComboBox<String> linesComboBox = new JComboBox<>(activeLines.toArray(new String[0]));
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(createDialogLabel("Client Name:"));
        panel.add(clientField);
        panel.add(createDialogLabel("Product:"));
        panel.add(productComboBox);
        panel.add(createDialogLabel("Quantity:"));
        panel.add(quantityField);
        panel.add(createDialogLabel("Assign to Line:"));
        panel.add(linesComboBox);
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Task", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int quantity = Integer.parseInt(quantityField.getText());
                String selectedProduct = (String) productComboBox.getSelectedItem();
                String selectedLine = (String) linesComboBox.getSelectedItem();
                int lineId = Integer.parseInt(selectedLine.split(" - ")[0]);
                Task newTask = new Task(clientField.getText(), selectedProduct, quantity);
                newTask.setAssignedProductionLineId(lineId);
                newTask.saveTaskToDisk();
                Task.activeTasks.put(newTask.getTaskId(), newTask);
                JOptionPane.showMessageDialog(this, "Task #" + newTask.getTaskId() + " added successfully!");
                refreshTasksTable();
            } catch (Exception ex) {
                Task.WriteErrMsgs("Failed to add task. Check input");
                JOptionPane.showMessageDialog(this, "Failed to add task. Check inputs.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleRunTask() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow == -1) {
            Task.WriteErrMsgs("No task selected for running");
            JOptionPane.showMessageDialog(this, "Please select a task to run.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = tasksTable.convertRowIndexToModel(selectedRow);
        int taskId = (int) tasksTableModel.getValueAt(modelRow, 0);
        Task taskToRun = Task.activeTasks.get(taskId);

        if (taskToRun == null || taskToRun.getStatus() != Task.TaskStatus.IN_PROGRESS) {
            JOptionPane.showMessageDialog(this, "Task cannot be run.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LockConfirmationDialog dialog = new LockConfirmationDialog(this);
        dialog.setVisible(true);
        boolean useLocking = dialog.shouldUseLocking();
        taskToRun.setOnCompletion(() -> refreshTasksTable());
        taskToRun.startProductionFromGUI(useLocking);
        JOptionPane.showMessageDialog(this, "Task" + taskId + " started.\nCheck the 'System Logs' tab for progress.");
    }

    private void handleCancelTask() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow == -1) {
            Task.WriteErrMsgs("No task selected for canceling");
            JOptionPane.showMessageDialog(this, "Please select a task to cancel.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int taskId = (int) tasksTableModel.getValueAt(selectedRow, 0);
        Task taskToCancel = Task.activeTasks.get(taskId);
        if (taskToCancel != null) {
            int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel Task #" + taskId + "?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                taskToCancel.cancelTask();
                refreshTasksTable();
            }
        }
    }

    private void handleUpdateTaskStatus() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow == -1) {
            Task.WriteErrMsgs("No task selected for updating");
            JOptionPane.showMessageDialog(this, "Please select a task to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int taskId = (int) tasksTableModel.getValueAt(selectedRow, 0);
        Task taskToUpdate = Task.activeTasks.get(taskId);
        if (taskToUpdate != null) {
            Task.TaskStatus[] statuses = Task.TaskStatus.values();
            Task.TaskStatus newStatus = (Task.TaskStatus) JOptionPane.showInputDialog(this, "Select new status for Task #" + taskId,
                    "Update Status", JOptionPane.QUESTION_MESSAGE, null, statuses, taskToUpdate.getStatus());
            if (newStatus != null) {
                taskToUpdate.setStatus(newStatus);
                JOptionPane.showMessageDialog(this, "Task" + taskId + " status updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTasksTable();
            }
        }
    }

    private void runTasksByLineReport(String lineIdStr, JTextArea resultArea) {
        try {
            int lineId = Integer.parseInt(lineIdStr.split(" - ")[0]);
            String result = Task.activeTasks.values().stream()
                    .filter(t -> t.getAssignedProductionLineId() == lineId)
                    .map(t -> "Task #" + t.getTaskId() + " (" + t.getProductNameForTable() + ") - Status: " + t.getStatus())
                    .collect(Collectors.joining("\n"));
            resultArea.setText(result.isEmpty() ? "No tasks found for this line." : result);
            Task.WriteErrMsgs("No tasks found for this line");
        } catch (Exception e) {
            Task.WriteErrMsgs("Invalid selection");
            resultArea.setText("Invalid selection.");
        }
    }

    private void runTasksByProductReport(String productName, JTextArea resultArea) {
        String result = Task.activeTasks.values().stream()
                .filter(t -> t.getProductNameForTable().equalsIgnoreCase(productName))
                .map(t -> "Task #" + t.getTaskId() + " for client '" + t.getClientNameForTable() + "' on Line " + t.getAssignedProductionLineId())
                .collect(Collectors.joining("\n"));
        resultArea.setText(result.isEmpty() ? "No tasks found for this product." : result);
        Task.WriteErrMsgs("No lines found for this product");
    }

    private void runLinesPerProductReport(String productName, JTextArea resultArea) {
        String result = Task.activeTasks.values().stream()
                .filter(t -> t.getProductNameForTable().equalsIgnoreCase(productName))
                .map(t -> String.valueOf(t.getAssignedProductionLineId()))
                .distinct()
                .collect(Collectors.joining("\n"));
        resultArea.setText(result.isEmpty() ? "No lines found for this product." : "Line IDs:\n" + result);
        Task.WriteErrMsgs("No lines found for this product");
    }

    private void runProductsPerLineReport(String lineIdStr, JTextArea resultArea) {
        try {
            int lineId = Integer.parseInt(lineIdStr.split(" - ")[0]);
            String result = Task.activeTasks.values().stream()
                    .filter(t -> t.getAssignedProductionLineId() == lineId)
                    .map(Task::getProductNameForTable)
                    .distinct()
                    .collect(Collectors.joining("\n"));
            Task.WriteErrMsgs("NO products manufactured on this line");
            resultArea.setText(result.isEmpty() ? "No products manufactured on this line." : result);
        } catch (Exception e) { resultArea.setText("Invalid selection."); }
    }

    private void runMostRequestedReport(String startDateStr, JTextArea resultArea) {
        try {
            Task.loadProductionRecordsFromDisk();
            LocalDateTime startDate = LocalDate.parse(startDateStr).atStartOfDay();
            LocalDateTime endDate = LocalDateTime.now();
            Map<String, Integer> productCounts = Task.allProductions.stream()
                    .filter(r -> !r.time.isBefore(startDate) && !r.time.isAfter(endDate))
                    .collect(Collectors.groupingBy(r -> r.productToProduce, Collectors.summingInt(r -> r.quantity)));
            if (productCounts.isEmpty()) {
                Task.WriteErrMsgs("No production records found in this period");
                resultArea.setText("No production records found in this period.");
                return;
            }
            String mostRequested = productCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(entry -> entry.getKey() + " (Total Quantity: " + entry.getValue() + ")")
                    .orElse("N/A");
            resultArea.setText("Most Requested Product:\n" + mostRequested);
        } catch (DateTimeParseException e) {
            resultArea.setText("Invalid date format. Please use YYYY-MM-DD.");
        }
    }

    private void refreshProductsDefTable() {
        productsDefTableModel.setRowCount(0);
        for (Product product : Product.productDefinitions.values()) {
            productsDefTableModel.addRow(new Object[]{product.getProductID(), product.getProductName()});
        }
    }

    private void refreshReqsTable(int productId) {
        reqsTableModel.setRowCount(0);
        Product product = Product.productDefinitions.get(productId);
        if (product != null) {
            for (Map.Entry<Item, Integer> entry : product.getRequiredItems().entrySet()) {
                reqsTableModel.addRow(new Object[]{entry.getKey().getID(), entry.getKey().getName(), entry.getValue()});
            }
        }
    }

    private void handleProductSelection(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting() && productsDefTable.getSelectedRow() != -1) {
            int selectedRow = productsDefTable.convertRowIndexToModel(productsDefTable.getSelectedRow());
            int productId = (Integer) productsDefTableModel.getValueAt(selectedRow, 0);
            refreshReqsTable(productId);
        }
    }

    private void handleAddProduct() {
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(createDialogLabel("Product ID:"));
        panel.add(idField);
        panel.add(createDialogLabel("Product Name:"));
        panel.add(nameField);
        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Product Definition", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int id = Integer.parseInt(idField.getText());
                String name = nameField.getText();
                if(Product.productDefinitions.containsKey(id)){
                    Task.WriteErrMsgs("Product ID exists");
                    JOptionPane.showMessageDialog(this, "Product ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if(name.trim().isEmpty()){
                    Task.WriteErrMsgs("Product name cannot be empty");
                    JOptionPane.showMessageDialog(this, "Product name cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Product newProduct = new Product(id, name);
                Product.productDefinitions.put(id, newProduct);
                Product.saveDefinitionsToFile();
                refreshProductsDefTable();
            } catch (NumberFormatException e) {
                Task.WriteErrMsgs("ID must be a number");
                JOptionPane.showMessageDialog(this, "ID must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteProduct() {
        int selectedRow = productsDefTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = productsDefTable.convertRowIndexToModel(selectedRow);
        int productId = (Integer) productsDefTableModel.getValueAt(modelRow, 0);
        if(JOptionPane.showConfirmDialog(this, "Delete this product definition?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            Product.productDefinitions.remove(productId);
            Product.saveDefinitionsToFile();
            refreshProductsDefTable();
            reqsTableModel.setRowCount(0);
        }
    }

    private void handleAddRequirement() {
        int selectedProdRow = productsDefTable.getSelectedRow();
        if (selectedProdRow == -1) {
            Task.WriteErrMsgs("Please select a product first");
            JOptionPane.showMessageDialog(this, "Please select a product first.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = productsDefTable.convertRowIndexToModel(selectedProdRow);
        int productId = (Integer) productsDefTableModel.getValueAt(modelRow, 0);
        Product targetProduct = Product.productDefinitions.get(productId);
        List<String[]> rawItems = InventoryManager.getAllItems();
        String[] itemChoices = new String[rawItems.size()];
        for(int i=0; i<rawItems.size(); i++){
            itemChoices[i] = (i+1) + ": " + rawItems.get(i)[0];
        }
        if(itemChoices.length == 0){
            Task.WriteErrMsgs("No items in inventory to add");
            JOptionPane.showMessageDialog(this, "No items in inventory to add.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String chosenItemStr = (String) JOptionPane.showInputDialog(this, "Select a material:", "Add Material", JOptionPane.QUESTION_MESSAGE, null, itemChoices, itemChoices[0]);
        if (chosenItemStr == null) return;
        String quantityStr = JOptionPane.showInputDialog(this, "Enter quantity:");
        if (quantityStr == null) return;
        try {
            int itemId = Integer.parseInt(chosenItemStr.split(":")[0]);
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) {
                Task.WriteErrMsgs("The quantity must be positive");
                JOptionPane.showMessageDialog(this, "Quantity must be positive.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String[] itemData = rawItems.get(itemId - 1);
            Item itemToAdd = new Item(itemId, itemData[0], itemData.length > 2 ? itemData[2] : "N/A", 0, Integer.parseInt(itemData[1]), 0);
            targetProduct.addRequiredItem(itemToAdd, quantity);
            Product.saveDefinitionsToFile();
            refreshReqsTable(productId);
        } catch(Exception e) {
            Task.WriteErrMsgs("Invalid input");
            JOptionPane.showMessageDialog(this, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleEditRequirement() {
        JOptionPane.showMessageDialog(this, "This feature can be implemented by removing the material and adding it again with the new quantity.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleDeleteRequirement() {
        int selectedProdRow = productsDefTable.getSelectedRow();
        int selectedReqRow = reqsTable.getSelectedRow();
        if (selectedProdRow == -1 || selectedReqRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a product and a material to remove.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int prodModelRow = productsDefTable.convertRowIndexToModel(selectedProdRow);
        int reqModelRow = reqsTable.convertRowIndexToModel(selectedReqRow);
        int productId = (Integer) productsDefTableModel.getValueAt(prodModelRow, 0);
        int itemId = (Integer) reqsTableModel.getValueAt(reqModelRow, 0);
        Product targetProduct = Product.productDefinitions.get(productId);
        Item itemToRemove = null;
        for(Item item : targetProduct.getRequiredItems().keySet()){
            if(item.getID() == itemId){
                itemToRemove = item;
                break;
            }
        }
        if (itemToRemove != null) {
            targetProduct.removeRequiredItem(itemToRemove);
            Product.saveDefinitionsToFile();
            refreshReqsTable(productId);
        }
    }

    private void handleLogout() {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            this.dispose();
            if(loginFrame != null) loginFrame.setVisible(true);
        }
    }

    public void onTaskFinished(int taskId) {
        System.out.println("GUI: Received notification that Task #" + taskId + " has finished. Refreshing table.");
        refreshTasksTable();
        Task finishedTask = Task.activeTasks.get(taskId);
        if (finishedTask != null && finishedTask.getStatus() == Task.TaskStatus.CANCELLED) {
            JOptionPane.showMessageDialog(
                    this,
                    "Task #" + taskId + " for product '" + finishedTask.getProductNameForTable() + "' was CANCELLED.\n" +
                            "Please check the 'System Logs' tab for details (e.g., insufficient materials).",
                    "Task Cancelled",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProductSupervisorFrame(null));
    }
}
