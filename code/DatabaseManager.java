// DatabaseManager.java
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private Connection conn;
    
    // Update these with your database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/pos_system";
    private static final String USER = "root";
    private static final String PASSWORD = "Pakistan@2025";

    public DatabaseManager() {
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
    }

    public User authenticateUser(String username, String password) {
        String sql = "SELECT user_id, username, role FROM Users WHERE username = ? AND password = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("role")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        return null;
    }

    public void displayProducts() {
        String sql = "SELECT p.product_id, p.name, c.name as category, p.price, p.stock_quantity " +
                     "FROM Products p JOIN Categories c ON p.category_id = c.category_id " +
                     "ORDER BY p.product_id";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\n=== Products ===");
            System.out.printf("%-5s %-20s %-15s %-10s %-10s%n", 
                "ID", "Name", "Category", "Price", "Stock");
            System.out.println("--------------------------------------------------------------");
            
            while (rs.next()) {
                System.out.printf("%-5d %-20s %-15s Rs.%-8.2f %-10d%n",
                    rs.getInt("product_id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getInt("stock_quantity")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public Product getProductById(int productId) {
        String sql = "SELECT * FROM Products WHERE product_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Product(
                    rs.getInt("product_id"),
                    rs.getString("name"),
                    rs.getInt("category_id"),
                    rs.getDouble("price"),
                    rs.getInt("stock_quantity")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        return null;
    }

    public boolean addProduct(String name, int categoryId, double price, int stock) {
        String sql = "INSERT INTO Products (name, category_id, price, stock_quantity) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, categoryId);
            stmt.setDouble(3, price);
            stmt.setInt(4, stock);
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public boolean addStock(int productId, int quantity) {
        String sql = "UPDATE Products SET stock_quantity = stock_quantity + ? WHERE product_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            
            boolean success = stmt.executeUpdate() > 0;
            
            if (success) {
                logInventory(productId, "IN", quantity);
            }
            
            return success;
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    public void displayCategories() {
        String sql = "SELECT * FROM Categories";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\n=== Categories ===");
            while (rs.next()) {
                System.out.printf("%d. %s%n", 
                    rs.getInt("category_id"),
                    rs.getString("name")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public Customer getCustomerByPhone(String phone) {
        String sql = "SELECT * FROM Customers WHERE phone = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return new Customer(
                    rs.getInt("customer_id"),
                    rs.getString("name"),
                    rs.getString("phone")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        return null;
    }

    public Customer addCustomer(String name, String phone) {
        String sql = "INSERT INTO Customers (name, phone) VALUES (?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, phone);
            
            if (stmt.executeUpdate() > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    return new Customer(rs.getInt(1), name, phone);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        return null;
    }

    public void displayCustomers() {
        String sql = "SELECT * FROM Customers ORDER BY customer_id";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\n=== Customers ===");
            System.out.printf("%-5s %-25s %-20s%n", "ID", "Name", "Phone");
            System.out.println("---------------------------------------------------");
            
            while (rs.next()) {
                System.out.printf("%-5d %-25s %-20s%n",
                    rs.getInt("customer_id"),
                    rs.getString("name"),
                    rs.getString("phone")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public int createOrder(Cart cart, User user, Customer customer) {
        String orderSql = "INSERT INTO Orders (customer_id, user_id, total_amount) VALUES (?, ?, ?)";
        String itemSql = "INSERT INTO Order_Items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        String updateStockSql = "UPDATE Products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
        
        try {
            conn.setAutoCommit(false);
            
            // Create order
            PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            if (customer != null) {
                orderStmt.setInt(1, customer.getCustomerId());
            } else {
                orderStmt.setNull(1, Types.INTEGER);
            }
            orderStmt.setInt(2, user.getUserId());
            orderStmt.setDouble(3, cart.getTotal());
            orderStmt.executeUpdate();
            
            ResultSet rs = orderStmt.getGeneratedKeys();
            int orderId = 0;
            if (rs.next()) {
                orderId = rs.getInt(1);
            }
            
            // Add order items and update stock
            PreparedStatement itemStmt = conn.prepareStatement(itemSql);
            PreparedStatement stockStmt = conn.prepareStatement(updateStockSql);
            
            for (CartItem item : cart.getItems()) {
                // Add order item
                itemStmt.setInt(1, orderId);
                itemStmt.setInt(2, item.getProduct().getProductId());
                itemStmt.setInt(3, item.getQuantity());
                itemStmt.setDouble(4, item.getProduct().getPrice());
                itemStmt.executeUpdate();
                
                // Update stock
                stockStmt.setInt(1, item.getQuantity());
                stockStmt.setInt(2, item.getProduct().getProductId());
                stockStmt.executeUpdate();
                
                // Log inventory
                logInventory(item.getProduct().getProductId(), "OUT", item.getQuantity());
            }
            
            conn.commit();
            conn.setAutoCommit(true);
            
            return orderId;
            
        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                System.out.println("Rollback error: " + ex.getMessage());
            }
            System.out.println("Error: " + e.getMessage());
            return -1;
        }
    }

    private void logInventory(int productId, String type, int quantity) {
        String sql = "INSERT INTO Inventory (product_id, transaction_type, quantity) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.setString(2, type);
            stmt.setInt(3, quantity);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Inventory log error: " + e.getMessage());
        }
    }

    public void displayRecentOrders() {
        String sql = "SELECT o.order_id, IFNULL(c.name, 'Walk-in') as customer, " +
                     "u.username, o.total_amount, o.order_date " +
                     "FROM Orders o " +
                     "LEFT JOIN Customers c ON o.customer_id = c.customer_id " +
                     "JOIN Users u ON o.user_id = u.user_id " +
                     "ORDER BY o.order_date DESC LIMIT 20";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            System.out.println("\n=== Recent Orders ===");
            System.out.printf("%-8s %-20s %-15s %-12s %-20s%n", 
                "Order ID", "Customer", "Cashier", "Amount", "Date");
            System.out.println("--------------------------------------------------------------------------");
            
            while (rs.next()) {
                System.out.printf("%-8d %-20s %-15s Rs.%-9.2f %-20s%n",
                    rs.getInt("order_id"),
                    rs.getString("customer"),
                    rs.getString("username"),
                    rs.getDouble("total_amount"),
                    rs.getTimestamp("order_date")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.out.println("Error closing connection: " + e.getMessage());
        }
    }
    // Add these methods to DatabaseManager class:

public List<Product> getAllProducts() {
    List<Product> products = new ArrayList<>();
    String sql = "SELECT * FROM Products ORDER BY product_id";
    
    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            products.add(new Product(
                rs.getInt("product_id"),
                rs.getString("name"),
                rs.getInt("category_id"),
                rs.getDouble("price"),
                rs.getInt("stock_quantity")
            ));
        }
    } catch (SQLException e) {
        System.out.println("Error: " + e.getMessage());
    }
    
    return products;
}

public List<Category> getAllCategories() {
    List<Category> categories = new ArrayList<>();
    String sql = "SELECT * FROM Categories ORDER BY category_id";
    
    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            categories.add(new Category(
                rs.getInt("category_id"),
                rs.getString("name")
            ));
        }
    } catch (SQLException e) {
        System.out.println("Error: " + e.getMessage());
    }
    
    return categories;
}

public String getCategoryName(int categoryId) {
    String sql = "SELECT name FROM Categories WHERE category_id = ?";
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, categoryId);
        ResultSet rs = stmt.executeQuery();
        
        if (rs.next()) {
            return rs.getString("name");
        }
    } catch (SQLException e) {
        System.out.println("Error: " + e.getMessage());
    }
    
    return "Unknown";
}

public List<Customer> getAllCustomers() {
    List<Customer> customers = new ArrayList<>();
    String sql = "SELECT * FROM Customers ORDER BY customer_id";
    
    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        while (rs.next()) {
            customers.add(new Customer(
                rs.getInt("customer_id"),
                rs.getString("name"),
                rs.getString("phone")
            ));
        }
    } catch (SQLException e) {
        System.out.println("Error: " + e.getMessage());
    }
    
    return customers;
}

public List<Order> getRecentOrders(int limit) {
    List<Order> orders = new ArrayList<>();
    String sql = "SELECT o.order_id, IFNULL(c.name, 'Walk-in') as customer, " +
                 "u.username, o.total_amount, o.order_date " +
                 "FROM Orders o " +
                 "LEFT JOIN Customers c ON o.customer_id = c.customer_id " +
                 "JOIN Users u ON o.user_id = u.user_id " +
                 "ORDER BY o.order_date DESC LIMIT ?";
    
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, limit);
        ResultSet rs = stmt.executeQuery();
        
        while (rs.next()) {
            orders.add(new Order(
                rs.getInt("order_id"),
                rs.getString("customer"),
                rs.getString("username"),
                rs.getDouble("total_amount"),
                rs.getTimestamp("order_date").toString()
            ));
        }
    } catch (SQLException e) {
        System.out.println("Error: " + e.getMessage());
    }
    
    return orders;
}

public SalesStats getSalesStats() {
    String sql = "SELECT COUNT(*) as total_orders, " +
                 "SUM(total_amount) as total_sales, " +
                 "AVG(total_amount) as avg_order " +
                 "FROM Orders";
    
    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        if (rs.next()) {
            double totalSales = rs.getDouble("total_sales");
            int totalOrders = rs.getInt("total_orders");
            double avgOrder = rs.getDouble("avg_order");
            
            return new SalesStats(totalSales, totalOrders, avgOrder);
        }
    } catch (SQLException e) {
        System.out.println("Error: " + e.getMessage());
    }
    
    return new SalesStats(0, 0, 0);
}
}