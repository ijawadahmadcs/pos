// Product.java
public class Product {
    private int productId;
    private String name;
    private int categoryId;
    private double price;
    private int stockQuantity;

    public Product(int productId, String name, int categoryId, double price, int stockQuantity) {
        this.productId = productId;
        this.name = name;
        this.categoryId = categoryId;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public int getProductId() { return productId; }
    public String getName() { return name; }
    public int getCategoryId() { return categoryId; }
    public double getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
}