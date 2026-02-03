package DJT_Company;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
public class Inventory {
    public static final Object PRODUCTION_LOCK = new Object();
    private static Map<String, Integer> productsInventory = new ConcurrentHashMap<>();
    private static Map<String, Integer> rawMaterialsInventory = new ConcurrentHashMap<>();
    private static Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private static final String RAW_FILE = "Storage_State.txt";
    private static final String PROD_FILE = "Produced_Inventory.txt";
    public static Map<String, Integer> getRawInv() {
        return rawMaterialsInventory;
    }
    public static void saveToFile() {
        Map<String, String> categoryMap = new HashMap<>();
        File rawFile = new File(RAW_FILE);
        if (rawFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(rawFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        // parts[0] = Name, parts[2] = Category
                        categoryMap.put(parts[0].trim(), parts[2].trim());
                    }
                }
            } catch (IOException e) {
                System.err.println("Could not pre-read categories, they might be lost: " + e.getMessage());
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(RAW_FILE))) {
            for (Map.Entry<String, Integer> entry : rawMaterialsInventory.entrySet()) {
                String name = entry.getKey();
                Integer quantity = entry.getValue();

                String category = categoryMap.getOrDefault(name, "N/A");
                bw.write(name + "," + quantity + "," + category);
                bw.newLine();
            }
        } catch (IOException e) {
            Task.WriteErrMsgs("Error saving raw materials: " + e.getMessage());
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PROD_FILE))) {
            for (Map.Entry<String, Integer> entry : productsInventory.entrySet()) {
                bw.write(entry.getKey() + "," + entry.getValue());
                bw.newLine();
            }
        } catch (IOException e) {
            Task.WriteErrMsgs("Error saving products: " + e.getMessage());
        }
    }

    public static void loadFromFile() {
        rawMaterialsInventory.clear();
        productsInventory.clear();
        File rawFile = new File(RAW_FILE);
        if (rawFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(rawFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        try {
                            String name = parts[0].trim();
                            int quantity = Integer.parseInt(parts[1].trim());
                            rawMaterialsInventory.put(name, quantity);
                        } catch (NumberFormatException e) {
                            System.err.println("Skipping corrupted inventory line (bad number): " + line);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Error loading raw materials file: " + e.getMessage());
            }
        }
        System.out.println("✅ Inventory loaded. " + rawMaterialsInventory.size() + " raw materials in memory.");

        File prodFile = new File(PROD_FILE);
        if (prodFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(prodFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        productsInventory.put(parts[0], Integer.parseInt(parts[1]));
                    }
                }
            } catch (IOException | NumberFormatException e) {
                System.err.println("Error loading products file: " + e.getMessage());
            }
        }
    }
    public static Map<String, Integer> getMaterialInv() {
        return rawMaterialsInventory;
    }

    public static void setMaterialInv(String name, int quantity) {
        try {
            if (quantity < 0) {
                throw new IllegalArgumentException("Quantity cannot be a negative value");
            }
        } catch (IllegalArgumentException e) {
            String ErrMessage = e.getMessage();
            Task.WriteErrMsgs(ErrMessage);
            return;
        }
        rawMaterialsInventory.put(name, quantity);
    }

    public static Map<String, Integer> getProdInv() {
        return productsInventory;
    }

    public static void setProdInv(String name, int quantity) {
        try {
            if (quantity < 0) {
                throw new IllegalArgumentException("Quantity cannot be a negative value");
            }
        } catch (IllegalArgumentException e) {
            String ErrMessage = e.getMessage();
            Task.WriteErrMsgs(ErrMessage);
            return;
        }
        productsInventory.put(name, quantity);
    }

    public static void updateProdInv(String name, int quantity) {
        Integer currentInv = productsInventory.getOrDefault(name, 0);
        int sum = currentInv + quantity;
        try {
            if (sum < 0) {
                throw new IllegalArgumentException("Cannot update " + name + " to a negative inventory: " +
                        currentInv + " + " + quantity + " = " + sum);
            }
        } catch (IllegalArgumentException e) {
            String ErrMessage = e.getMessage();
            Task.WriteErrMsgs(ErrMessage);
            return;
        }
        productsInventory.put(name, sum);
        saveToFile();
    }

    public static void unlock(String materialId) {
        ReentrantLock lock = locks.get(materialId);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    public static boolean lock(String materialId) {
        ReentrantLock lock = locks.computeIfAbsent(materialId, k -> new ReentrantLock());
        try {
            return lock.tryLock(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("Interrupted by user: " + e.getMessage());
            String ErrMessage = e.getMessage();
            Task.WriteErrMsgs(ErrMessage);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public static void unlockAll(List<String> materialIds) {
        for (String materialId : materialIds) {
            unlock(materialId);
        }
    }
    



    
    public static synchronized boolean consumeMaterial(String name, int quantityToConsume) {
        Integer currentStock = rawMaterialsInventory.get(name);

        
        if (currentStock != null && currentStock >= quantityToConsume) {
            
            rawMaterialsInventory.put(name, currentStock - quantityToConsume);
            System.out.println("Inventory Log: Consumed " + quantityToConsume + " of '" + name + "'. New stock: " + (currentStock - quantityToConsume));
            return true; 
        }

        
        System.err.println("Inventory Log: FAILED to consume " + quantityToConsume + " of '" + name + "'. Stock is " + currentStock);
        return false; 
    }


}