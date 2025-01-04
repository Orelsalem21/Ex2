package assignments.ex2;

/**
 * CellEntry represents an entry in the spreadsheet with a position (x, y) and validity status.
 */
public class CellEntry {
    private final int x;      // X coordinate of the cell
    private final int y;      // Y coordinate of the cell
    private final boolean valid; // Validity of the cell

    /**
     * Constructor for CellEntry.
     * @param x X coordinate of the cell.
     * @param y Y coordinate of the cell.
     * @param valid Validity of the cell.
     */
    public CellEntry(int x, int y, boolean valid) {
        this.x = x;
        this.y = y;
        this.valid = valid;
    }

    /**
     * Checks if the cell entry is valid.
     * @return True if valid, false otherwise.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Gets the X coordinate of the cell entry.
     * @return X coordinate as an integer.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the Y coordinate of the cell entry.
     * @return Y coordinate as an integer.
     */
    public int getY() {
        return y;
    }
}
