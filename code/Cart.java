// Cart.java
import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<CartItem> items;

    public Cart() {
        this.items = new ArrayList<>();
    }

    public void addItem(Product product, int quantity) {
        // Check if product already in cart
        for (CartItem item : items) {
            if (item.getProduct().getProductId() == product.getProductId()) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        // Add new item
        items.add(new CartItem(product, quantity));
    }

    public void removeItem(int productId) {
        items.removeIf(item -> item.getProduct().getProductId() == productId);
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double getTotal() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    public void displayCart() {
        if (items.isEmpty()) {
            System.out.println("\nCart is empty!");
            return;
        }

        System.out.println("\n=== Shopping Cart ===");
        System.out.printf("%-20s %-10s %-12s %-12s%n", 
            "Product", "Price", "Quantity", "Subtotal");
        System.out.println("-------------------------------------------------------");

        for (CartItem item : items) {
            System.out.printf("%-20s Rs.%-8.2f %-12d Rs.%-10.2f%n",
                item.getProduct().getName(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                item.getSubtotal()
            );
        }

        System.out.println("-------------------------------------------------------");
        System.out.printf("Total: Rs.%.2f%n", getTotal());
    }

    public void clear() {
        items.clear();
    }
}