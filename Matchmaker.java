import java.util.ArrayList;
import java.io.*;

/**
 * Matchmaker.java
 * Core logic class. Stores all lost and found reports in ArrayLists.
 * Saves and loads data from text files so reports persist after restart.
 * Handles matching with a scored ranking system.
 *
 * SCORING (max 6 points):
 *   Item type  = 2 points  (most important)
 *   Color      = 1 point
 *   Brand      = 1 point
 *   Location   = 1 point
 *   Date       = 1 point   (within 3 days counts)
 *   TOTAL MAX  = 6 points
 *
 * Only matches with score >= 2 are shown (weak matches filtered out).
 *
 * PERSISTENT STORAGE:
 *   lost_items.txt  — one lost report per line, fields separated by "|"
 *   found_items.txt — one found report per line, fields separated by "|"
 */
public class Matchmaker {

    // File paths — saved in the same folder the program runs from
    private static final String LOST_FILE  = "lost_items.txt";
    private static final String FOUND_FILE = "found_items.txt";

    // Minimum score to show a match (filters out weak/irrelevant results)
    private static final int MIN_MATCH_SCORE = 2;

    private ArrayList<LostItem>  lostItems;
    private ArrayList<FoundItem> foundItems;

    private int lostIDCounter  = 1;
    private int foundIDCounter = 1;

    // Constructor — seeds sample data on first run, then loads everything from files
    public Matchmaker() {
        lostItems  = new ArrayList<LostItem>();
        foundItems = new ArrayList<FoundItem>();
        seedSampleDataIfFirstRun(); // writes sample data to files ONLY if files don't exist yet
        loadFromFiles();            // always load from files (includes sample + any user reports)
    }

    // ---------------------------------------------------------------
    //  ADD REPORTS (also saves to file immediately)
    // ---------------------------------------------------------------

    public LostItem addLostItem(String reporterName, String contactInfo,
                                String itemType, String color, String brand,
                                String location, String date) {
        LostItem item = new LostItem(lostIDCounter++, reporterName, contactInfo,
                                     itemType, color, brand, location, date);
        lostItems.add(item);
        saveLostItems(); // save to file right away
        return item;
    }

