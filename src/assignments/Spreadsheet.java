package assignments;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a spreadsheet with cells that can hold text, numbers, or formulas.
 */
public class Spreadsheet implements Sheet {
    private Cell[][] cells;
    private int width;
    private int height;


    /**
     * Constructor to initialize the spreadsheet with the specified dimensions.
     *
     * @param width The number of columns.
     * @param height The number of rows.
     */
    public Spreadsheet(int width, int height) {
        // Check if the width (number of columns) is between 1 and 26 (A-Z)
        if (width < 1 || width > 26) {
            throw new IllegalArgumentException("The number of columns must be between 1 and 26.");
        }

        if (height < 0 || height > 99) {
            throw new IllegalArgumentException("The number of rows must be between 0 and 99.");
        }

        this.width = width;
        this.height = height;
        this.cells = new Cell[height][width]; // Create the array
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                cells[i][j] = new SCell("");  // Initialize the cells
            }
        }
    }
    // Public methods (overriding Sheet interface)

    /**
     * Gets the cell at the specified coordinates (x, y).
     *
     * @param x The x-coordinate (column index).
     * @param y The y-coordinate (row index).
     * @return The cell at the specified coordinates, or null if the coordinates are out of bounds.
     */
    @Override
    public Cell get(int x, int y) {
        return isIn(x, y) ? cells[y][x] : null;
    }

    /**
     * Gets the cell based on the cell name (e.g., "A1").
     *
     * @param cellName The name of the cell (e.g., "A1").
     * @return The cell corresponding to the given name, or null if the cell name is invalid.
     */
    @Override
    public Cell get(String cellName) {
        int x = cellNameToX(cellName);
        int y = cellNameToY(cellName);
        return isIn(x, y) ? cells[y][x] : null;
    }

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
     * Sets the data for the cell at the given coordinates.
     *
     * @param x The x-coordinate (column index).
     * @param y The y-coordinate (row index).
     * @param s The data to set for the cell.
     */
    @Override
    public void set(int x, int y, String s) {
        if (isIn(x, y)) {
            cells[y][x] = new SCell(s);
        }
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
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /**
     * Returns the width (number of columns) of the spreadsheet.
     *
     * @return The width of the spreadsheet.
     */
    @Override
    public int width() {
        return width;
    }

    /**
     * Returns the height (number of rows) of the spreadsheet.
     *
     * @return The height of the spreadsheet.
     */
    @Override
    public int height() {
        return height;
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
        if (!isIn(x, y)) {
            return Ex2Utils.ERR_FORM;
        }
        Cell cell = cells[y][x];
        if (cell.getType() == Ex2Utils.FORM) {
            Set<String> visitedCells = new HashSet<>();
            return evaluateFormula(cell.getData(), visitedCells, x, y);
        }
        return cell.getData();
    }
    /**
     * Evaluates the entire spreadsheet and returns a 2D array of the evaluated values.
     *
     * @return A 2D array of the evaluated values for all cells.
     */
    @Override
    public String[][] eval() {
        String[][] result = new String[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                result[y][x] = eval(x, y);
            }
        }
        return result;
    }

    /**
     * Calculates the depth of each cell in terms of its formula dependencies.
     *
     * @return A 2D array representing the depth of each cell.
     */
    @Override
    public int[][] depth() {
        int[][] depths = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                depths[y][x] = calculateDepth(x, y);
            }
        }
        return depths;
    }
    
    /**
     * Placeholder for the save method, which is not implemented in this class.
     * This method is handled and fully implemented in the Ex2Sheet class.
     *
     * @param fileName The name of the file to save the spreadsheet data.
     * @throws UnsupportedOperationException as this method is not supported in this class.
     */
    @Override
    public void save(String fileName) throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void load(String fileName) throws IOException {
        throw new UnsupportedOperationException("Not supported.");
    }

    // Private helper methods

    /**
     * Evaluates a formula and returns its result.
     * Handles circular references and computation of the formula's value.
     *
     * @param formula The formula to evaluate.
     * @param visitedCells A set of cells that have already been visited to prevent circular references.
     * @param x The x-coordinate (column index).
     * @param y The y-coordinate (row index).
     * @return The evaluated result of the formula.
     */
    private String evaluateFormula(String formula, Set<String> visitedCells, int x, int y) {
        String cellName = (char) ('A' + x) + String.valueOf(y + 1);
        if (visitedCells.contains(cellName)) {
            return Ex2Utils.ERR_FORM;
        }
        visitedCells.add(cellName);

        try {
            Double result = SCell.computeForm(formula, this, visitedCells);
            return result != null ? result.toString() : Ex2Utils.ERR_FORM;
        } catch (Exception e) {
            return Ex2Utils.ERR_FORM;
        } finally {
            visitedCells.remove(cellName);
        }
    }



    /**
     * Calculates the depth of a cell's formula by analyzing its dependencies.
     *
     * @param x The x-coordinate (column index).
     * @param y The y-coordinate (row index).
     * @return The depth of the formula.
     */
    private int calculateDepth(int x, int y) {
        Cell cell = cells[y][x];
        if (cell.getType() != Ex2Utils.FORM) {
            return 0;
        }
        Set<String> visitedCells = new HashSet<>();
        return calculateFormulaDepth(cell.getData(), visitedCells, x, y);
    }

    /**
     * Calculates the formula depth considering its dependencies.
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
            return -1;
        }
        visitedCells.add(cellName);

        int maxDepth = 0;
        visitedCells.remove(cellName);
        return maxDepth + 1;
    }

    /**
     * Converts a cell name (e.g., "A1") to its x-coordinate (column index).
     *
     * @param cellName The cell name (e.g., "A1").
     * @return The x-coordinate of the cell.
     */
    public int cellNameToX(String cellName) {
        return cellName.charAt(0) - 'A';
    }

    /**
     * Converts a cell name (e.g., "A1") to its y-coordinate (row index).
     *
     * @param cellName The cell name (e.g., "A1").
     * @return The y-coordinate of the cell.
     */
    public int cellNameToY(String cellName) {
        return Integer.parseInt(cellName.substring(1)) - 1;
    }
}
