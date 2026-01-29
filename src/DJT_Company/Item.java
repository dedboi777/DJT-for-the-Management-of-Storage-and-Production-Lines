package DJT_Company;
public  class Item {
    private int ID;
    private String name;
    private String category;
    private double price;
    private int stock;
    private int minAllowed;
    private Inventory inv;
    private final Object stockLock = new Object();

    public Item(int ID, String name, String category, double price, int stock, int minAllowed) {
        this.ID = ID;
        this.name = name;
        this.category = category;
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        if (minAllowed < 0) {
            throw new IllegalArgumentException("Minimum allowed cannot be negative");
        }
        this.price = price;
        this.stock = stock;
        this.minAllowed = minAllowed;
        inv = new Inventory();
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }


    public void setCategory(String category) {
        this.category = category;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        synchronized(stockLock) {
            try {
                if (stock >= 0) {
                    this.stock = stock;
                    inv.setMaterialInv(this.name, this.stock);
                }
                else{
                    throw new IllegalArgumentException("The stock must be a positive value");
                }
            } catch (IllegalArgumentException e) {
                String ErrMessage = e.getMessage();
                Task.WriteErrMsgs(ErrMessage);
            }
        }
    }

    public void addStock(int quantity){
        if(quantity > 0) {
            synchronized (stockLock) {
                this.stock += quantity;
                inv.setMaterialInv(this.name, this.stock);
                stockLock.notifyAll();
            }
        }
    }

    public boolean useStock(int quantity){
        synchronized (stockLock) {
            if(quantity > 0  &&  this.stock >= quantity){
                this.stock -= quantity;
                
                return true;
            }
            return false;
        }
    }

    public int getMinAllowed() {
        return minAllowed;
    }

    public void setMinAllowed(int minAllowed) {
        synchronized(stockLock) {
            try {
                if(minAllowed >= 0){
                    this.minAllowed = minAllowed;
                } else {
                    throw new IllegalArgumentException("The minimum must be a positive value");
                }
            } catch (IllegalArgumentException e) {
                String ErrMessage = e.getMessage();
                Task.WriteErrMsgs(ErrMessage);
            }
        }
    }

    public boolean belowMinAllowed(){
        return stock <= minAllowed;
    }

    public double getPrice() {
        synchronized (stockLock) {
            return price;
        }
    }

    public void setPrice(double price) {
        synchronized(stockLock) {
            try {
                if (price >= 0) {
                    this.price = price;
                } else {
                    throw new IllegalArgumentException("The price must be a positive value");
                }
            } catch (IllegalArgumentException e) {
                String ErrMessage = e.getMessage();
                Task.WriteErrMsgs(ErrMessage);
            }
        }
    }


    public double getTotalValue(){
        return price * stock;
    }

    public void printInfo() {
        synchronized(stockLock) {
            System.out.println("Item's name: " + name +
                    "\nItem's id: " + ID +
                    "\nItem's category: " + category +
                    "\nItem's price: " + price +
                    "\nItem's stock: " + stock +
                    "\nThe minimum allowed of this item: " + minAllowed);
        }
    }
}