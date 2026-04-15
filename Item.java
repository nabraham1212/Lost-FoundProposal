/**
 * Item.java
 * Base class representing a generic lost or found item.
 * Used as the parent for LostItem and FoundItem via inheritance.
 */
public class Item {

    // Instance variables (the 5 matching details)
    private String itemType;   // e.g., "water bottle", "jacket", "headphones"
    private String color;      // e.g., "blue", "black"
    private String brand;      // e.g., "Nike", "Apple", "unknown"
    private String location;   // e.g., "gym", "cafeteria", "classroom 204"
    private String date;       // Format: MM/DD/YYYY

    // Constructor
    public Item(String itemType, String color, String brand, String location, String date) {
        this.itemType = itemType.toLowerCase().trim();
        this.color = color.toLowerCase().trim();
        this.brand = brand.toLowerCase().trim();
        this.location = location.toLowerCase().trim();
        this.date = date.trim();
    }

    // Getters
    public String getItemType() { return itemType; }
    public String getColor()    { return color; }
    public String getBrand()    { return brand; }
    public String getLocation() { return location; }
    public String getDate()     { return date; }

    // Returns a formatted summary of the item details
    public String getSummary() {
        return "Type: " + itemType +
               " | Color: " + color +
               " | Brand: " + brand +
               " | Location: " + location +
               " | Date: " + date;
    }
}
