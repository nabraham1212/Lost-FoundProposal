/**
 * FoundItem.java
 * Represents an item that has been found and turned in.
 * Extends Item (inherits all 5 detail fields).
 * Adds: who found it and where it is being held.
 */
public class FoundItem extends Item {

    private String finderName;
    private String heldAt;    // e.g., "front office", "room 101"
    private int reportID;

    // Constructor
    public FoundItem(int reportID, String finderName, String heldAt,
                     String itemType, String color, String brand,
                     String location, String date) {
        super(itemType, color, brand, location, date);
        this.reportID   = reportID;
        this.finderName = finderName.trim();
        this.heldAt     = heldAt.trim();
    }

    // Getters
    public String getFinderName() { return finderName; }
    public String getHeldAt()     { return heldAt; }
    public int    getReportID()   { return reportID; }

    // Full display string for this found item report
    @Override
    public String toString() {
        return "[FOUND #" + reportID + "] " + getSummary() +
               " | Found by: " + finderName +
               " | Currently at: " + heldAt;
    }
}
