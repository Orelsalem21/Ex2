/**
 * Represents a cell entry in a spreadsheet, implementing the Index2D interface.
 * The index is represented as a string (e.g., "A1", "B2").
 */
public class CellEntry implements Index2D {
    private String index;

    /**
     * Constructs a CellEntry with the specified index.
     *
     * @param index the string index of the cell (e.g., "A1").
     */
    public CellEntry(String index) {
        this.index = index;
    }

    /**
     * Gets the index of the cell.
     *
     * @return the index of the cell as a string.
     */
    public String getIndex() {
        return index;
    }

    /**
     * Sets the index of the cell.
     *
     * @param index the new index for the cell.
     */
    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * Validates the current cell index.
     * A valid index starts with a letter followed by a number (e.g., "A1").
     *
     * @return true if the index is valid, false otherwise.
     */
    @Override
    public boolean isValid() {
        if (this.index == null || this.index.isEmpty()) {
            return false;
        }
        if (this.index.length() > 3 || this.index.length() < 2) {
            return false;
        }
        if (!isLetter(this.index.charAt(0))) {
            return false;
        }
        try {
            int num = Integer.parseInt(this.index.substring(1));
            return num >= 0 && num <= 99;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Gets the X coordinate (column index) of the cell.
     *
     * @return the X coordinate, or -1 if the index is invalid.
     */
    @Override
    public int getX() {
        String[] arr = Ex2Utils.ABC;
        for (int i = 0; i < arr.length; i++) {
            if (String.valueOf(this.index.charAt(0)).equals(arr[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets the Y coordinate (row index) of the cell.
     *
     * @return the Y coordinate, or -1 if the index is invalid.
     */
    @Override
    public int getY() {
        try {
            return Integer.parseInt(this.index.substring(1));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Returns the string representation of the cell index.
     *
     * @return the index as a string.
     */
    @Override
    public String toString() {
        return this.index;
    }

    /**
     * Checks if a given character is a valid column letter.
     *
     * @param c the character to check.
     * @return true if the character is a valid column letter, false otherwise.
     */
    public static boolean isLetter(char c) {
        String s = String.valueOf(c);
        for (String str : Ex2Utils.ABC) {
            if (str.equals(s)) {
                return true;
            }
        }
        return false;
    }
}
