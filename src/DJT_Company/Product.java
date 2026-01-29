package DJT_Company;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Product {
    private int productID;
    private String productName;
    private Map<Item, Integer> requiredItems;
    private double price;
    private Inventory inv;
    private static List<Integer> prodLinesAssociatedWProd;

    
    public static Map<Integer, Product> productDefinitions = new ConcurrentHashMap<>();
    private static final String DEFINITIONS_FILE = "product_definitions.txt";
    

    public enum ProductionStatus {
        SUCCESS,
        INSUFFICIENT_MATERIALS,
        WAITING_FOR_MATERIALS,
        FAILED
    }

    
        public Product(int productID, String productName) {
        this.productID = productID;
        this.productName = productName;
        this.requiredItems = new HashMap<>(); 
        this.inv = new Inventory();
        if (prodLinesAssociatedWProd == null) {
            prodLinesAssociatedWProd = new ArrayList<>();
        }
    }

    
        public Product(int productID, String productName, double price) {
        this(productID, productName); 
        this.price = price; 
    }

    
    
    

    public static void saveDefinitionsToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(DEFINITIONS_FILE))) {
            for (Product product : productDefinitions.values()) {
                
                writer.println("PROD:" + product.getProductID() + "," + product.getProductName());
                
                for (Map.Entry<Item, Integer> entry : product.getRequiredItems().entrySet()) {
                    Item item = entry.getKey();
                    Integer quantity = entry.getValue();
                    
                    writer.println("REQ:" + item.getID() + "," + quantity);
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving product definitions: " + e.getMessage());
            Task.WriteErrMsgs("Error saving product definitions: " + e.getMessage());
        }
    }

    public static void loadDefinitionsFromFile() {
        productDefinitions.clear();
        File file = new File(DEFINITIONS_FILE);
        if (!file.exists()) {
            return; 
        }

        
        
        
        Map<Integer, Item> allItemsById = new HashMap<>();
        List<String[]> rawItems = InventoryManager.getAllItems();
        for (int i = 0; i < rawItems.size(); i++) {
            String[] rawItem = rawItems.get(i);
            try {
                
                int id = i + 1;
                Item item = new Item(id, rawItem[0], rawItem.length > 2 ? rawItem[2] : "N/A", 0, Integer.parseInt(rawItem[1]), 0);
                allItemsById.put(id, item);
            } catch (Exception e) {
                System.err.println("Skipping invalid item in inventory while loading definitions: " + String.join(",", rawItem));
            }
        }
        


        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            Product currentProduct = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("PROD:")) {
                    String[] parts = line.substring(5).split(",", 2);
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    currentProduct = new Product(id, name); 
                    productDefinitions.put(id, currentProduct);
                } else if (line.startsWith("REQ:") && currentProduct != null) {
                    String[] parts = line.substring(4).split(",");
                    int itemId = Integer.parseInt(parts[0]);
                    int quantity = Integer.parseInt(parts[1]);

                    Item requiredItem = allItemsById.get(itemId); 
                    if (requiredItem != null) {
                        currentProduct.addRequiredItem(requiredItem, quantity);
                    } else {
                        System.err.println("Could not find item with ID: " + itemId + " for product " + currentProduct.getProductName());
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading product definitions: " + e.getMessage());
            Task.WriteErrMsgs("Error loading product definitions: " + e.getMessage());
        }
    }

    public List<Integer> getProdLinesAssociatedWProd() {
        return prodLinesAssociatedWProd;
    }

    public double getPrice() {
        return calcPrice();
    }

    public double calcPrice() {
        double totalCost = 0.0;
        for (Map.Entry<Item, Integer> entry : requiredItems.entrySet()) {
            Item item = entry.getKey();
            int quantity = entry.getValue();
            totalCost += item.getPrice() * quantity;
        }
        return totalCost;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Map<Item, Integer> getRequiredItems() {
        return requiredItems;
    }

    public void setRequiredItems(Map<Item, Integer> requiredItems) {
        this.requiredItems = requiredItems;
    }

    public void addRequiredItem(Item item, int quantity) {
        try {
            if (quantity >= 0) {
                requiredItems.put(item, quantity);
            } else {
                Task.WriteErrMsgs("Quantity cannot be negative.");
                throw new IllegalArgumentException("Quantity cannot be negative.");
            }
        } catch (IllegalArgumentException e) {
            String ErrMessage = e.getMessage();
            Task.WriteErrMsgs(ErrMessage);
        }
        this.price = calcPrice();
    }

    public void removeRequiredItem(Item item) {
        requiredItems.remove(item);
        this.price = calcPrice();
    }

    public void updateRequiredItemQuantity(Item item, int newQuantity) {
        try {
            if (requiredItems.containsKey(item)) {
                if (newQuantity >= 0) {
                    requiredItems.put(item, newQuantity);
                } else {
                    Task.WriteErrMsgs("The quantity must be larger or equal to zero");
                    throw new IllegalArgumentException("The quantity must be larger or equal to zero");
                }
            } else {
                Task.WriteErrMsgs("The item does not exist");
                throw new IllegalArgumentException("The item does not exist");
            }
        } catch (IllegalArgumentException e) {
            String ErrMessage = e.getMessage();
            Task.WriteErrMsgs(ErrMessage);
        }
        this.price = calcPrice();
    }

    public double CalculateProductionCost(int quantity) {
        return getPrice() * quantity;
    }

    


    public ProductionStatus consumeMaterials(int quantity, boolean waitIfUnavailable) {
        if (!canProduce(quantity)) {
            if (waitIfUnavailable) {
                return ProductionStatus.WAITING_FOR_MATERIALS;
            } else {
                return ProductionStatus.INSUFFICIENT_MATERIALS;
            }
        }
        try {
            doConsumption(quantity);
            return ProductionStatus.SUCCESS;
        } catch (Exception e) {
            return ProductionStatus.FAILED;
        }
    }

    public boolean canProduce(int quantity) {
        Inventory.loadFromFile();
        System.out.println("Checking if production is possible for " + quantity + "x '" + this.productName + "'...");

        
        for (Map.Entry<Item, Integer> entry : requiredItems.entrySet()) {
            String itemName = entry.getKey().getName();
            int requiredAmount = entry.getValue() * quantity;

            
            int liveStock = Inventory.getMaterialInv().getOrDefault(itemName, 0);

            if (liveStock < requiredAmount) {
                Task.WriteErrMsgs("Production check FAILED for item '" + itemName + "'. Required: " + requiredAmount + ", Available: " + liveStock);
                System.err.println("Production check FAILED for item '" + itemName + "'. Required: " + requiredAmount + ", Available: " + liveStock);
                return false; 
            }
        }

        System.out.println("Production check PASSED. All materials are available.");
        return true; 
    }



    private void doConsumption(int quantity) {
        
        for (Map.Entry<Item, Integer> entry : requiredItems.entrySet()) {
            String itemName = entry.getKey().getName();
            int requiredQuantity = entry.getValue() * quantity;

            
            
            if (!Inventory.consumeMaterial(itemName, requiredQuantity)) {
                Task.WriteErrMsgs("CRITICAL: Consumption failed for " + itemName + " despite pre-check.");
                throw new IllegalStateException("CRITICAL: Consumption failed for " + itemName + " despite pre-check.");
            }
        }

        
        Inventory.updateProdInv(this.productName, quantity);
        Inventory.saveToFile();
    }

    public void printInfo() {
        System.out.println("Product's id: " + productID +
                "\nProduct's name: " + productName +
                "\nRequired Items: " + requiredItems +
                "\nProduct's price: " + getPrice());
    }
}

