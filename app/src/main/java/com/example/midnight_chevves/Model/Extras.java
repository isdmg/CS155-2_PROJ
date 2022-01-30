package com.example.midnight_chevves.Model;

public class Extras {
    private String ExtraID, ProductID, ProductName, parentRef;
    private int ProductPrice, Quantity;

    public Extras() {
    }

    public Extras(String ExtraID, String ProductID, String ProductName, int ProductPrice, int Quantity, String parentRef) {
        this.ExtraID = ExtraID;
        this.ProductID = ProductID;
        this.ProductName = ProductName;
        this.ProductPrice = ProductPrice;
        this.Quantity = Quantity;
        this.parentRef = parentRef;
    }

    public String getExtraID() {
        return ExtraID;
    }

    public void setExtraID(String extraID) {
        ExtraID = extraID;
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

    public String getParentRef() {
        return parentRef;
    }

    public void setParentRef(String parentRef) {
        this.parentRef = parentRef;
    }

    public int getProductPrice() {
        return ProductPrice;
    }

    public void setProductPrice(int productPrice) {
        ProductPrice = productPrice;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int quantity) {
        Quantity = quantity;
    }
}
