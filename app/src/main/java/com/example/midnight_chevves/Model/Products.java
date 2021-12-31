package com.example.midnight_chevves.Model;


public class Products {
    private String Name, Description, Price, Category, ID, RDate;
    private int Slots;

    public Products() {
    }

    public Products(String Name, String Description, String Price, String Category, String ID, String RDate, int Slots) {
        this.Name = Name;
        this.Description = Description;
        this.Price = Price;
        this.Category = Category;
        this.ID = ID;
        this.RDate = RDate;
        this.Slots = Slots;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        this.Description = Description;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        this.Price = Price;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        this.Category = Category;
    }

    public String getID() {
        return ID;
    }

    public void setID(String pid) {
        this.ID = pid;
    }

    public String getRDate() {
        return RDate;
    }

    public void setRDate(String RDate) {
        this.RDate = RDate;
    }

    public int getSlots() {
        return Slots;
    }

    public void setSlots(int slot) {
        this.Slots = Slots;
    }

}
