package com.example.midnight_chevves.Model;


public class Products {
    private String pname, description, price, image, category, pid, rdate, time;
    private int slots;

    public Products() {
    }

    public Products(String pname, String description, String price, String image, String category, String pid, String rdate, String time, int slots) {
        this.pname = pname;
        this.description = description;
        this.price = price;
        this.image = image;
        this.category = category;
        this.pid = pid;
        this.rdate = rdate;
        this.time = time;
        this.slots = slots;
    }

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getRDate() {
        return rdate;
    }

    public void setRDate(String rdate) {
        this.rdate = rdate;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slot) {
        this.slots = slot;
    }

}
