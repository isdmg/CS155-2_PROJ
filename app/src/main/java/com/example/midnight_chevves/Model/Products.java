package com.example.midnight_chevves.Model;


public class Products {
    private String Name, Description, Alias, Category, ID, RDate;
    private int Price, Slots;

    public Products() {
    }

    public Products(String Name, String Description, String Alias, int Price, String Category, String ID, String RDate, int Slots) {
        this.Name = Name;
        this.Description = Description;
        this.Alias = Alias;
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

    public String getAlias() {
        return Alias;
    }

    public void setAlias(String alias) {
        Alias = alias;
    }

    public int getPrice() {
        return Price;
    }

    public void setPrice(int price) {
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
