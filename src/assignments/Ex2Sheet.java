package assignments;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a spreadsheet with cells that can hold text, numbers, or formulas.
 */
public class Ex2Sheet implements Sheet {
    private Cell[][] table;

    /**
     * Constructor to initialize the spreadsheet with the specified dimensions.
     *
     * @param x The number of columns.
     * @param y The number of rows.
     */
    public Ex2Sheet(int x, int y) {
        table = new Cell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell(Ex2Utils.EMPTY_CELL);
            }
        }
    }

    /**
     * Default constructor initializing the spreadsheet with default width and height.
     */
    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    // Public methods

    /**
     * Returns the evaluated value of a cell at the given coordinates.
     *
     * @param x The x-coordinate (column index).
     * @param y The y-coordinate (row index).
     * @return The evaluated value of the cell at (x, y).
     */
    @Override
    public String value(int x, int y) {
        return eval(x, y);
    }

    /**
     * Gets the cell at the specified coordinates.
     *
     * @param x The x-coordinate (column index).
     * @param y The y-coordinate (row index).
     * @return The cell at the specified coordinates.
     */
    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    /**
     * Gets the cell based on the cell name (e.g., "A1").
     *
     * @param cellName The name of the cell (e.g., "A1").
     * @return The cell corresponding to the given cell name.
     */
    @Override
    public Cell get(String cellName) {
        int x = xCell(cellName);
        int y = yCell(cellName);
        return isIn(x, y) ? table[x][y] : null;
    }

    /**
     * Returns the width (number of columns) of the spreadsheet.
     *
     * @return The width of the spreadsheet.
     */
    @Override
    public int width() {
        return table.length;
    }

    /**
     * Returns the height (number of rows) of the spreadsheet.
     *
     * @return The height of the spreadsheet.
     */
    @Override
    public int height() {
        return table[0].length;
    }

    /**
     * Sets the data for a specific cell at the given coordinates.
     *
     * @param x The x-coordinate (column index).
     * @param y The y-coordinate (row index).
     * @param s The data to set for the cell.
     */
    @Override
    public void set(int x, int y, String s) {
        table[x][y] = new SCell(s);
    }

    /**
     * Evaluates the entire spreadsheet and returns a 2D array of the evaluated values.
     *
     * @return A 2D array of the evaluated values for all cells.
     */
    @Override
    public String[][] eval() {
        String[][] result = new String[height()][width()];
        for (int y = 0; y < height(); y++) {
            for (int x = 0; x < width(); x++) {
                result[y][x] = eval(x, y);
            }
        }
        return result;
    }

    /**
     * Checks if the coordinates (x, y) are within the valid range of the spreadsheet.
     *
     * @param x The x-coordinate (column index).
     * @param y The y-coordinate (row index).
     * @return true if the coordinates are valid, otherwise false.
     */
    @Override
    public boolean isIn(int x, int y) {
        return x >= 0 && x < width() && y >= 0 && y < height();
    }

    /**
     * Calculates the depth of each cell in terms of its formula dependencies.
     *
     * @return A 2D array representing the depth of each cell.
     */
    @Override
    public int[][] depth() {
        int[][] depths = new int[width()][height()];
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                depths[x][y] = calculateDepth(x, y);
            }
        }
        return depths;
    }

    /**
     * Evaluates the value of a specific cell at the given coordinates.
     *
     * @param x The x-coordinate (column index).
     * @param y The y-coordinate (row index).
     * @return The evaluated value of the cell at (x, y).
     */
    @Override
    public String eval(int x, int y) {
        Cell cell = get(x, y);
        if (cell == null) {
            return Ex2Utils.EMPTY_CELL;
        }

        if (cell.getType() == Ex2Utils.FORM) {
            Set<String> visitedCells = new HashSet<>();
            return evaluateFormula(cell.getData(), visitedCells, x, y);
        }
        return cell.getData();
    }
    // Saving and loading methods

    /**
     * Saves the spreadsheet data to a file.
     *
     * @param fileName The name of the file to save data to.
     * @throws IOException if an I/O error occurs during saving.
     */
    @Override
    public void save(String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (int x = 0; x < width(); x++) {
                for (int y = 0; y < height(); y++) {
                    Cell cell = get(x, y);
                    if (cell != null && !cell.getData().isEmpty()) {
                        writer.write(x + "," + y + "," + cell.getData());
                        writer.newLine();
                    }
                }
            }
        }
    }

    /**
     * Loads the spreadsheet data from a file.
     * Clears all existing data before loading new data.
     *
     * @param fileName The name of the file to load data from.
     * @throws IOException if an I/O error occurs during loading.
     */
    @Override
    public void load(String fileName) throws IOException {
        // Clear all existing data first
        clearAllCells();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip metadata or irrelevant lines
                if (line.trim().isEmpty() || line.startsWith("I2CS")) {
                    continue;
                }

                // Parse the line
                String[] parts = line.split(",", 3);
                if (parts.length == 3) {
                    try {
                        int x = Integer.parseInt(parts[0].trim());
                        int y = Integer.parseInt(parts[1].trim());
                        String value = parts[2].trim();

                        // Set the value in the table
                        if (isIn(x, y)) {
                            set(x, y, value);
                        }
                    } catch (NumberFormatException e) {
                        // Skip lines with invalid coordinates
                        continue;
                    }
                }
            }
        }
    }

    /**
     * Clears all cells in the spreadsheet.
     */
    private void clearAllCells() {
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                set(x, y, "");  // Clear the cell
            }
        }
    }

    // Private helper methods

    /**
     * Calculates the depth of a formula in a cell.
     *
     * @param x The x-coordinate (column index).
     * @param y The y-coordinate (row index).
     * @return The depth of the cell's formula.
     */
    private int calculateDepth(int x, int y) {
        Cell cell = get(x, y);
        if (cell.getType() != Ex2Utils.FORM) {
            return 0;
        }
        Set<String> visitedCells = new HashSet<>();
        return calculateFormulaDepth(cell.getData(), visitedCells, x, y);
    }

    /**
     * Calculates the maximum depth of a formula, considering its dependencies.
     *
     * @param formula The formula to analyze.
     * @param visitedCells A set of cells that have already been evaluated to prevent circular dependencies.
     * @param x The x-coordinate (column index).
     * @param y The y-coordinate (row index).
     * @return The depth of the formula.
     */
    private int calculateFormulaDepth(String formula, Set<String> visitedCells, int x, int y) {
        String cellName = (char) ('A' + x) + String.valueOf(y + 1);
        if (visitedCells.contains(cellName)) {
            return -1; // Indicate circular reference
        }
        visitedCells.add(cellName);

        int maxDepth = 0;
        if (formula != null && formula.length() > 1) {
            String expr = formula.substring(1).trim(); // Remove the '=' sign

            // Look for cell references in the formula (e.g., A1, B2, etc.)
            for (int i = 0; i < expr.length(); i++) {
                if (Character.isLetter(expr.charAt(i)) && i + 1 < expr.length()) {
                    StringBuilder cellRef = new StringBuilder();
                    cellRef.append(expr.charAt(i));

                    // Collect the number part of the cell reference
                    while (i + 1 < expr.length() && Character.isDigit(expr.charAt(i + 1))) {
                        cellRef.append(expr.charAt(++i));
                    }

                    String ref = cellRef.toString();
                    if (ref.matches("[A-Z][0-9]+")) {
                        int refX = ref.charAt(0) - 'A';
                        int refY = Integer.parseInt(ref.substring(1)) - 1;

                        if (isIn(refX, refY)) {
                            Cell refCell = get(refX, refY);
                            if (refCell != null && refCell.getType() == Ex2Utils.FORM) {
                                int depth = calculateFormulaDepth(refCell.getData(), new HashSet<>(visitedCells), refX, refY);
                                if (depth == -1) return -1; // Propagate circular reference error
                                maxDepth = Math.max(maxDepth, depth);
                            }
                        }
                    }
                }
            }
        }

        visitedCells.remove(cellName);
        return maxDepth + 1;
    }

    private String evaluateFormula(String formula, Set<String> visitedCells, int x, int y) {
        // בדיקה מיוחדת להפניה עצמית ישירה
        if (formula.substring(1).trim().equals("A" + y)) {
            return Ex2Utils.ERR_CYCLE;
        }

        String cellName = (char) ('A' + x) + String.valueOf(y);
        if (visitedCells.contains(cellName)) {
            return Ex2Utils.ERR_CYCLE;
        }
        visitedCells.add(cellName);

        try {
            Double result = SCell.computeForm(formula, this, visitedCells);
            if (result == null) {
                return Ex2Utils.ERR_FORM;
            }
            if (result == Ex2Utils.ERR_CYCLE_FORM) {
                return Ex2Utils.ERR_CYCLE;
            }
            if (result == Ex2Utils.ERR_FORM_FORMAT) {
                return Ex2Utils.ERR_FORM;
            }
            return result.toString();
        } catch (Exception e) {
            return Ex2Utils.ERR_FORM;
        } finally {
            visitedCells.remove(cellName);
        }
    }
    /**
     * Converts a cell name (e.g., "A1") to its x-coordinate (column index).
     *
     * @param cellName The cell name (e.g., "A1").
     * @return The x-coordinate of the cell.
     */
    private int xCell(String cellName) {
        if (cellName.length() < 2) {
            return -1;
        }
        return cellName.charAt(0) - 'A';
    }

    /**
     * Converts a cell name (e.g., "A1") to its y-coordinate (row index).
     *
     * @param cellName The cell name (e.g., "A1").
     * @return The y-coordinate of the cell.
     */
    private int yCell(String cellName) {
        if (cellName.length() < 2) {
            return -1;
        }
        return Integer.parseInt(cellName.substring(1)) - 1;
    }
}
