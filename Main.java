import java.util.ArrayList;
import java.util.Scanner;

/**
 * Main.java
 * Runner / entry point for the Lost & Found Matchmaker program.
 * Provides a console menu for users to:
 *   1. Report a lost item
 *   2. Report a found item
 *   3. Find matches for a lost item
 *   4. View all lost reports
 *   5. View all found reports
 *   6. Search lost items by type
 *   7. Load sample data (for testing)
 *   8. Exit
 */
public class Main {

    static Matchmaker system = new Matchmaker();
    static Scanner scanner   = new Scanner(System.in);

    public static void main(String[] args) {

        printBanner();

        // Auto-load sample data so there's something to test
        loadSampleData();
        System.out.println("  >> Sample data loaded. You can start testing right away.\n");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": reportLostItem();   break;
                case "2": reportFoundItem();  break;
                case "3": findMatches();       break;
                case "4": viewAllLost();       break;
                case "5": viewAllFound();      break;
                case "6": searchByType();      break;
                case "7":
                    loadSampleData();
                    System.out.println("  >> More sample data loaded.\n");
                    break;
                case "8":
                    System.out.println("\n  Goodbye! Come pick up your stuff.\n");
                    running = false;
                    break;
                default:
                    System.out.println("  Invalid option. Enter a number 1-8.\n");
            }
        }
    }

    // ---------------------------------------------------------------
    //  MENU OPTIONS
    // ---------------------------------------------------------------

    static void reportLostItem() {
        System.out.println("\n--- REPORT A LOST ITEM ---");
        System.out.print("Your name: ");
        String name = scanner.nextLine();

        System.out.print("Your contact (email or student ID): ");
        String contact = scanner.nextLine();

        System.out.print("Item type (e.g. water bottle, jacket, headphones): ");
        String type = scanner.nextLine();

        System.out.print("Color: ");
        String color = scanner.nextLine();

        System.out.print("Brand (or 'unknown'): ");
        String brand = scanner.nextLine();

        System.out.print("Where did you last have it? (e.g. gym, cafeteria): ");
        String location = scanner.nextLine();

        System.out.print("Date lost (MM/DD/YYYY): ");
        String date = scanner.nextLine();

        LostItem item = system.addLostItem(name, contact, type, color, brand, location, date);
        System.out.println("\n  ✓ Lost item report submitted!");
        System.out.println("  " + item + "\n");
    }

    static void reportFoundItem() {
        System.out.println("\n--- REPORT A FOUND ITEM ---");
        System.out.print("Your name: ");
        String name = scanner.nextLine();

        System.out.print("Where is the item being held? (e.g. front office, room 101): ");
        String heldAt = scanner.nextLine();

        System.out.print("Item type: ");
        String type = scanner.nextLine();

        System.out.print("Color: ");
        String color = scanner.nextLine();

        System.out.print("Brand (or 'unknown'): ");
        String brand = scanner.nextLine();

        System.out.print("Where was it found? ");
        String location = scanner.nextLine();

        System.out.print("Date found (MM/DD/YYYY): ");
        String date = scanner.nextLine();

        FoundItem item = system.addFoundItem(name, heldAt, type, color, brand, location, date);
        System.out.println("\n  ✓ Found item report submitted!");
        System.out.println("  " + item + "\n");
    }

    static void findMatches() {
        System.out.println("\n--- FIND MATCHES FOR A LOST ITEM ---");

        if (system.getLostItems().isEmpty()) {
            System.out.println("  No lost items on file. Add one first.\n");
            return;
        }

        System.out.println("  Current lost item report IDs:");
        for (LostItem item : system.getLostItems()) {
            System.out.println("    #" + item.getReportID() + " — " + item.getItemType() +
                               " (" + item.getColor() + ") reported by " + item.getReporterName());
        }

        System.out.print("\n  Enter the Lost Item Report ID to match: ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("  Invalid ID. Please enter a number.\n");
            return;
        }

        LostItem target = system.findLostByID(id);
        if (target == null) {
            System.out.println("  No lost item found with ID #" + id + "\n");
            return;
        }

        System.out.println("\n  Looking for matches for:");
        System.out.println("  " + target);

        ArrayList<MatchResult> matches = system.findMatches(target);

        if (matches.isEmpty()) {
            System.out.println("\n  No matches found yet. Check back later.\n");
            return;
        }

        System.out.println("\n  TOP MATCHES (" + matches.size() + " found):");
        System.out.println("  " + "-".repeat(60));

        int count = 0;
        for (MatchResult result : matches) {
            if (count >= 5) break; // show top 5 max
            System.out.println("\n  Match #" + (count + 1));
            System.out.println(result);
            count++;
        }
        System.out.println("\n  " + "-".repeat(60) + "\n");
    }

    static void viewAllLost() {
        System.out.println("\n--- ALL LOST ITEM REPORTS (" +
                           system.getLostItems().size() + " total) ---");
        system.printAllLost();
        System.out.println();
    }

    static void viewAllFound() {
        System.out.println("\n--- ALL FOUND ITEM REPORTS (" +
                           system.getFoundItems().size() + " total) ---");
        system.printAllFound();
        System.out.println();
    }

    static void searchByType() {
        System.out.println("\n--- SEARCH LOST ITEMS BY TYPE ---");
        System.out.print("  Enter item type keyword (e.g. jacket, bottle): ");
        String keyword = scanner.nextLine();

        ArrayList<LostItem> results = system.searchLostByType(keyword);
        if (results.isEmpty()) {
            System.out.println("  No lost items found matching: " + keyword + "\n");
        } else {
            System.out.println("  Found " + results.size() + " result(s):");
            for (LostItem item : results) {
                System.out.println("  " + item);
            }
            System.out.println();
        }
    }

    // ---------------------------------------------------------------
    //  SAMPLE DATA — 30+ reports for testing
    // ---------------------------------------------------------------

    static void loadSampleData() {
        // --- LOST ITEMS ---
        system.addLostItem("Marcus T.",  "marcus@school.edu",  "water bottle", "blue",   "Hydro Flask", "gym",          "04/07/2025");
        system.addLostItem("Sofia R.",   "sofia@school.edu",   "jacket",       "black",  "Nike",        "cafeteria",    "04/08/2025");
        system.addLostItem("Jaylen K.",  "jaylen@school.edu",  "headphones",   "white",  "Apple",       "library",      "04/06/2025");
        system.addLostItem("Priya M.",   "priya@school.edu",   "lunch box",    "red",    "unknown",     "classroom 204","04/09/2025");
        system.addLostItem("Carlos V.",  "carlos@school.edu",  "calculator",   "black",  "TI",          "math hallway", "04/05/2025");
        system.addLostItem("Aisha W.",   "aisha@school.edu",   "jacket",       "gray",   "Adidas",      "gym",          "04/10/2025");
        system.addLostItem("Devon P.",   "devon@school.edu",   "water bottle", "green",  "Nalgene",     "cafeteria",    "04/07/2025");
        system.addLostItem("Luna S.",    "luna@school.edu",    "earbuds",      "white",  "Samsung",     "hallway",      "04/08/2025");
        system.addLostItem("Tyler H.",   "tyler@school.edu",   "backpack",     "black",  "JanSport",    "gym",          "04/06/2025");
        system.addLostItem("Nadia F.",   "nadia@school.edu",   "water bottle", "blue",   "Hydro Flask", "gym",          "04/08/2025");
        system.addLostItem("Kwame B.",   "kwame@school.edu",   "headphones",   "black",  "Sony",        "library",      "04/09/2025");
        system.addLostItem("Isabelle G.","isabelle@school.edu","jacket",       "black",  "North Face",  "cafeteria",    "04/10/2025");
        system.addLostItem("Omar A.",    "omar@school.edu",    "phone",        "black",  "Apple",       "classroom 101","04/07/2025");
        system.addLostItem("Zoe C.",     "zoe@school.edu",     "lunch box",    "purple", "unknown",     "cafeteria",    "04/05/2025");
        system.addLostItem("Ethan L.",   "ethan@school.edu",   "calculator",   "black",  "Casio",       "science room", "04/08/2025");

        // --- FOUND ITEMS ---
        system.addFoundItem("Coach Rivera", "gym office",   "water bottle", "blue",   "Hydro Flask", "gym",          "04/07/2025");
        system.addFoundItem("Ms. Chen",     "front office", "jacket",       "black",  "Nike",        "cafeteria",    "04/08/2025");
        system.addFoundItem("Mr. Davis",    "library desk", "headphones",   "white",  "Apple",       "library",      "04/06/2025");
        system.addFoundItem("Lunch staff",  "cafeteria",    "lunch box",    "red",    "unknown",     "cafeteria",    "04/09/2025");
        system.addFoundItem("Mr. Kim",      "room 204",     "calculator",   "black",  "TI",          "math hallway", "04/05/2025");
        system.addFoundItem("Janitor",      "gym lost box", "jacket",       "gray",   "Adidas",      "gym",          "04/10/2025");
        system.addFoundItem("Coach Rivera", "gym office",   "water bottle", "green",  "unknown",     "gym",          "04/07/2025");
        system.addFoundItem("Ms. Park",     "front office", "earbuds",      "white",  "Samsung",     "hallway",      "04/08/2025");
        system.addFoundItem("Janitor",      "gym lost box", "backpack",     "black",  "JanSport",    "gym",          "04/06/2025");
        system.addFoundItem("Coach Rivera", "gym office",   "water bottle", "blue",   "Hydro Flask", "gym",          "04/09/2025");
        system.addFoundItem("Mr. Davis",    "library desk", "headphones",   "black",  "Sony",        "library",      "04/09/2025");
        system.addFoundItem("Ms. Chen",     "front office", "jacket",       "black",  "North Face",  "cafeteria",    "04/10/2025");
        system.addFoundItem("Student aide", "room 101",     "phone",        "black",  "Apple",       "classroom 101","04/07/2025");
        system.addFoundItem("Lunch staff",  "cafeteria",    "lunch box",    "purple", "unknown",     "cafeteria",    "04/05/2025");
        system.addFoundItem("Ms. Park",     "front office", "calculator",   "black",  "Casio",       "science room", "04/08/2025");
    }

    // ---------------------------------------------------------------
    //  DISPLAY HELPERS
    // ---------------------------------------------------------------

    static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════╗");
        System.out.println("  ║        SCHOOL LOST & FOUND MATCHMAKER           ║");
        System.out.println("  ║         APCSA Final Project — 2025-2026         ║");
        System.out.println("  ╚══════════════════════════════════════════════════╝");
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
        System.out.println("   7. Load more sample data");
        System.out.println("   8. Exit");
        System.out.print("\n  Enter choice (1-8): ");
    }
}
