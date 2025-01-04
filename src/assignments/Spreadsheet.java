package assignments;

public class Spreadsheet {
    private Cell[][] cells; // 2D array to store the cells as type Cell
    private int width;       // Width of the spreadsheet (number of columns)
    private int height;      // Height of the spreadsheet (number of rows)

    /**
     * Constructor to initialize the Spreadsheet with given dimensions.
     * Initializes all cells with empty SCell objects (using the Cell interface).
     *
     * @param width  Number of columns.
     * @param height Number of rows.
     */
    public Spreadsheet(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[height][width]; // Initialize the 2D array of Cells

        // Initialize each cell with an empty SCell objects
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                cells[i][j] = new SCell(""); // Default empty data as a Cell object
            }
        }
    }

    /**
     * Gets the cell at the specified (x, y) position.
     *
     * @param x The column index (x-coordinate).
     * @param y The row index (y-coordinate).
     * @return The Cell object at position (x, y).
     */
    public Cell get(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Invalid cell coordinates");
        }
        return cells[y][x];
    }

    /**
     * Sets the cell at the specified (x, y) position with a new Cell object.
     *
     * @param x The column index (x-coordinate).
     * @param y The row index (y-coordinate).
     * @param c The Cell object to set at position (x, y).
     */
    public void set(int x, int y, Cell c) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Invalid cell coordinates");
        }
        cells[y][x] = c;
    }

    /**
     * Returns the number of columns (width) of the spreadsheet.
     *
     * @return The width of the spreadsheet.
     */
    public int width() {
        return width;
    }

    /**
     * Returns the number of rows (height) of the spreadsheet.
     *
     * @return The height of the spreadsheet.
     */
    public int height() {
        return height;
    }

    /**
     * Converts a cell notation (e.g., "F13") to the corresponding x-coordinate (column index).
     *
     * @param c The cell notation (e.g., "F13").
     * @return The corresponding x-coordinate (column index).
     */
    public int xCell(String c) {
        if (c == null || c.length() < 2) return -1; // Invalid input
        char columnChar = c.charAt(0);
        if (columnChar < 'A' || columnChar > 'Z') return -1; // Invalid column
        return columnChar - 'A'; // Convert column letter to index
    }

    /**
     * Converts a cell notation (e.g., "F13") to the corresponding y-coordinate (row index).
     *
     * @param c The cell notation (e.g., "F13").
     * @return The corresponding y-coordinate (row index).
     */
    public int yCell(String c) {
        if (c == null || c.length() < 2) return -1; // Invalid input
        try {
            return Integer.parseInt(c.substring(1)) - 1; // Convert row number to index
        } catch (NumberFormatException e) {
            return -1; // Invalid row number
        }
    }

    /**
     * Evaluates the value of the cell at position (x, y), including formula evaluation.
     *
     * @param x The column index (x-coordinate).
     * @param y The row index (y-coordinate).
     * @return The evaluated value of the cell.
     */
    public String eval(int x, int y) {
        // For now, just return the raw data from the cell
        return cells[y][x].getData();
    }

    /**
     * Evaluates the values of all cells in the spreadsheet.
     *
     * @return A 2D array containing the evaluated values of all cells.
     */
    public String[][] evalAll() {
        String[][] result = new String[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                result[i][j] = eval(j, i); // Evaluate each cell
            }
        }
        return result;
    }

    /**
     * Computes the computational depth of each cell.
     * If the cell is a number or text, the depth is 0.
     * If the cell contains a formula, the depth depends on its dependencies.
     *
     * @return A 2D array representing the depth of each cell.
     */
    public int[][] depth() {
        int[][] depth = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // Calculate depth for each cell
                depth[i][j] = 0; // Default to 0 for simplicity
            }
        }
        return depth;
    }
}
