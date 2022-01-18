package com.example.midnight_chevves.Model;



public class Cart {
    private String ListID, ProductID, ProductName, PurchaseDate;
    private int ProductPrice, Quantity;

    public Cart() {
    }

    public Cart(String ListID, String ProductID, String ProductName, int ProductPrice, String PurchaseDate, int Quantity) {
        this.ListID = ListID;
        this.ProductID = ProductID;
        this.ProductName = ProductName;
        this.ProductPrice = ProductPrice;
        this.PurchaseDate = PurchaseDate;
        this.Quantity = Quantity;
    }

    public String getListID() {
        return ListID;
    }

    public void setListID(String listID) {
        ListID = listID;
    }

    public String getProductID() {
        return ProductID;
    }

    public void setProductID(String productID) {
        ProductID = productID;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public int getProductPrice() {
        return ProductPrice;
    }

    public void setProductPrice(int productPrice) {
        ProductPrice = productPrice;
    }

    public String getPurchaseDate() {
        return PurchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        PurchaseDate = purchaseDate;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int quantity) {
        Quantity = quantity;
    }
}
