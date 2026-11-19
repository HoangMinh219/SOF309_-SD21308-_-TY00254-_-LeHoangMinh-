package com.polycoffee.entity;

public class Drink {
    private Integer id;
    private Integer categoryId;
    private String name;
    private int price;
    private String image;
    private String description;
    private boolean active;

    public Drink() {}

    public Drink(Integer id, Integer categoryId, String name, int price, String image, String description, boolean active) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.price = price;
        this.image = image;
        this.description = description;
        this.active = active;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
