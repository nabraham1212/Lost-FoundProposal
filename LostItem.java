/**
 * LostItem.java
 * Represents an item that a student has lost.
 * Extends Item (inherits all 5 detail fields).
 * Adds: reporter name and contact info.
 */
public class LostItem extends Item {

    private String reporterName;
    private String contactInfo;  // e.g., email or student ID
    private int reportID;

    // Constructor
    public LostItem(int reportID, String reporterName, String contactInfo,
                    String itemType, String color, String brand,
                    String location, String date) {
        super(itemType, color, brand, location, date);
        this.reportID    = reportID;
        this.reporterName = reporterName.trim();
        this.contactInfo  = contactInfo.trim();
    }

    // Getters
    public String getReporterName() { return reporterName; }
    public String getContactInfo()  { return contactInfo; }
    public int    getReportID()     { return reportID; }

    // Full display string for this lost item report
    @Override
    public String toString() {
        return "[LOST #" + reportID + "] " + getSummary() +
               " | Reported by: " + reporterName +
               " | Contact: " + contactInfo;
    }
}
