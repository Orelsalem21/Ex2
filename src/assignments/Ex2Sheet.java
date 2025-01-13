import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
/**
 * Implementation of the Sheet interface using a 2D array of SCell objects.
 * Supports spreadsheet functionalities such as setting, evaluating, loading, and saving cell data.
 */
public class Ex2Sheet implements Sheet {
    /**
     * 2D array of cells representing the spreadsheet's table.
     * Each cell is initialized with default values and type is set to TEXT.
     * The cell entries are assigned based on the column (letter) and row (number).
     *
     * @param x the number of columns in the spreadsheet.
     * @param y the number of rows in the spreadsheet.
     */
    private Cell[][] table;

    /**
     * Constructs a spreadsheet with the specified dimensions.
     * Initializes each cell in the table with default values, sets its type to TEXT,
     * and assigns a cell entry based on its column and row position.
     *
     * @param x the number of columns.
     * @param y the number of rows.
     */
    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for (int i = 0; i < x; i = i + 1) {
            for (int j = 0; j < y; j = j + 1) {
                table[i][j] = new SCell("", this);
                table[i][j].setType(Ex2Utils.TEXT);
                ((SCell) table[i][j]).setEntry(new CellEntry(Ex2Utils.ABC[i] + j));
            }
        }
        eval();
    }

    /**
     * Constructs a spreadsheet with default dimensions defined by Ex2Utils.
     * The default dimensions are typically WIDTH x HEIGHT as specified in Ex2Utils.
     */
    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    /**
     * Retrieves the value of a cell at the specified coordinates.
     * Evaluates the cell if needed and handles errors such as invalid format or cycle errors.
     *
     * @param x the column index of the cell.
     * @param y the row index of the cell.
     * @return the value of the cell, or error messages if applicable.
     */
    @Override
    public String value(int x, int y) {
        String ans = Ex2Utils.EMPTY_CELL;
        SCell c = (SCell) get(x, y);
        if (c != null) {
            this.eval();
            if (c.getType() == Ex2Utils.ERR_FORM_FORMAT) {
                return Ex2Utils.ERR_FORM;
            } else if (c.getType() == Ex2Utils.ERR_CYCLE_FORM) {
                return Ex2Utils.ERR_CYCLE;
            }
            ans = c.toString();
            if (c.getType() == Ex2Utils.FORM) {
                try {
                    eval();
                    ans = String.valueOf(c.computeForm(ans));
                } catch (SCell.ErrorForm e) {
                    ans = Ex2Utils.ERR_FORM;
                } catch (SCell.ErrorCycle e) {
                    ans = Ex2Utils.ERR_CYCLE;
                }
            } else if (c.getType() == Ex2Utils.NUMBER) {
                ans = Double.parseDouble(ans) + "";
            } else if (c.getType() == Ex2Utils.TEXT) {
                ans = ans;
            }
        }
        return ans;
    }

    /**
     * Retrieves the cell at the specified coordinates.
     *
     * @param x the column index.
     * @param y the row index.
     * @return the cell at the specified coordinates.
     */
    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    /**
     * Retrieves the cell corresponding to the given string index.
     *
     * @param cords the cell index as a string (e.g., "A1").
     * @return the cell at the specified index.
     */
    @Override
    public Cell get(String cords) {
        Cell ans = null;
        CellEntry ce = new CellEntry(cords);

        if (ce.isValid()) {
            int x = ce.getX();
            int y = ce.getY();
            ans = get(x, y);
        }
        return ans;
    }

    /**
     * Returns the width (number of columns) of the spreadsheet.
     *
     * @return the number of columns in the spreadsheet.
     */
    @Override
    public int width() {
        return table.length;
    }

    /**
     * Returns the height (number of rows) of the spreadsheet.
     *
     * @return the number of rows in the spreadsheet.
     */
    @Override
    public int height() {
        return table[0].length;
    }

    /**
     * Sets the value of the cell at the specified coordinates.
     * Also updates dependent cells and evaluates the changed cell.
     *
     * @param x the column index.
     * @param y the row index.
     * @param s the value to set.
     */
    @Override
    public void set(int x, int y, String s) {
        SCell c = new SCell(s, this);
        c.setEntry(new CellEntry(Ex2Utils.ABC[x] + y));
        table[x][y] = c;
        updateDependentCells(x, y);
        eval(x, y);
    }

    /**
     * Updates all dependent cells that refer to the given cell.
     *
     * @param x the column index of the updated cell.
     * @param y the row index of the updated cell.
     */
    private void updateDependentCells(int x, int y) {
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                SCell cell = (SCell) get(i, j);
                if (cell.getType() == Ex2Utils.FORM) {
                    ArrayList<SCell> refs = cell.getReferences(cell.getData());
                    for (SCell ref : refs) {
                        if (ref.entry.getIndex().equals(Ex2Utils.ABC[x] + y)) {
                            eval(i, j);
                        }
                    }
                }
            }
        }
    }

    /**
     * Evaluates all cells in the spreadsheet to update their values.
     * Resets error states and processes each cell based on its type.
     */
    @Override
    public void eval() {
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (table[i][j] != null) {
                    SCell cell = (SCell) table[i][j];
                    cell.resetVisited();
                    // Reset error states
                    if (cell.getType() == Ex2Utils.ERR_CYCLE_FORM ||
                            cell.getType() == Ex2Utils.ERR_FORM_FORMAT) {
                        cell.setType(Ex2Utils.FORM);
                    }
                }
            }
        }

        int[][] dd = depth();

        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (dd[i][j] == -1) {
                    SCell cell = (SCell) get(i, j);
                    cell.setType(Ex2Utils.ERR_CYCLE_FORM);
                    cell.setValue(Ex2Utils.ERR_CYCLE);
                }
            }
        }

        for (int depth = 0; depth <= getMaxDepth(dd); depth++) {
            for (int i = 0; i < width(); i++) {
                for (int j = 0; j < height(); j++) {
                    if (dd[i][j] == depth) {
                        evaluateCell(i, j);
                    }
                }
            }
        }
    }

    /**
     * Returns the maximum depth from the given depth matrix.
     *
     * @param depths the depth matrix.
     * @return the maximum depth value.
     */
    private int getMaxDepth(int[][] depths) {
        int max = 0;
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (depths[i][j] > max) {
                    max = depths[i][j];
                }
            }
        }
        return max;
    }

    /**
     * Evaluates a specific cell at the given coordinates.
     *
     * @param x the column index of the cell.
     * @param y the row index of the cell.
     */
    private void evaluateCell(int x, int y) {
        SCell cell = (SCell) get(x, y);
        if (cell == null) return;

        cell.setType(Ex2Utils.TEXT);
        cell.setValue("");
        cell.resetVisited();

        String str = cell.getData();
        if (str.isEmpty()) {
            cell.setValue("");
            cell.setType(Ex2Utils.TEXT);
            return;
        }

        if (str.charAt(0) == '=') {
            if (cell.isForm(str)) {
                ArrayList<String> cyclePath = new ArrayList<>();
                if (cell.detectCycle(cyclePath)) {
                    cell.setType(Ex2Utils.ERR_CYCLE_FORM);
                    cell.setValue(Ex2Utils.ERR_CYCLE);
                } else {
                    try {
                        String result = eval(x, y);
                        cell.setValue(result);
                        if (result.equals(Ex2Utils.ERR_FORM)) {
                            cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                        } else {
                            cell.setType(Ex2Utils.FORM);
                        }
                    } catch (Exception e) {
                        cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                        cell.setValue(Ex2Utils.ERR_FORM);
                    }
                }
            } else {
                cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                cell.setValue(Ex2Utils.ERR_FORM);
            }
        } else if (cell.isNumber(str)) {
            cell.setValue(Double.parseDouble(str) + "");
            cell.setType(Ex2Utils.NUMBER);
        } else {
            cell.setValue(str);
            cell.setType(Ex2Utils.TEXT);
        }
    }

    /**
     * Checks if the given coordinates are within the bounds of the spreadsheet.
     *
     * @param xx the column index.
     * @param yy the row index.
     * @return true if the coordinates are valid, false otherwise.
     */
    @Override
    public boolean isIn(int xx, int yy) {
        boolean ans = xx >= 0 && yy >= 0;
        if (xx > this.width() || yy > this.height()) {
            ans = false;
        }
        return ans;
    }

    /**
     * Calculates the depth of each cell, determining the order of evaluation.
     *
     * @return a 2D array representing the depth of each cell.
     */
    @Override
    public int[][] depth() {
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (table[i][j] != null) {
                    ((SCell) table[i][j]).resetVisited();
                }
            }
        }

        int[][] ans = new int[width()][height()];

        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                SCell cell = (SCell) get(i, j);
                if (cell != null) {
                    // calc order for this cell
                    int order = cell.calcOrder();
                    ans[i][j] = order;
                    // Update this cell order
                    cell.setOrder(order);
                }
            }
        }
        System.out.println(Arrays.deepToString(ans));
        return ans;
    }

    /**
     * Clears the entire table by setting all cells to empty and text type.
     */
    public void clearTable() {
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                set(i, j, "");
                get(i, j).setType(Ex2Utils.TEXT);
            }
        }
    }

    /**
     * Loads spreadsheet data from a file and populates the cells.
     *
     * @param fileName the name of the file to load the data from.
     * @throws IOException if an error occurs while reading the file.
     */
    @Override
    public void load(String fileName) throws IOException {
        try {
            clearTable();
            File file = new File(fileName);
            Scanner myReader = new Scanner(file);
            if (!myReader.hasNext()) {
                return;
            }
            String line = myReader.nextLine();
            while (myReader.hasNextLine() && line != null) {
                line = myReader.nextLine();
                line = line.replaceAll(" ", "");
                String[] arr = line.split(",");
                if (arr.length < 3) {
                    continue;
                }
                try {
                    int x = Integer.parseInt(arr[0]);
                    int y = Integer.parseInt(arr[1]);
                    this.set(x, y, arr[2]);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            }
            myReader.close();
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
        eval();
    }

    /**
     * Saves the current state of the spreadsheet to a file.
     *
     * @param fileName the name of the file to save the data to.
     * @throws IOException if an error occurs while writing to the file.
     */
    @Override
    public void save(String fileName) throws IOException {
        try {
            File newFile = new File(fileName);
            FileWriter myWriter = new FileWriter(newFile);
            myWriter.write("FirstLine\n");
            for (int i = 0; i < width(); i++) {
                for (int j = 0; j < height(); j++) {
                    SCell cell = (SCell) get(i, j);
                    if (cell != null && !cell.getData().isEmpty()) {
                        myWriter.write(i + "," + j + "," + cell.getData() + "\n");
                    }
                }
            }
            myWriter.close();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Evaluates the cell at the specified coordinates and returns the result.
     *
     * @param x the column index of the cell.
     * @param y the row index of the cell.
     * @return the evaluated value of the cell.
     */
    @Override
    public String eval(int x, int y) {
        String ans = null;
        SCell cell = (SCell) get(x, y);
        if (cell != null) {
            ans = cell.toString();
            try {
                ans = String.valueOf(cell.computeForm(ans));
            } catch (SCell.ErrorForm e) {
                ans = Ex2Utils.ERR_FORM;
            } catch (SCell.ErrorCycle e) {
                ans = Ex2Utils.ERR_CYCLE;
            }
            cell.setValue(ans);
        }
        return ans;
    }
}