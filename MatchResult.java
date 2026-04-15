/**
 * MatchResult.java
 * Stores one potential match between a LostItem and a FoundItem.
 * Holds the found item reference and the match score.
 * Used by Matchmaker to rank results.
 */
public class MatchResult {

    private FoundItem foundItem;
    private int score;           // Higher = better match (max 5)
    private String scoreBreakdown; // Human-readable explanation

    // Constructor
    public MatchResult(FoundItem foundItem, int score, String scoreBreakdown) {
        this.foundItem      = foundItem;
        this.score          = score;
        this.scoreBreakdown = scoreBreakdown;
    }

    // Getters
    public FoundItem getFoundItem()     { return foundItem; }
    public int       getScore()         { return score; }
    public String    getScoreBreakdown(){ return scoreBreakdown; }

    // Display this result clearly
    @Override
    public String toString() {
        return "  Match Score: " + score + "/5\n" +
               "  " + foundItem.toString() + "\n" +
               "  Matched on: " + scoreBreakdown;
    }
}
