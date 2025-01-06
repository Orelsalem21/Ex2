package assignments;

public class CellEntry implements Index2D {
    private int x; // X coordinate (column index)
    private int y; // Y coordinate (row index)

    /**
     * Constructor to initialize the cell's coordinates.
     *
     * @param x The x-coordinate (column index).
     * @param y The y-coordinate (row index).
     */
    public CellEntry(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Checks if the coordinates are valid (non-negative).
     *
     * @return true if both x and y are non-negative, otherwise false.
     */
    @Override
    public boolean isValid() {
        return x >= 0 && y >= 0;
    }

    /**
     * Gets the x-coordinate of this cell.
     *
     * @return the x-coordinate (column index).
     */
    @Override
    public int getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of this cell.
     *
     * @return the y-coordinate (row index).
     */
    @Override
    public int getY() {
        return y;
    }

    /**
     * Returns the string representation of the cell's position in the format "A1", "B2", etc.
     *
     * @return a string representing the cell's position (e.g., "A1").
     */
    @Override
    public String toString() {
        // Convert x to letter (0=A, 1=B, etc.)
        char col = (char) ('A' + x);
        // y starts from 0, but we want to display from 1
        int row = y + 1;
        return "" + col + row;
    }

    /**
     * Converts a cell name (e.g., "A1", "B2") into its corresponding x and y coordinates.
     *
     * @param cellName The cell name in the format "A1", "B2", etc.
     * @return a new CellEntry object representing the coordinates of the cell.
     */
    public static CellEntry fromString(String cellName) {
        char colChar = cellName.charAt(0);
        int xCord = colChar - 'A';  // Convert column letter to x index
        int yCord = Integer.parseInt(cellName.substring(1)) - 1;  // Convert row number to y index
        return new CellEntry(xCord, yCord);
    }
}
