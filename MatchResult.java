/**
 * MatchResult.java
 * Stores one potential match between a LostItem and a FoundItem.
 * Holds the found item reference and the match score.
 * Used by Matchmaker to rank results.
 *
 * MAX SCORE = 6:
 *   type     = 2 points
 *   color    = 1 point
 *   brand    = 1 point
 *   location = 1 point
 *   date     = 1 point
 *   TOTAL    = 6 points max
 */
public class MatchResult {

    public static final int MAX_SCORE = 6; // real maximum, not 5

    private FoundItem foundItem;
    private int score;
    private String scoreBreakdown;

    // Constructor
    public MatchResult(FoundItem foundItem, int score, String scoreBreakdown) {
        this.foundItem      = foundItem;
        this.score          = score;
        this.scoreBreakdown = scoreBreakdown;
    }

    // Getters
    public FoundItem getFoundItem()      { return foundItem; }
    public int       getScore()          { return score; }
    public String    getScoreBreakdown() { return scoreBreakdown; }

    @Override
    public String toString() {
        return "  Match Score: " + score + "/" + MAX_SCORE + "\n" +
               "  " + foundItem.toString() + "\n" +
               "  Matched on: " + scoreBreakdown;
    }
}
