// Order.java - New model class
public class Order {
    private int orderId;
    private String customerName;
    private String userName;
    private double totalAmount;
    private String orderDate;

    public Order(int orderId, String customerName, String userName, 
                 double totalAmount, String orderDate) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.userName = userName;
        this.totalAmount = totalAmount;
        this.orderDate = orderDate;
    }

    public int getOrderId() { return orderId; }
    public String getCustomerName() { return customerName; }
    public String getUserName() { return userName; }
    public double getTotalAmount() { return totalAmount; }
    public String getOrderDate() { return orderDate; }
}