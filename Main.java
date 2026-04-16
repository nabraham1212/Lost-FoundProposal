import java.util.ArrayList;
import java.util.Scanner;

/**
 * Main.java
 * Entry point for the Paradise Valley High School Lost & Found Matchmaker.
 * Console-based menu. All user data is saved to text files and reloaded
 * on startup so reports persist between sessions.
 *
 * PVHS-specific: contact info must be a @pvlearners.net email or a
 * numeric student ID (6+ digits).
 *
 * MENU OPTIONS:
 *   1. Report a lost item
 *   2. Report a found item
 *   3. Find matches for a lost item
 *   4. View all lost reports
 *   5. View all found reports
 *   6. Search lost items by type
 *   7. Exit
 */
public class Main {

    static Matchmaker db     = new Matchmaker();
    static Scanner    scanner = new Scanner(System.in);

    public static void main(String[] args) {
        printBanner();

        // Tell the user how many reports are already loaded
        int lostCount  = db.getLostItems().size();
        int foundCount = db.getFoundItems().size();
        if (lostCount > 0 || foundCount > 0) {
            System.out.println("  >> Loaded " + lostCount + " lost report(s) and " +
                               foundCount + " found report(s) from saved data.\n");
        } else {
            System.out.println("  >> No saved data found. Start by reporting a lost or found item.\n");
        }

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": reportLostItem();  break;
                case "2": reportFoundItem(); break;
                case "3": findMatches();     break;
                case "4": viewAllLost();     break;
                case "5": viewAllFound();    break;
                case "6": searchByType();    break;
                case "7":
                    System.out.println("\n  Goodbye! Reports have been saved.\n");
                    running = false;
                    break;
                default:
                    System.out.println("  Invalid choice. Enter a number 1-7.\n");
            }
        }
    }

    // ---------------------------------------------------------------
    //  OPTION 1 — Report a lost item
    // ---------------------------------------------------------------

    static void reportLostItem() {
        System.out.println("\n--- REPORT A LOST ITEM ---");

        String name     = promptRequired("Your full name");
        String contact  = promptContact();
        String type     = promptRequired("Item type (e.g. water bottle, jacket, headphones)");
        String color    = promptRequired("Color");
        String brand    = promptRequired("Brand (or type 'unknown')");
        String location = promptRequired("Where did you last have it? (e.g. gym, cafeteria)");
        String date     = promptDate("Date lost (MM/DD/YYYY)");

        LostItem item = db.addLostItem(name, contact, type, color, brand, location, date);
        System.out.println("\n  ✓ Lost item report saved!");
        System.out.println("  " + item + "\n");
    }

    // ---------------------------------------------------------------
    //  OPTION 2 — Report a found item
    // ---------------------------------------------------------------

    static void reportFoundItem() {
        System.out.println("\n--- REPORT A FOUND ITEM ---");

        String name     = promptRequired("Your full name");
        String heldAt   = promptRequired("Where is the item being held? (e.g. front office, room 101)");
        String type     = promptRequired("Item type");
        String color    = promptRequired("Color");
        String brand    = promptRequired("Brand (or type 'unknown')");
        String location = promptRequired("Where was it found?");
        String date     = promptDate("Date found (MM/DD/YYYY)");

        FoundItem item = db.addFoundItem(name, heldAt, type, color, brand, location, date);
        System.out.println("\n  ✓ Found item report saved!");
        System.out.println("  " + item + "\n");
    }

    // ---------------------------------------------------------------
    //  OPTION 3 — Find matches for a lost item
    // ---------------------------------------------------------------

    static void findMatches() {
        System.out.println("\n--- FIND MATCHES FOR A LOST ITEM ---");

        if (db.getLostItems().isEmpty()) {
            System.out.println("  No lost item reports on file. Add one first.\n");
            return;
        }

        System.out.println("  Current lost reports:");
        for (LostItem item : db.getLostItems()) {
            System.out.println("    #" + item.getReportID() + " — " +
                               item.getItemType() + " (" + item.getColor() + ")" +
                               " — reported by " + item.getReporterName());
        }

        System.out.print("\n  Enter the Lost Report ID to search matches for: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("  Invalid ID. Enter a number.\n");
            return;
        }

        LostItem target = db.findLostByID(id);
        if (target == null) {
            System.out.println("  No lost report found with ID #" + id + "\n");
            return;
        }

        System.out.println("\n  Searching matches for:");
        System.out.println("  " + target);

        ArrayList<MatchResult> matches = db.findMatches(target);

        if (matches.isEmpty()) {
            System.out.println("\n  No strong matches found yet. Check back as more found items are reported.\n");
            return;
        }

        System.out.println("\n  TOP MATCHES (" + matches.size() + " found):");
        System.out.println("  " + "-".repeat(65));

        int count = 0;
        for (MatchResult result : matches) {
            if (count >= 5) break; // show top 5
            System.out.println("\n  Match #" + (count + 1));
            System.out.println(result);
            count++;
        }
        System.out.println("\n  " + "-".repeat(65) + "\n");
    }

    // ---------------------------------------------------------------
    //  OPTION 4 — View all lost reports
    // ---------------------------------------------------------------

    static void viewAllLost() {
        int total = db.getLostItems().size();
        System.out.println("\n--- ALL LOST REPORTS (" + total + " total) ---");
        if (total == 0) {
            System.out.println("  No lost reports on file.\n");
            return;
        }
        db.printAllLost();
        System.out.println();
    }

    // ---------------------------------------------------------------
    //  OPTION 5 — View all found reports
    // ---------------------------------------------------------------

    static void viewAllFound() {
        int total = db.getFoundItems().size();
        System.out.println("\n--- ALL FOUND REPORTS (" + total + " total) ---");
        if (total == 0) {
            System.out.println("  No found reports on file.\n");
            return;
        }
        db.printAllFound();
        System.out.println();
    }

    // ---------------------------------------------------------------
    //  OPTION 6 — Search lost items by type
    // ---------------------------------------------------------------

    static void searchByType() {
        System.out.println("\n--- SEARCH LOST ITEMS BY TYPE ---");
        System.out.print("  Enter item type keyword (e.g. jacket, water bottle): ");
        String keyword = scanner.nextLine().trim();

        if (keyword.isEmpty()) {
            System.out.println("  Please enter a keyword to search.\n");
            return;
        }

        ArrayList<LostItem> results = db.searchLostByType(keyword);
        if (results.isEmpty()) {
            System.out.println("  No lost items found matching: \"" + keyword + "\"\n");
        } else {
            System.out.println("  Found " + results.size() + " result(s):");
            for (LostItem item : results) {
                System.out.println("  " + item);
            }
            System.out.println();
        }
    }

    // ---------------------------------------------------------------
    //  INPUT VALIDATION HELPERS
    // ---------------------------------------------------------------

    /**
     * Prompts with the given label and keeps asking until the user
     * enters something that is not blank.
     */
    static String promptRequired(String label) {
        String input = "";
        while (input.isEmpty()) {
            System.out.print("  " + label + ": ");
            input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                System.out.println("  This field is required. Please enter a value.");
            }
        }
        return input;
    }

    /**
     * Prompts for PVHS contact info.
     * Valid inputs:
     *   - ends with @pvlearners.net
     *   - is a numeric string of 6 or more digits (student ID)
     * Keeps asking until one of those is entered.
     */
    static String promptContact() {
        String input = "";
        boolean valid = false;

        while (!valid) {
            System.out.print("  Contact (PVHS email or student ID): ");
            input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("  This field is required. Please enter a value.");
            } else if (input.endsWith("@pvlearners.net")) {
                valid = true;
            } else if (input.matches("\\d{6,}")) {
                valid = true;
            } else {
                System.out.println("  Invalid contact. Enter a @pvlearners.net email or your student ID (6+ digits).");
            }
        }
        return input;
    }

    /**
     * Prompts for a date in MM/DD/YYYY format.
     * Keeps asking until the format looks correct (basic check).
     */
    static String promptDate(String label) {
        String input = "";
        boolean valid = false;

        while (!valid) {
            System.out.print("  " + label + ": ");
            input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("  This field is required. Please enter a date.");
            } else if (input.matches("\\d{2}/\\d{2}/\\d{4}")) {
                valid = true;
            } else {
                System.out.println("  Invalid format. Use MM/DD/YYYY (example: 04/15/2025).");
            }
        }
        return input;
    }

    // ---------------------------------------------------------------
    //  DISPLAY HELPERS
    // ---------------------------------------------------------------

    static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════╗");
        System.out.println("  ║     PARADISE VALLEY HIGH SCHOOL                     ║");
        System.out.println("  ║     LOST & FOUND MATCHMAKER                         ║");
        System.out.println("  ║     APCSA Final Project — 2025-2026                 ║");
        System.out.println("  ╚══════════════════════════════════════════════════════╝");
        System.out.println();
    }

    static void printMenu() {
        System.out.println("  MENU:");
        System.out.println("   1. Report a lost item");
        System.out.println("   2. Report a found item");
        System.out.println("   3. Find matches for a lost item");
        System.out.println("   4. View all lost reports");
        System.out.println("   5. View all found reports");
        System.out.println("   6. Search lost items by type");
        System.out.println("   7. Exit");
        System.out.print("\n  Enter choice (1-7): ");
    }
}
