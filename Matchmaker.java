import java.util.ArrayList;

/**
 * Matchmaker.java
 * Core logic class. Stores all lost and found reports.
 * Handles matching: compares a LostItem against all FoundItems
 * and returns ranked results using a simple scoring system.
 *
 * SCORING (max 5 points per match):
 *   Item type  = 2 points  (most important)
 *   Color      = 1 point
 *   Brand      = 1 point
 *   Location   = 0.5 points (rounded in integer: counted if same)
 *   Date       = 0.5 points (counted if within 3 days)
 *
 * We use integer math, so:
 *   type match  = 2
 *   color match = 1
 *   brand match = 1
 *   location    = 1  (we weight it as full point for simplicity)
 *   date        = 1  (same or within 3 days)
 * Max possible = 6, but we display it clearly to the teacher.
 */
public class Matchmaker {

    private ArrayList<LostItem>  lostItems;
    private ArrayList<FoundItem> foundItems;

    private int lostIDCounter  = 1;
    private int foundIDCounter = 1;

    // Constructor
    public Matchmaker() {
        lostItems  = new ArrayList<LostItem>();
        foundItems = new ArrayList<FoundItem>();
    }

    // ---------------------------------------------------------------
    //  ADD REPORTS
    // ---------------------------------------------------------------

    public LostItem addLostItem(String reporterName, String contactInfo,
                                String itemType, String color, String brand,
                                String location, String date) {
        LostItem item = new LostItem(lostIDCounter++, reporterName, contactInfo,
                                     itemType, color, brand, location, date);
        lostItems.add(item);
        return item;
    }

    public FoundItem addFoundItem(String finderName, String heldAt,
                                  String itemType, String color, String brand,
                                  String location, String date) {
        FoundItem item = new FoundItem(foundIDCounter++, finderName, heldAt,
                                       itemType, color, brand, location, date);
        foundItems.add(item);
        return item;
    }

    // ---------------------------------------------------------------
    //  GETTERS / VIEW ALL
    // ---------------------------------------------------------------

    public ArrayList<LostItem>  getLostItems()  { return lostItems; }
    public ArrayList<FoundItem> getFoundItems() { return foundItems; }

    public void printAllLost() {
        if (lostItems.isEmpty()) {
            System.out.println("  No lost item reports on file.");
            return;
        }
        for (LostItem item : lostItems) {
            System.out.println("  " + item);
        }
    }

    public void printAllFound() {
        if (foundItems.isEmpty()) {
            System.out.println("  No found item reports on file.");
            return;
        }
        for (FoundItem item : foundItems) {
            System.out.println("  " + item);
        }
    }

    // ---------------------------------------------------------------
    //  CORE MATCHING LOGIC
    // ---------------------------------------------------------------

    /**
     * Takes a LostItem and compares it against every FoundItem.
     * Returns a sorted list of MatchResult objects (best match first).
     * Only returns results with score >= 1.
     */
    public ArrayList<MatchResult> findMatches(LostItem lost) {
        ArrayList<MatchResult> results = new ArrayList<MatchResult>();

        for (FoundItem found : foundItems) {
            int score = 0;
            StringBuilder breakdown = new StringBuilder();

            // 1. Item type (worth 2 points — most important field)
            if (lost.getItemType().equals(found.getItemType())) {
                score += 2;
                breakdown.append("type ");
            }

            // 2. Color (1 point)
            if (lost.getColor().equals(found.getColor())) {
                score += 1;
                breakdown.append("color ");
            }

            // 3. Brand (1 point)
            if (!lost.getBrand().equals("unknown") &&
                !found.getBrand().equals("unknown") &&
                lost.getBrand().equals(found.getBrand())) {
                score += 1;
                breakdown.append("brand ");
            }

            // 4. Location (1 point)
            if (lost.getLocation().equals(found.getLocation())) {
                score += 1;
                breakdown.append("location ");
            }

            // 5. Date — within 3 days (1 point)
            if (datesAreClose(lost.getDate(), found.getDate(), 3)) {
                score += 1;
                breakdown.append("date ");
            }

            // Only add if there is at least some match
            if (score >= 1) {
                String breakdownStr = breakdown.toString().trim();
                if (breakdownStr.isEmpty()) breakdownStr = "none";
                results.add(new MatchResult(found, score, breakdownStr));
            }
        }

        // Sort: highest score first (simple selection sort for APCSA)
        for (int i = 0; i < results.size() - 1; i++) {
            int maxIndex = i;
            for (int j = i + 1; j < results.size(); j++) {
                if (results.get(j).getScore() > results.get(maxIndex).getScore()) {
                    maxIndex = j;
                }
            }
            // Swap
            MatchResult temp = results.get(i);
            results.set(i, results.get(maxIndex));
            results.set(maxIndex, temp);
        }

        return results;
    }

    // ---------------------------------------------------------------
    //  SEARCH HELPERS
    // ---------------------------------------------------------------

    /** Find a lost item by its report ID. Returns null if not found. */
    public LostItem findLostByID(int id) {
        for (LostItem item : lostItems) {
            if (item.getReportID() == id) return item;
        }
        return null;
    }

    /** Search lost items by item type keyword */
    public ArrayList<LostItem> searchLostByType(String type) {
        ArrayList<LostItem> results = new ArrayList<LostItem>();
        for (LostItem item : lostItems) {
            if (item.getItemType().contains(type.toLowerCase().trim())) {
                results.add(item);
            }
        }
        return results;
    }

    // ---------------------------------------------------------------
    //  DATE COMPARISON HELPER
    // ---------------------------------------------------------------

    /**
     * Compares two dates in MM/DD/YYYY format.
     * Returns true if they are within 'dayRange' days of each other.
     * If date format is invalid, returns false (no match on date).
     */
    private boolean datesAreClose(String date1, String date2, int dayRange) {
        try {
            int days1 = dateToDays(date1);
            int days2 = dateToDays(date2);
            return Math.abs(days1 - days2) <= dayRange;
        } catch (Exception e) {
            return false; // bad format — skip date scoring
        }
    }

    /**
     * Converts MM/DD/YYYY to a rough "total days" integer for comparison.
     * Not a real calendar — just good enough for close-date detection.
     */
    private int dateToDays(String date) {
        String[] parts = date.split("/");
        int month = Integer.parseInt(parts[0]);
        int day   = Integer.parseInt(parts[1]);
        int year  = Integer.parseInt(parts[2]);
        return (year * 365) + (month * 30) + day;
    }
}
