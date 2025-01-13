/**
 * Represents a cell entry with an index in a spreadsheet.
 * The index is validated and converted to 2D coordinates.
 */
public class CellEntry implements Index2D {
    private String index;

    /**
     * Constructs a CellEntry with a given index.
     *
     * @param index the cell index (e.g., "A1", "B2").
     */
    public CellEntry(String index) {
        this.index = index;
    }

    /**
     * Gets the index of the cell.
     *
     * @return the index as a string.
     */
    public String getIndex() {
        return index;
    }

    /**
     * Sets the index of the cell.
     *
     * @param index the new index to set.
     */
    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * Validates if the cell index is valid based on specific rules:
     * - The index is not null or empty.
     * - The index length is between 2 and 3 characters.
     * - The first character is a valid letter (A-Z).
     * - The numeric part of the index is between 0 and 99.
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
     * Gets the X coordinate (column) of the cell based on its index.
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
     * Gets the Y coordinate (row) of the cell based on its index.
     *
     * @return the Y coordinate, or -1 if the numeric part is invalid.
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
     * Converts the cell index to a string representation.
     *
     * @return the index as a string.
     */
    @Override
    public String toString() {
        return this.index;
    }

    /**
     * Checks if a given character is a valid letter (A-Z).
     *
     * @param c the character to check.
     * @return true if the character is a letter, false otherwise.
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
