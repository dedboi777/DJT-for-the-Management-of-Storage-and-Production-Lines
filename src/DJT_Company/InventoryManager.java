package DJT_Company;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
public class InventoryManager {
    private static final String FILE_PATH = "Storage_State.txt";

    public static List<String[]> getAllItems() {
        List<String[]> items = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return items;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    items.add(line.split(","));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    public static void addItem(String name, String qty, String category) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH, true))) {
            pw.println(name + "," + qty + "," + category);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteItem(String itemName) {
        List<String[]> allItems = getAllItems();
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (String[] item : allItems) {
                if (!item[0].equalsIgnoreCase(itemName)) {
                    pw.println(String.join(",", item));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    

    public static void updateItem(String oldName, String newName, String newQty, String newCategory) {
        List<String[]> allItems = getAllItems();
        
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH, false))) {
            for (String[] item : allItems) {
                
                if (item.length > 0 && item[0].equalsIgnoreCase(oldName)) {
                    
                    pw.println(newName + "," + newQty + "," + newCategory);
                } else {
                    
                    pw.println(String.join(",", item));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static boolean deductQuantity(String itemName, int amountToDeduct) {
        List<String[]> allItems = getAllItems();
        boolean success = false;

        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (String[] item : allItems) {
                if (item[0].equalsIgnoreCase(itemName)) {
                    try {
                        int currentQty = Integer.parseInt(item[1]);
                        if (currentQty >= amountToDeduct) {

                            String category = (item.length > 2) ? item[2] : "General";
                            pw.println(item[0] + "," + (currentQty - amountToDeduct) + "," + category);
                            success = true;
                        } else {
                            pw.println(String.join(",", item));
                        }
                    } catch (NumberFormatException e) {
                        pw.println(String.join(",", item));
                    }
                } else {
                    pw.println(String.join(",", item));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }
    public static List<String[]> searchItems(String name, String category, String status) {
        List<String[]> allItems = getAllItems();
        List<String[]> filteredItems = new ArrayList<>();

        for (String[] item : allItems) {

            if (item.length < 2) continue;

            try {
                String itemName = item[0].toLowerCase();
                int qty = Integer.parseInt(item[1].trim());


                String itemCategory = (item.length > 2) ? item[2].toLowerCase() : "general";

                boolean matchesName = name.isEmpty() || itemName.contains(name.toLowerCase());
                boolean matchesCategory = category.isEmpty() || itemCategory.contains(category.toLowerCase());
                boolean matchesStatus = false;

                if (status.equals("All")) {
                    matchesStatus = true;
                } else {
                    int minLimit = 5;
                    if (status.equals("Available") && qty > minLimit) {
                        matchesStatus = true;
                    } else if (status.equals("Finished") && qty == 0) {
                        matchesStatus = true;
                    } else if (status.equals("Below Minimum") && qty > 0 && qty <= minLimit) {
                        matchesStatus = true;
                    }
                }

                if (matchesName && matchesCategory && matchesStatus) {
                    filteredItems.add(item);
                }
            } catch (NumberFormatException e) {

                System.err.println("Skipping invalid quantity for item: " + item[0]);
            }
        }
        return filteredItems;
    }
}