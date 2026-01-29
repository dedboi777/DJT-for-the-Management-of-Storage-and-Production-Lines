package DJT_Company;
import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Task extends Thread {
    public static Map<Integer, Task> activeTasks = new ConcurrentHashMap<>();

    private volatile boolean isCancelled = false;

    public void cancelTask() {
        this.isCancelled = true;
        this.status = TaskStatus.CANCELLED;
        activeTasks.remove(this.taskId);
        updateAllTasksOnDisk();
        System.out.println("Task #" + taskId + " has been removed from Memory and Disk.");
    }

    public String getClientNameForTable() { return clientName != null ? clientName : client; }
    public String getProductNameForTable() { return productToProduce != null ? productToProduce : (product != null ? product.getProductName() : "N/A"); }
    public int getQuantityForTable() { return quantity > 0 ? quantity : requiredQuantity; }
    

    private static Scanner input = new Scanner(System.in);
    private static final String TASKS_FILE = "tasks_data.txt";
    private int quantity;
    public String productToProduce;
    private String clientName;
    private int taskId;
    private Product product;
    private int requiredQuantity;
    private String client;
    private LocalDate startDate;
    private LocalDate deliverDate;
    private TaskStatus status;
    private int assignedProductionLineId;
    private int completionPercentage;
    private Map<String, Integer> reservedItems;
    private ProductLine assignedLine;
    public static Map<Integer, ProductLine> ProductLines = new HashMap<>();
    private Inventory inv;
    private DailyTrigger dt;
    private static FileWriter FWriter;
    private List<String> tasksAssociatedWProd;
    private List<String> prodsAssociatedWProdLine;
    public static List<ProductionRecord> allProductions = new ArrayList<>();
    private Runnable onCompletion;

    public Task(int assignedProductionLineId, String client, int completionPercentage, LocalDate deliverDate, Product product,
                int requiredQuantity, Map<String, Integer> reservedItems, LocalDate startDate, TaskStatus status, int taskId) {
        this.assignedProductionLineId = assignedProductionLineId;
        this.client = client;
        this.completionPercentage = completionPercentage;
        this.deliverDate = deliverDate;
        this.product = product;
        this.requiredQuantity = requiredQuantity;
        this.reservedItems = reservedItems;
        this.startDate = startDate;
        this.status = status;
        this.taskId = taskId;
        inv = new Inventory();
        dt = new DailyTrigger();
        this.tasksAssociatedWProd = new ArrayList<>();
        this.prodsAssociatedWProdLine = new ArrayList<>();
        this.ProductLines = new HashMap<>();

    }
    public Task(String clientName, String productToProduce, int quantity) {
        this.clientName = clientName;
        this.productToProduce = productToProduce;
        this.quantity = quantity;
        this.taskId = (int)(Math.random() * 1000);
        this.status = TaskStatus.IN_PROGRESS;
        inv = new Inventory();
        dt = new DailyTrigger();
    }
    public static class     ProductionRecord {
        public LocalDateTime time;
        public int taskId;
        public int quantity;
        public String productToProduce;
    }

    public enum TaskStatus {
        IN_PROGRESS, COMPLETED, CANCELLED;
    }

    public int getTaskId() {
        return taskId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(int requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDeliverDate() {
        return deliverDate;
    }

    public void setDeliverDate(LocalDate deliverDate) {
        this.deliverDate = deliverDate;
    }

    public TaskStatus getStatus() {
        return status;
    }





    public void setStatus(TaskStatus newStatus) {
        
        synchronized (this) {
            
            if (this.status == newStatus) return;

            this.status = newStatus;
            System.out.println("✅ Task #" + this.taskId + " status updated to " + this.status + " in memory.");

            
            Task.updateAllTasksOnDisk();

            
            
            
            if (onCompletion != null) {
                SwingUtilities.invokeLater(onCompletion);
            }
        }
    }

    public void startProductionFromGUI(boolean useLocking) {
        System.out.println("--- GUI: Starting production for Task #" + this.taskId + " with locking: " + useLocking + " ---");
        
        Product productDefinition = Product.productDefinitions.values().stream()
                .filter(p -> p.getProductName().equalsIgnoreCase(this.productToProduce))
                .findFirst()
                .orElse(null);

        if (productDefinition == null) {
            System.err.println("Task #" + taskId + " FAILED: Product definition for '" + this.productToProduce + "' not found.");
            this.setStatus(TaskStatus.CANCELLED);
            if (onCompletion != null) SwingUtilities.invokeLater(onCompletion);
            return;
        }
        
        this.setProduct(productDefinition);
        this.setRequiredQuantity(this.quantity);

        
        
        Thread productionThread = new Thread(() -> executeProductionLogicGUI(useLocking));
        productionThread.setDaemon(true);
        productionThread.start();
    }



    

        private boolean acquireLocks(List<String> lockedMaterials) {
        
        if (product == null || product.getRequiredItems() == null || product.getRequiredItems().isEmpty()) {
            System.out.println("Task #" + taskId + ": No materials to lock.");
            return true; 
        }

        
        
        
        List<String> materialNames = product.getRequiredItems().keySet().stream()
                .map(Item::getName)
                .sorted()
                .collect(Collectors.toList());

        
        for (String materialName : materialNames) {
            if (Inventory.lock(materialName)) {
                
                lockedMaterials.add(materialName);
            } else {
                
                
                System.err.println("Task #" + taskId + " FAILED to acquire lock for: '" + materialName + "'. Releasing all acquired locks.");
                Inventory.unlockAll(lockedMaterials); 
                return false; 
            }
        }

        
        return true;
    }



    private void executeProductionLogicGUI(boolean useLocking) {
        List<String> lockedMaterials = new ArrayList<>();
        try {
            if (isCancelled) {
                System.err.println("Task #" + taskId + " aborted, already cancelled.");
                
                if (onCompletion != null) SwingUtilities.invokeLater(onCompletion);
                return;
            }

            Inventory.loadFromFile();

            Product productDefinition = Product.productDefinitions.values().stream()
                    .filter(p -> p.getProductName().equalsIgnoreCase(this.productToProduce))
                    .findFirst().orElse(null);
            if (productDefinition == null) {
                this.setStatus(TaskStatus.CANCELLED);
                if (onCompletion != null) SwingUtilities.invokeLater(onCompletion); 
                return;
            }
            this.setProduct(productDefinition);
            this.setRequiredQuantity(this.quantity);

            if (useLocking) {
                if (!acquireLocks(lockedMaterials)) {
                    this.setStatus(TaskStatus.CANCELLED);
                    if (onCompletion != null) SwingUtilities.invokeLater(onCompletion); 
                    return;
                }
                System.out.println("Task #" + taskId + ": All material locks acquired.");
            }

            
            try {
                System.out.println(">>> TEST DELAY: Simulating production...");
                Thread.sleep(5000);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw e; }
            

            if (!product.canProduce(requiredQuantity)) {
                System.err.println("Task #" + taskId + " FAILED: Insufficient materials.");
                this.setStatus(TaskStatus.CANCELLED);
                if (onCompletion != null) SwingUtilities.invokeLater(onCompletion); 
                return;
            }

            Product.ProductionStatus status = product.consumeMaterials(requiredQuantity, false);
            if (status == Product.ProductionStatus.SUCCESS) {
                System.out.println("✅ Task #" + taskId + " production successful!");
                recordProduction();
                this.setStatus(TaskStatus.COMPLETED);
                if (onCompletion != null) SwingUtilities.invokeLater(onCompletion); 
            } else {
                System.err.println("❌ Task #" + taskId + " FAILED during consumption with status: " + status);
                this.setStatus(TaskStatus.CANCELLED);
                if (onCompletion != null) SwingUtilities.invokeLater(onCompletion); 
            }

        } catch (InterruptedException e) {
            System.err.println("Task: " + taskId + " was interrupted.");
            this.setStatus(TaskStatus.CANCELLED);
            Thread.currentThread().interrupt();
            if (onCompletion != null) SwingUtilities.invokeLater(onCompletion); 
        } catch (Exception e) {
            System.err.println("An unexpected error occurred in Task #" + taskId + ": " + e.getMessage());
            this.setStatus(TaskStatus.CANCELLED);
            e.printStackTrace();
            if (onCompletion != null) SwingUtilities.invokeLater(onCompletion); 
        } finally {
            
            Inventory.unlockAll(lockedMaterials);
            System.out.println("--- Task #" + taskId + ": Lock cleanup finished. ---");
        }
    }


    public int getAssignedProductionLineId() {
        return assignedProductionLineId;
    }

    public void setAssignedProductionLineId(int assignedProductionLineId) {
        this.assignedProductionLineId = assignedProductionLineId;
    }

    public void setAssignedLine(ProductLine assignedLine) {
        this.assignedLine = assignedLine;
    }

    public ProductLine getAssignedLine() {
        return assignedLine;
    }

    public int getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(int completionPercentage) {
        if (completionPercentage < 0)
            completionPercentage = 0;
        if (completionPercentage > 100)
            completionPercentage = 100;
        this.completionPercentage = completionPercentage;
    }

    public Map<String, Integer> getReservedItems() {
        return reservedItems;
    }

    public void setReservedItems(Map<String, Integer> reservedItems) {
        this.reservedItems = reservedItems;
    }

    public void reserveItem(String itemName, int quantity) {
        reservedItems.put(itemName, reservedItems.getOrDefault(itemName, 0) + quantity);
    }

    public void ReleaseReservedItems() {
        reservedItems.clear();
    }

    public void updateCompletion(int unitsCompleted) {
        int totalRequired = getRequiredQuantity();
        if (totalRequired > 0) {
            int newPercentage = (unitsCompleted * 100) / totalRequired;
            setCompletionPercentage(newPercentage);
        }
    }

    public void setOnCompletion(Runnable onCompletion) {
        this.onCompletion = onCompletion;
    }

    public boolean isCompleted() {
        return (status == TaskStatus.COMPLETED);
    }

    public void ReadStorageFile() {
        try(BufferedReader br = new BufferedReader(new FileReader("Storage_State.txt"))) {
            String line;
            char c = 0;
            String name;
            String quantity;
            int validQuantity;
            int commaIndex;
            int dotIndex;
            while((line = br.readLine()) != null) {
                commaIndex = line.indexOf(',');
                dotIndex = line.indexOf('.');
                if (commaIndex == -1) {
                    continue;
                }
                if (dotIndex == -1) {
                    continue;
                }
                if(line.startsWith("Products:")) {
                    c = 1;
                }
                else if(line.startsWith("Materials:")) {
                    c = 2;
                }
                if( c == 1) {
                    if(line.startsWith("Name: ")){
                        name = line.substring(6, commaIndex);
                        quantity = line.substring(commaIndex + 1, dotIndex);
                        validQuantity = Integer.parseInt(quantity);
                        inv.setProdInv(name, validQuantity);
                    }
                }
                else if( c == 2 ) {
                    if(line.startsWith("Name: ")){
                        name = line.substring(6, commaIndex);
                        quantity = line.substring(commaIndex + 1, dotIndex);
                        validQuantity = Integer.parseInt(quantity);
                        inv.setMaterialInv(name, validQuantity);
                    }
                }
            }
        } catch(IOException e) {
            System.out.println("Failed to read: " + e.getMessage());
            String ErrMessage = e.getMessage();
            WriteErrMsgs(ErrMessage);
        }
    }

    public void WriteStorageFile() {
        try (FileWriter fWriter = new FileWriter("Storage_State.txt")) {
            fWriter.write("Products:\n");
            for (Map.Entry<String, Integer> entry : inv.getProdInv().entrySet()){
                String key = entry.getKey();
                int value = entry.getValue();
                fWriter.write("Name: " + key + ", " + value + ".\n");
            }
            fWriter.write("Materials:\n");
            for (Map.Entry<String, Integer> entry : inv.getMaterialInv().entrySet()){
                String key = entry.getKey();
                int value = entry.getValue();
                fWriter.write("Name: " + key + ", " + value + ".\n");
            }
        } catch (IOException e) {
            System.out.println("Writing operation failed: " + e.getMessage());
            String ErrMessage = e.getMessage();
            WriteErrMsgs(ErrMessage);
        }
    }

    public static void WriteErrMsgs(String ErrMsg) {
        if (FWriter == null) {
            try {
                FWriter = new FileWriter("error.txt", true);
            } catch (IOException e) {
                System.err.println("Cannot create file: " + e.getMessage());
                return;

            }
        }
        try {
            FWriter.write("ErrMsg: " + ErrMsg + "\n");
            FWriter.flush();
        } catch (IOException e) {
            System.out.println("Writing operation interrupted: " + e.getMessage());

        }
    }

    
    @Override
    public void run() {
        activeTasks.put(this.taskId, this);
        try {
            ReadStorageFile();
            dt.startChecking(this);

            if (isCancelled) return;

            System.out.println("Before we start any tasks, would you like to lock the materials that are to be used in the following task?(Y:Yes, N:No)");
            String lock = getStringInput().trim().toUpperCase();
            boolean useLocking = "Y".equals(lock);

            if(useLocking) {
                System.out.println("Locking will be used");
            }
            else {
                System.out.println("Locking won't be used");
            }

            while (true) {
                if (isCancelled) {
                    System.out.println("Task " + taskId + " has been cancelled and is stopping...");
                    return;
                }

                System.out.println("""
                                What task would you like to execute?
                                \n0: Exit Task
                                \n1: Produce a product
                                \n2: Add a new product line
                                \n3: Change a product line's status
                                \n4: Show the completed tasks percentage in a product line
                                \n5: Add this task from a product line
                                \n6: Remove this task from a product line
                                \n7: Show tasks for a product line
                                \n8: Show tasks associated with a product
                                \n9: Show product lines associated with a product
                                \n10: Show products associated with a product line
                                \n11: Most produced product in a certain timeline
                                """);

                int choice = getIntInput();
                try{
                    switch (choice) {
                        case 0:
                            System.out.println("Task " + taskId  + " exiting.");
                            return;
                        case 1:
                            makeProd(useLocking);
                            System.out.println("Task: " + taskId + " has finished");
                            break;
                        case 2:
                            int lineId = getIntInput();
                            String lineName = getStringInput();
                            addNewProdLine(lineId, lineName);
                            break;
                        case 3:
                            lineId = getIntInput();
                            String newStatus = getStringInput();
                            changeProdLineStat(lineId, newStatus);
                            break;
                        case 4:
                            lineId = getIntInput();
                            printCompletedTasksPercentageForProdLine(lineId);
                            break;
                        case 5:
                            lineId = getIntInput();
                            addTaskToProdLine(lineId);
                            break;
                        case 6:
                            lineId = getIntInput();
                            removeTaskfromProdLine(lineId);
                            break;
                        case 7:
                            lineId = getIntInput();
                            showTasksForProdLine(lineId);
                            break;
                        case 8:
                            showTasksForCertainProd();
                            break;
                        case 9:
                            showProdLinesAssociatedWProd();
                            break;
                        case 10:
                            showProdsAssociatedWProdLine();
                            break;
                        case 11:
                            LocalDateTime start = getDateTimeInput();
                            LocalDateTime end = getDateTimeInput();
                            findMostProduced(start, end);
                            break;
                        default:
                            throw new AssertionError();
                    }
                } catch (AssertionError e) {
                    String ErrMessage = e.getMessage();
                    WriteErrMsgs(ErrMessage);
                }
            }
        } finally {
            activeTasks.remove(this.taskId);
        }
    }

    private static void findMostProduced(LocalDateTime start, LocalDateTime end) {
        Map<Integer, Integer> totalTasks = new HashMap<>();

        for (ProductionRecord record : allProductions) {
            if (!record.time.isBefore(start) && !record.time.isAfter(end)) {
                totalTasks.put(record.taskId, totalTasks.getOrDefault(record.taskId, 0) + record.quantity);
            }
        }

        if (totalTasks.isEmpty()) {
            return;
        }

        int maxTaskId = 0;
        int maxQuantity = 0;

        for (Map.Entry<Integer, Integer> entry : totalTasks.entrySet()) {
            if (entry.getValue() > maxQuantity) {
                maxQuantity = entry.getValue();
                maxTaskId = entry.getKey();
            }
        }
        System.out.println("Most produced task ID: " + maxTaskId +
                " (quantity: " + maxQuantity + ")");
    }

    private LocalDateTime getDateTimeInput() {
        System.out.println("Enter date (YYYY-MM-DD): ");
        String date = getStringInput();

        System.out.println("Enter time (HH:mm): ");
        String time = getStringInput();

        String dateTime = date + "T" + time + ":00";
        return LocalDateTime.parse(dateTime);
    }

    private void recordProduction() {
        ProductionRecord record = new ProductionRecord();
        record.time = LocalDateTime.now();
        record.taskId = this.taskId;
        record.quantity = this.quantity;
        record.productToProduce = this.productToProduce;
        allProductions.add(record);
        saveProductionRecordsToDisk();
    }

    public void printCompletedTasksPercentageForProdLine(int lineId) {
        ProductLine pL = ProductLines.get(lineId);
        System.out.println(pL.getCompletion());
    }

    private static synchronized int getIntInput() {
        while(true) {
            try {
                int value = input.nextInt();
                input.nextLine();
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input: " + e.getMessage() + " Please enter a number.");
                String ErrMessage = e.getMessage();
                WriteErrMsgs(ErrMessage);
            }
        }
    }

    private static synchronized String getStringInput() {
        return input.nextLine().trim();
    }

    public void showProdsAssociatedWProdLine() {
        System.out.println(prodsAssociatedWProdLine);
    }

    public void showProdLinesAssociatedWProd() {
        if(product == null) {
            System.out.println("No product given for this task");
            return;
        }
        System.out.println(product.getProdLinesAssociatedWProd());
    }

    public void showTasksForCertainProd() {
        if (tasksAssociatedWProd == null || tasksAssociatedWProd.isEmpty()) {
            System.out.println("No tasks associated with this product");
            return;
        }
        System.out.println(tasksAssociatedWProd);
    }

    public void showTasksForProdLine(int lineId) {
        if (ProductLines == null) {
            System.out.println("ProductLines not inititialised");
            return;
        }
        ProductLine pL = ProductLines.get(lineId);
        if (pL != null) {
            pL.getTasks();
        }
    }

    public void removeTaskfromProdLine(int lineId) {
        ProductLine pL = ProductLines.get(lineId);
        pL.removeTask(this);
    }
    public static void saveLinesToDisk() {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter("production_lines.txt", false))) {
            for (ProductLine pl : ProductLines.values()) {
                pw.println(pl.getLineId() + "," + pl.getLineName() + "," + pl.getStatus());
            }
            System.out.println("✅ Production lines updated on disk.");
        } catch (java.io.IOException e) {
            System.err.println("❌ Failed to save lines: " + e.getMessage());
        }
    }
    public void addTaskToProdLine(int lineId) {
        ProductLine pL = ProductLines.get(lineId);
        pL.addTask(this);
        product.getProdLinesAssociatedWProd().add(lineId);
        prodsAssociatedWProdLine.add(product.getProductName());
    }

    public void changeProdLineStat(int lineId, String status) {
        ProductLine pL = ProductLines.get(lineId);
        pL.setStatus(status);
    }

    public static void addNewProdLine(int lineId, String lineName) {
        ProductLine pL = new ProductLine(lineId, lineName);
        ProductLines.put(lineId, pL);
    }

    public void makeProd(boolean useLocking) {
        if (isCancelled) return;

        tasksAssociatedWProd.add("Making product " + product.getProductName());
        List<String> lockedMaterials = new ArrayList<>();
        try {
            if (useLocking) {
                Map<Item, Integer> requiredItems = product.getRequiredItems();

                if (requiredItems == null || requiredItems.isEmpty()) {
                    System.out.println("No materials required for task " + taskId);
                    this.status = TaskStatus.COMPLETED;
                    return;
                }

                List<String> materialNames = new ArrayList<>();
                for (Item item : requiredItems.keySet()) {
                    if (item != null && item.getName() != null) {
                        materialNames.add(item.getName());
                    }
                }

                if (materialNames.isEmpty()) {
                    System.out.println("Task " + taskId + " has no valid materials to lock");
                    this.status = TaskStatus.COMPLETED;
                    return;
                }

                Collections.sort(materialNames);

                boolean allLocksAcquired = true;
                for (String materialName : materialNames) {
                    if (Inventory.lock(materialName)) {
                        lockedMaterials.add(materialName);
                    } else {
                        System.out.println("Failed to lock: " + materialName);
                        allLocksAcquired = false;
                        break;
                    }
                }

                if (!allLocksAcquired) {
                    Collections.reverse(lockedMaterials);
                    for (String locked : lockedMaterials) {
                        Inventory.unlock(locked);
                    }
                    this.status = TaskStatus.CANCELLED;
                    return;
                }
                System.out.println("All materials locked successfully");
            }

            System.out.println("Running task: " + taskId);

            if (product == null) {
                System.out.println("Task " + taskId + " has no product assigned");
                this.status = TaskStatus.CANCELLED;
                return;
            }

            if (!product.canProduce(requiredQuantity)) {
                System.out.println("Insufficient materials for task " + taskId);
                System.out.println("Options:");
                System.out.println("1. Wait for materials to be restocked");
                System.out.println("2. Cancel this task");
                System.out.print("Choose (1 or 2): ");

                String choice = getStringInput();

                if ("1".equals(choice)) {
                    System.out.println("Waiting for materials...");
                    int waitAttempts = 0;
                    int maxWaitAttempts = 60;

                    while (!product.canProduce(requiredQuantity) && waitAttempts < maxWaitAttempts) {
                        if (isCancelled) return;

                        if (useLocking) {
                            Collections.reverse(lockedMaterials);
                            for (String material : lockedMaterials) {
                                Inventory.unlock(material);
                            }
                            lockedMaterials.clear();
                        }

                        Thread.sleep(500);

                        if (useLocking) {
                            Map<Item, Integer> requiredItems = product.getRequiredItems();
                            List<String> materialNames = new ArrayList<>();
                            for (Item item : requiredItems.keySet()) {
                                if (item != null && item.getName() != null) {
                                    materialNames.add(item.getName());
                                }
                            }
                            Collections.sort(materialNames);

                            boolean allReacquired = true;
                            for (String materialName : materialNames) {
                                if (Inventory.lock(materialName)) {
                                    lockedMaterials.add(materialName);
                                } else {
                                    System.out.println("Could not re-acquire lock for: " + materialName);
                                    allReacquired = false;
                                    break;
                                }
                            }

                            if (!allReacquired) {
                                Collections.reverse(lockedMaterials);
                                for (String material : lockedMaterials) {
                                    Inventory.unlock(material);
                                }
                                this.status = TaskStatus.CANCELLED;
                                return;
                            }
                        }

                        waitAttempts++;
                        if (waitAttempts % 10 == 0) {
                            System.out.print(".");
                        }
                    }

                    if (!product.canProduce(requiredQuantity)) {
                        System.out.println("\nTimeout: Materials not available after 30 seconds");
                        this.status = TaskStatus.CANCELLED;
                        return;
                    }
                } else {
                    System.out.println("Task cancelled by the user");
                    this.status = TaskStatus.CANCELLED;
                    return;
                }
            }

            Product.ProductionStatus status = product.consumeMaterials(requiredQuantity, false);
            if (status == Product.ProductionStatus.SUCCESS) {
                System.out.println("Task " + taskId + " production successful");
                recordProduction();
                updateCompletion(requiredQuantity);
                this.status = TaskStatus.COMPLETED;
            } else {
                System.out.println("Task " + taskId + " failed: " + status);
                this.status = TaskStatus.CANCELLED;
            }

        } catch (InterruptedException e) {
            System.out.println("Task: " + taskId + " interrupted.");
            String ErrMessage = e.getMessage();
            WriteErrMsgs(ErrMessage);
            Thread.currentThread().interrupt();
            this.status = TaskStatus.CANCELLED;

        } finally {
            if (useLocking && lockedMaterials != null && !lockedMaterials.isEmpty()) {
                Collections.reverse(lockedMaterials);
                for (String material : lockedMaterials) {
                    try {
                        Inventory.unlock(material);
                    } catch (Exception e) {
                        System.err.println("Error unlocking " + material + ": " + e.getMessage());
                    }
                }
            }
        }
    }
    public static List<Task> filterTasksByStatus(List<Task> allTasks, TaskStatus status) {
        List<Task> filteredList = new ArrayList<>();
        for (Task task : allTasks) {
            if (task.getStatus() == status) {
                filteredList.add(task);
            }
        }
        return filteredList;
    }
    public static void saveProductionRecordsToDisk() {
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter("production_history.txt", false))) {
            for (ProductionRecord record : allProductions) {
                
                pw.println(record.time + "," + record.taskId + "," + record.quantity + "," + record.productToProduce);
            }
            System.out.println("✅ Production history backed up to disk.");
        } catch (java.io.IOException e) {
            System.err.println("❌ Failed to save history: " + e.getMessage());
        }
    }
    public static void loadProductionRecordsFromDisk() {
        java.io.File file = new java.io.File("production_history.txt");
        if (!file.exists()) return;

        allProductions.clear();
        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 4) {
                    ProductionRecord record = new ProductionRecord();
                    record.time = LocalDateTime.parse(data[0]);
                    record.taskId = Integer.parseInt(data[1]);
                    record.quantity = Integer.parseInt(data[2]);
                    record.productToProduce = data[3];
                    allProductions.add(record);
                }
            }
            System.out.println("✅ Loaded " + allProductions.size() + " historical production records.");
        } catch (Exception e) {
            System.out.println("❌ Error loading history: " + e.getMessage());
        }
    }
    public void startTaskProcess() {
        System.out.println("--- System: Production Process Started ---");
        this.start();
    }
    public static void loadLinesFromDisk() {
        String filePath = "production_lines.txt";
        java.io.File file = new java.io.File(filePath);

        if (!file.exists()) return;

        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 2) {
                    int id = Integer.parseInt(data[0].trim());
                    String name = data[1].trim();

                    ProductLine pl = new ProductLine(id, name);

                    if (data.length >= 3) {
                        pl.setStatus(data[2].trim());
                    }

                    ProductLines.put(id, pl);
                }
            }
            System.out.println("✅ Loaded " + ProductLines.size() + " lines with their status.");
        } catch (Exception e) {
            System.out.println("❌ Error loading lines: " + e.getMessage());
        }
    }

    public void saveTaskToDisk() {
        try (java.io.FileWriter fw = new java.io.FileWriter("tasks_data.txt", true);
             java.io.PrintWriter pw = new java.io.PrintWriter(fw)) {

            String client = getClientNameForTable().trim();
            String product = getProductNameForTable().trim();


            pw.println(taskId + "," + client + "," + product + "," +
                    getQuantityForTable() + "," + assignedProductionLineId + "," + status);

            System.out.println("Task #" + taskId + " saved to disk.");
        } catch (java.io.IOException e) {
            System.err.println("Failed to save task: " + e.getMessage());
        }
    }
    public static synchronized void updateAllTasksOnDisk() {
        String filePath = "tasks_data.txt";
        try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(filePath, false))) {
            if (activeTasks != null) {
                for (Task t : activeTasks.values()) {
                    String id = String.valueOf(t.taskId);
                    String client = t.getClientNameForTable().trim();
                    String product = t.getProductNameForTable().trim();
                    String qty = String.valueOf(t.getQuantityForTable());
                    String lineId = String.valueOf(t.getAssignedProductionLineId());
                    String status = t.getStatus().toString(); 
                    pw.println(id + "," + client + "," + product + "," + qty + "," + lineId + "," + status);
                }
            }
            System.out.println("💾 All tasks status synced to disk successfully.");
        } catch (java.io.IOException e) {
            System.err.println("❌ Critical Error: Could not save tasks to disk! " + e.getMessage());
        }
    }
    public static void loadTasksFromDisk() {
        java.io.File file = new java.io.File("tasks_data.txt");
        if (!file.exists()) return;

        activeTasks.clear();

        try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                if (data.length >= 6) {
                    try {
                        int id = Integer.parseInt(data[0].trim());
                        String client = data[1].trim();
                        String product = data[2].trim();
                        int qty = Integer.parseInt(data[3].trim());
                        int lineId = Integer.parseInt(data[4].trim());
                        String statusStr = data[5].trim(); 


                        Task t = new Task(client, product, qty);
                        t.taskId = id;
                        t.setAssignedProductionLineId(lineId);


                        try {
                            t.status = TaskStatus.valueOf(statusStr);
                        } catch (IllegalArgumentException e) {

                            t.status = TaskStatus.IN_PROGRESS;
                        }


                        activeTasks.put(id, t);

                    } catch (NumberFormatException e) {
                        System.err.println("Skipping corrupted line: " + line);
                    }
                }
            }
            System.out.println("✅ " + activeTasks.size() + " tasks loaded with their correct status.");
        } catch (Exception e) {
            System.err.println("❌ Error loading tasks from disk: " + e.getMessage());
            WriteErrMsgs(e.getMessage());
        }
    }
    public void printInfo() {
        System.out.println("Task id: " + taskId +
                "\nProduct: " + product +
                "\nRequired quantity: " + requiredQuantity +
                "\nClient: " + client +
                "\nStarting date: " + startDate +
                "\nDelivery date: " + deliverDate +
                "\nTask status: " + status +
                "\nAssigned product line id: " + assignedProductionLineId +
                "\nCompletion percentage: " + completionPercentage +
                "\nReserved items: " + reservedItems +
                "\nAssigned line: " + assignedLine);
    }
}