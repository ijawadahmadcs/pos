// Main.java
import java.util.Scanner;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static DatabaseManager db = new DatabaseManager();
    private static User currentUser = null;

    public static void main(String[] args) {
        System.out.println("=== POS System ===");
        
        if (login()) {
            showMainMenu();
        }
        
        db.close();
        scanner.close();
    }

    private static boolean login() {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        
        currentUser = db.authenticateUser(username, password);
        
        if (currentUser != null) {
            System.out.println("\nWelcome, " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
            return true;
        } else {
            System.out.println("Invalid credentials!");
            return false;
        }
    }

    private static void showMainMenu() {
        while (true) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. New Sale");
            System.out.println("2. Manage Products");
            System.out.println("3. Manage Customers");
            System.out.println("4. View Reports");
            System.out.println("5. Logout");
            System.out.print("Choose option: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    newSale();
                    break;
                case 2:
                    if (currentUser.getRole().equals("Admin")) {
                        manageProducts();
                    } else {
                        System.out.println("Admin access required!");
                    }
                    break;
                case 3:
                    manageCustomers();
                    break;
                case 4:
                    viewReports();
                    break;
                case 5:
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    private static void newSale() {
        Cart cart = new Cart();
        
        while (true) {
            System.out.println("\n=== New Sale ===");
            System.out.println("1. Add Product to Cart");
            System.out.println("2. View Cart");
            System.out.println("3. Complete Sale");
            System.out.println("4. Cancel");
            System.out.print("Choose option: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    addProductToCart(cart);
                    break;
                case 2:
                    cart.displayCart();
                    break;
                case 3:
                    completeSale(cart);
                    return;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    private static void addProductToCart(Cart cart) {
        db.displayProducts();
        System.out.print("Enter Product ID: ");
        int productId = getIntInput();
        System.out.print("Enter Quantity: ");
        int quantity = getIntInput();
        
        Product product = db.getProductById(productId);
        if (product != null) {
            if (product.getStockQuantity() >= quantity) {
                cart.addItem(product, quantity);
                System.out.println("Added to cart!");
            } else {
                System.out.println("Insufficient stock! Available: " + product.getStockQuantity());
            }
        } else {
            System.out.println("Product not found!");
        }
    }

    private static void completeSale(Cart cart) {
        if (cart.getItems().isEmpty()) {
            System.out.println("Cart is empty!");
            return;
        }
        
        System.out.print("Enter Customer Phone (or press Enter to skip): ");
        scanner.nextLine(); // consume newline
        String phone = scanner.nextLine();
        
        Customer customer = null;
        if (!phone.isEmpty()) {
            customer = db.getCustomerByPhone(phone);
            if (customer == null) {
                System.out.print("Customer not found. Add new customer? (y/n): ");
                if (scanner.nextLine().equalsIgnoreCase("y")) {
                    System.out.print("Customer Name: ");
                    String name = scanner.nextLine();
                    customer = db.addCustomer(name, phone);
                }
            }
        }
        
        int orderId = db.createOrder(cart, currentUser, customer);
        if (orderId > 0) {
            System.out.println("\n=== Sale Completed ===");
            System.out.println("Order ID: " + orderId);
            System.out.println("Total: Rs. " + String.format("%.2f", cart.getTotal()));
            System.out.println("Thank you!");
        } else {
            System.out.println("Sale failed!");
        }
    }

    private static void manageProducts() {
        while (true) {
            System.out.println("\n=== Manage Products ===");
            System.out.println("1. View All Products");
            System.out.println("2. Add Product");
            System.out.println("3. Update Stock");
            System.out.println("4. Back");
            System.out.print("Choose option: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    db.displayProducts();
                    break;
                case 2:
                    addProduct();
                    break;
                case 3:
                    updateStock();
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    private static void addProduct() {
        scanner.nextLine(); // consume newline
        System.out.print("Product Name: ");
        String name = scanner.nextLine();
        
        db.displayCategories();
        System.out.print("Category ID: ");
        int categoryId = getIntInput();
        
        System.out.print("Price: ");
        double price = getDoubleInput();
        
        System.out.print("Stock Quantity: ");
        int stock = getIntInput();
        
        if (db.addProduct(name, categoryId, price, stock)) {
            System.out.println("Product added successfully!");
        } else {
            System.out.println("Failed to add product!");
        }
    }

    private static void updateStock() {
        db.displayProducts();
        System.out.print("Enter Product ID: ");
        int productId = getIntInput();
        System.out.print("Enter Quantity to Add: ");
        int quantity = getIntInput();
        
        if (db.addStock(productId, quantity)) {
            System.out.println("Stock updated successfully!");
        } else {
            System.out.println("Failed to update stock!");
        }
    }

    private static void manageCustomers() {
        while (true) {
            System.out.println("\n=== Manage Customers ===");
            System.out.println("1. View All Customers");
            System.out.println("2. Add Customer");
            System.out.println("3. Back");
            System.out.print("Choose option: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    db.displayCustomers();
                    break;
                case 2:
                    scanner.nextLine(); // consume newline
                    System.out.print("Customer Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Phone: ");
                    String phone = scanner.nextLine();
                    
                    if (db.addCustomer(name, phone) != null) {
                        System.out.println("Customer added successfully!");
                    } else {
                        System.out.println("Failed to add customer!");
                    }
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    private static void viewReports() {
        System.out.println("\n=== Sales Report ===");
        db.displayRecentOrders();
    }

    private static int getIntInput() {
        while (!scanner.hasNextInt()) {
            scanner.next();
            System.out.print("Invalid input. Try again: ");
        }
        int value = scanner.nextInt();
        return value;
    }

    private static double getDoubleInput() {
        while (!scanner.hasNextDouble()) {
            scanner.next();
            System.out.print("Invalid input. Try again: ");
        }
        double value = scanner.nextDouble();
        return value;
    }
}