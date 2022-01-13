package com.example.midnight_chevves.Model;



public class Orders {
    private String OrderId, OrderStatus, OrderDate;

    public Orders() {
    }

    public Orders(String OrderId, String OrderStatus, String OrderDate) {
        this.OrderId = OrderId;
        this.OrderStatus = OrderStatus;
        this.OrderDate = OrderDate;
    }

    public String getOrderId() {
        return OrderId;
    }

    public void setOrderId(String orderId) {
        OrderId = orderId;
    }

    public String getOrderStatus() {
        return OrderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        OrderStatus = orderStatus;
    }

    public String getOrderDate() {
        return OrderDate;
    }

    public void setOrderDate(String orderDate) {
        OrderDate = orderDate;
    }
}