    public FoundItem addFoundItem(String finderName, String heldAt,
                                  String itemType, String color, String brand,
                                  String location, String date) {
        FoundItem item = new FoundItem(foundIDCounter++, finderName, heldAt,
                                       itemType, color, brand, location, date);
        foundItems.add(item);
        saveFoundItems(); // save to file right away
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
     * Compares a LostItem against all FoundItems.
     * Returns a sorted list of MatchResults (best first).
     * Only includes matches with score >= MIN_MATCH_SCORE (currently 2).
     * This filters out weak or unrelated matches.
     */
    public ArrayList<MatchResult> findMatches(LostItem lost) {
        ArrayList<MatchResult> results = new ArrayList<MatchResult>();

        for (FoundItem found : foundItems) {
            int score = 0;
            StringBuilder breakdown = new StringBuilder();

            // 1. Item type (2 points — most important)
            if (lost.getItemType().equals(found.getItemType())) {
                score += 2;
                breakdown.append("type ");
            }

            // 2. Color (1 point)
            if (lost.getColor().equals(found.getColor())) {
                score += 1;
                breakdown.append("color ");
            }

            // 3. Brand (1 point) — skip if either side is "unknown"
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

            // 5. Date within 3 days (1 point)
            if (datesAreClose(lost.getDate(), found.getDate(), 3)) {
                score += 1;
                breakdown.append("date ");
            }

            // Only include if score meets the minimum threshold
            if (score >= MIN_MATCH_SCORE) {
                String breakdownStr = breakdown.toString().trim();
                results.add(new MatchResult(found, score, breakdownStr));
            }
        }

        // Sort highest score first (selection sort — standard APCSA)
        for (int i = 0; i < results.size() - 1; i++) {
            int maxIndex = i;
            for (int j = i + 1; j < results.size(); j++) {
                if (results.get(j).getScore() > results.get(maxIndex).getScore()) {
                    maxIndex = j;
                }
            }
            MatchResult temp = results.get(i);
            results.set(i, results.get(maxIndex));
            results.set(maxIndex, temp);
        }

        return results;
    }

    // ---------------------------------------------------------------
    //  SEARCH HELPERS
    // ---------------------------------------------------------------

    /** Find a lost item by report ID. Returns null if not found. */
    public LostItem findLostByID(int id) {
        for (LostItem item : lostItems) {
            if (item.getReportID() == id) return item;
        }
        return null;
    }

    /**
     * Search lost items by item type keyword.
     * Returns empty list (not all items) if keyword is blank.
     */
    public ArrayList<LostItem> searchLostByType(String type) {
        ArrayList<LostItem> results = new ArrayList<LostItem>();
        String keyword = type.toLowerCase().trim();

        // Don't match everything when input is blank
        if (keyword.isEmpty()) return results;

        for (LostItem item : lostItems) {
            if (item.getItemType().contains(keyword)) {
                results.add(item);
            }
        }
        return results;
    }

    // ---------------------------------------------------------------
    //  FIRST-RUN SEED
    // ---------------------------------------------------------------

    /**
     * Called once, at startup, before loadFromFiles().
     * If lost_items.txt or found_items.txt do not exist yet,
     * this writes the built-in sample reports to those files.
     * On every subsequent run the files already exist, so this does nothing.
     * This guarantees sample data appears exactly once — never duplicated.
     */
    private void seedSampleDataIfFirstRun() {
        boolean lostFileMissing  = !new File(LOST_FILE).exists();
        boolean foundFileMissing = !new File(FOUND_FILE).exists();

        // Only seed if at least one file is missing
        if (!lostFileMissing && !foundFileMissing) return;

        // Temporary counters just for writing the seed file
        int tempLostID  = 1;
        int tempFoundID = 1;

        // --- SEED: LOST ITEMS ---
        if (lostFileMissing) {
            try {
                PrintWriter w = new PrintWriter(new FileWriter(LOST_FILE));
                w.println(tempLostID++ + "|Marcus T.|marcus@pvlearners.net|water bottle|blue|hydro flask|gym|04/07/2026");
                w.println(tempLostID++ + "|Sofia R.|sofia@pvlearners.net|jacket|black|nike|cafeteria|04/08/2026");
                w.println(tempLostID++ + "|Jaylen K.|jaylen@pvlearners.net|headphones|white|apple|library|04/06/2026");
                w.println(tempLostID++ + "|Priya M.|priya@pvlearners.net|lunch box|red|unknown|classroom 204|04/09/2026");
                w.println(tempLostID++ + "|Carlos V.|carlos@pvlearners.net|calculator|black|ti|math hallway|04/05/2026");
                w.println(tempLostID++ + "|Aisha W.|aisha@pvlearners.net|jacket|gray|adidas|gym|04/10/2026");
                w.println(tempLostID++ + "|Devon P.|devon@pvlearners.net|water bottle|green|nalgene|cafeteria|04/07/2026");
                w.println(tempLostID++ + "|Luna S.|luna@pvlearners.net|earbuds|white|samsung|hallway|04/08/2026");
                w.println(tempLostID++ + "|Tyler H.|tyler@pvlearners.net|backpack|black|jansport|gym|04/06/2026");
                w.println(tempLostID++ + "|Nadia F.|nadia@pvlearners.net|water bottle|blue|hydro flask|gym|04/08/2026");
                w.println(tempLostID++ + "|Kwame B.|kwame@pvlearners.net|headphones|black|sony|library|04/09/2026");
                w.println(tempLostID++ + "|Isabelle G.|isabelle@pvlearners.net|jacket|black|north face|cafeteria|04/10/2026");
                w.println(tempLostID++ + "|Omar A.|omar@pvlearners.net|phone|black|apple|classroom 101|04/07/2026");
                w.println(tempLostID++ + "|Zoe C.|zoe@pvlearners.net|lunch box|purple|unknown|cafeteria|04/05/2026");
                w.println(tempLostID++ + "|Ethan L.|ethan@pvlearners.net|calculator|black|casio|science room|04/08/2026");
                w.close();
            } catch (IOException e) {
                System.out.println("  Warning: Could not write sample lost items. (" + e.getMessage() + ")");
            }
        }

        // --- SEED: FOUND ITEMS ---
        if (foundFileMissing) {
            try {
                PrintWriter w = new PrintWriter(new FileWriter(FOUND_FILE));
                w.println(tempFoundID++ + "|Coach Rivera|gym office|water bottle|blue|hydro flask|gym|04/07/2026");
                w.println(tempFoundID++ + "|Ms. Chen|front office|jacket|black|nike|cafeteria|04/08/2026");
                w.println(tempFoundID++ + "|Mr. Davis|library desk|headphones|white|apple|library|04/06/2026");
                w.println(tempFoundID++ + "|Lunch Staff|cafeteria|lunch box|red|unknown|cafeteria|04/09/2026");
                w.println(tempFoundID++ + "|Mr. Kim|room 204|calculator|black|ti|math hallway|04/05/2026");
                w.println(tempFoundID++ + "|Janitor|gym lost box|jacket|gray|adidas|gym|04/10/2026");
                w.println(tempFoundID++ + "|Coach Rivera|gym office|water bottle|green|unknown|gym|04/07/2026");
                w.println(tempFoundID++ + "|Ms. Park|front office|earbuds|white|samsung|hallway|04/08/2026");
                w.println(tempFoundID++ + "|Janitor|gym lost box|backpack|black|jansport|gym|04/06/2026");
                w.println(tempFoundID++ + "|Coach Rivera|gym office|water bottle|blue|hydro flask|gym|04/09/2026");
                w.println(tempFoundID++ + "|Mr. Davis|library desk|headphones|black|sony|library|04/09/2026");
                w.println(tempFoundID++ + "|Ms. Chen|front office|jacket|black|north face|cafeteria|04/10/2026");
                w.println(tempFoundID++ + "|Student Aide|room 101|phone|black|apple|classroom 101|04/07/2026");
                w.println(tempFoundID++ + "|Lunch Staff|cafeteria|lunch box|purple|unknown|cafeteria|04/05/2026");
                w.println(tempFoundID++ + "|Ms. Park|front office|calculator|black|casio|science room|04/08/2026");
                w.close();
            } catch (IOException e) {
                System.out.println("  Warning: Could not write sample found items. (" + e.getMessage() + ")");
            }
        }
    }

    // ---------------------------------------------------------------
    //  PERSISTENT STORAGE — SAVE
    // ---------------------------------------------------------------

    /**
     * Saves all lost items to lost_items.txt.
     * Each line = one report, fields separated by "|".
     * Format: id|reporterName|contactInfo|itemType|color|brand|location|date
     */
    private void saveLostItems() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(LOST_FILE));
            for (LostItem item : lostItems) {
                writer.println(
                    item.getReportID()     + "|" +
                    item.getReporterName() + "|" +
                    item.getContactInfo()  + "|" +
                    item.getItemType()     + "|" +
                    item.getColor()        + "|" +
                    item.getBrand()        + "|" +
                    item.getLocation()     + "|" +
                    item.getDate()
                );
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("  Warning: Could not save lost items. (" + e.getMessage() + ")");
        }
    }

    /**
     * Saves all found items to found_items.txt.
     * Format: id|finderName|heldAt|itemType|color|brand|location|date
     */
    private void saveFoundItems() {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(FOUND_FILE));
            for (FoundItem item : foundItems) {
                writer.println(
                    item.getReportID()   + "|" +
                    item.getFinderName() + "|" +
                    item.getHeldAt()     + "|" +
                    item.getItemType()   + "|" +
                    item.getColor()      + "|" +
                    item.getBrand()      + "|" +
                    item.getLocation()   + "|" +
                    item.getDate()
                );
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("  Warning: Could not save found items. (" + e.getMessage() + ")");
        }
    }

    // ---------------------------------------------------------------
    //  PERSISTENT STORAGE — LOAD
    // ---------------------------------------------------------------

    /**
     * Called once at startup. Reads lost_items.txt and found_items.txt
     * and rebuilds the ArrayLists. If files don't exist yet, that's fine —
     * it just means no data has been saved yet.
     */
    private void loadFromFiles() {
        loadLostItems();
        loadFoundItems();
    }

    private void loadLostItems() {
        File file = new File(LOST_FILE);
        if (!file.exists()) return; // no file yet, nothing to load

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length < 8) continue; // skip malformed lines

                int    id           = Integer.parseInt(parts[0]);
                String reporterName = parts[1];
                String contactInfo  = parts[2];
                String itemType     = parts[3];
                String color        = parts[4];
                String brand        = parts[5];
                String location     = parts[6];
                String date         = parts[7];

                // Reconstruct the LostItem with its original ID
                LostItem item = new LostItem(id, reporterName, contactInfo,
                                             itemType, color, brand, location, date);
                lostItems.add(item);

                // Keep the ID counter ahead of all loaded IDs
                if (id >= lostIDCounter) lostIDCounter = id + 1;
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("  Warning: Could not load lost items. (" + e.getMessage() + ")");
        }
    }

    private void loadFoundItems() {
        File file = new File(FOUND_FILE);
        if (!file.exists()) return;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\|");
                if (parts.length < 8) continue;

                int    id         = Integer.parseInt(parts[0]);
                String finderName = parts[1];
                String heldAt     = parts[2];
                String itemType   = parts[3];
                String color      = parts[4];
                String brand      = parts[5];
                String location   = parts[6];
                String date       = parts[7];

                FoundItem item = new FoundItem(id, finderName, heldAt,
                                               itemType, color, brand, location, date);
                foundItems.add(item);

                if (id >= foundIDCounter) foundIDCounter = id + 1;
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("  Warning: Could not load found items. (" + e.getMessage() + ")");
        }
    }

    // ---------------------------------------------------------------
    //  DATE COMPARISON HELPER
    // ---------------------------------------------------------------

    /**
     * Returns true if two MM/DD/YYYY dates are within dayRange days.
     * Uses a simple integer conversion — not a real calendar, but
     * accurate enough for close-date matching.
     */
    private boolean datesAreClose(String date1, String date2, int dayRange) {
        try {
            int days1 = dateToDays(date1);
            int days2 = dateToDays(date2);
            return Math.abs(days1 - days2) <= dayRange;
        } catch (Exception e) {
            return false;
        }
    }

    private int dateToDays(String date) {
        String[] parts = date.split("/");
        int month = Integer.parseInt(parts[0]);
        int day   = Integer.parseInt(parts[1]);
        int year  = Integer.parseInt(parts[2]);
        return (year * 365) + (month * 30) + day;
    }
}
