import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Implementation of a spreadsheet using a 2D array of cells.
 * Provides methods for manipulating and evaluating cell data.
 */
public class Ex2Sheet implements Sheet {
    private Cell[][] table;

    /**
     * Constructs a new spreadsheet with the given dimensions.
     *
     * @param x the number of columns.
     * @param y the number of rows.
     */
    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell("", this);
                table[i][j].setType(Ex2Utils.TEXT);
                ((SCell) table[i][j]).setEntry(new CellEntry(Ex2Utils.ABC[i] + j));
            }
        }
        eval();
    }

    /**
     * Constructs a new spreadsheet with default dimensions.
     */
    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    /**
     * Returns the computed value of a cell at the given coordinates.
     *
     * @param x the x-coordinate of the cell.
     * @param y the y-coordinate of the cell.
     * @return the computed value of the cell as a string.
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
            }
        }
        return ans;
    }

    /**
     * Returns the cell at the specified coordinates.
     *
     * @param x the x-coordinate of the cell.
     * @param y the y-coordinate of the cell.
     * @return the cell at the specified coordinates.
     */
    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    /**
     * Returns the cell at the specified coordinate in string format.
     *
     * @param cords the coordinate in string format (e.g., "A1").
     * @return the cell at the specified coordinate, or null if invalid.
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
     * Returns the width of the spreadsheet.
     *
     * @return the number of columns in the spreadsheet.
     */
    @Override
    public int width() {
        return table.length;
    }

    /**
     * Returns the height of the spreadsheet.
     *
     * @return the number of rows in the spreadsheet.
     */
    @Override
    public int height() {
        return table[0].length;
    }

    /**
     * Updates the cell at the specified coordinates with the given value.
     *
     * @param x the x-coordinate of the cell.
     * @param y the y-coordinate of the cell.
     * @param s the new value to set.
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
     * Updates cells dependent on the specified cell.
     *
     * @param x the x-coordinate of the cell.
     * @param y the y-coordinate of the cell.
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
     * Evaluates the entire spreadsheet.
     */
    @Override
    public void eval() {
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (table[i][j] != null) {
                    SCell cell = (SCell) table[i][j];
                    cell.resetVisited();
                    if (cell.getType() == Ex2Utils.ERR_CYCLE_FORM || cell.getType() == Ex2Utils.ERR_FORM_FORMAT) {
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
     * Computes the maximum depth of the given depth matrix.
     *
     * @param depths a 2D array of cell depths.
     * @return the maximum depth in the matrix.
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
     * Evaluates a single cell.
     *
     * @param x the x-coordinate of the cell.
     * @param y the y-coordinate of the cell.
     */
    private void evaluateCell(int x, int y) {
        SCell cell = (SCell) get(x, y);
        if (cell == null) return;

        String str = cell.getData();
        if (str.isEmpty()) {
            cell.setValue("");
            cell.setType(Ex2Utils.TEXT);
        } else if (str.charAt(0) == '=') {
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
     * Checks if the given coordinates are within the spreadsheet.
     *
     * @param xx the x-coordinate.
     * @param yy the y-coordinate.
     * @return true if the coordinates are valid, false otherwise.
     */
    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && yy >= 0 && xx < width() && yy < height();
    }

    /**
     * Computes the depth of each cell in the spreadsheet.
     *
     * @return a 2D array of cell depths.
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
                    int order = cell.calcOrder();
                    ans[i][j] = order;
                    cell.setOrder(order);
                }
            }
        }
        return ans;
    }

    /**
     * Clears all cells in the spreadsheet.
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
     * Loads a spreadsheet from the specified file.
     *
     * @param fileName the name of the file to load.
     * @throws IOException if the file cannot be read.
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
     * Saves the spreadsheet to the specified file.
     *
     * @param fileName the name of the file to save.
     * @throws IOException if the file cannot be written.
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
     * Evaluates the cell at the specified coordinates.
     *
     * @param x the x-coordinate of the cell.
     * @param y the y-coordinate of the cell.
     * @return the evaluated value as a string.
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
