package assignments;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class Ex2Sheet implements Sheet {
    private Cell[][] table;

    public Ex2Sheet(int x, int y) {
        table = new Cell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell(Ex2Utils.EMPTY_CELL);
            }
        }
    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        return eval(x, y);
    }

    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    @Override
    public Cell get(String cellName) {
        int x = xCell(cellName);
        int y = yCell(cellName);
        return isIn(x, y) ? table[x][y] : null;
    }

    @Override
    public int width() {
        return table.length;
    }

    @Override
    public int height() {
        return table[0].length;
    }

    @Override
    public void set(int x, int y, String s) {
        table[x][y] = new SCell(s);
    }

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

    @Override
    public boolean isIn(int x, int y) {
        return x >= 0 && x < width() && y >= 0 && y < height();
    }

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

    private int calculateDepth(int x, int y) {
        Cell cell = get(x, y);
        if (cell.getType() != Ex2Utils.FORM) {
            return 0;
        }
        Set<String> visitedCells = new HashSet<>();
        return calculateFormulaDepth(cell.getData(), visitedCells, x, y);
    }

    private int calculateFormulaDepth(String formula, Set<String> visitedCells, int x, int y) {
        String cellName = (char) ('A' + x) + String.valueOf(y + 1);
        if (visitedCells.contains(cellName)) {
            return -1; // Circular reference
        }
        visitedCells.add(cellName);

        int maxDepth = 0;
        // Add logic to analyze the formula and find the maximum depth
        visitedCells.remove(cellName);
        return maxDepth + 1;
    }

    @Override
    public void load(String fileName) throws IOException {
        // Implement load logic if needed
    }

    @Override
    public void save(String fileName) throws IOException {
        // Implement save logic if needed
    }

    private String evaluateFormula(String formula, Set<String> visitedCells, int x, int y) {
        String cellName = (char) ('A' + x) + String.valueOf(y + 1);
        cellName = cellName.toUpperCase();
        if (visitedCells.contains(cellName)) {
            return Ex2Utils.ERR_CYCLE;
        }
        visitedCells.add(cellName);

        try {
            SCell scell = new SCell(formula); // יצירת מופע חדש
            Object result = scell.computeFormula(formula, this);

            // Explicit type checking and handling
            if (result == null || result.equals(Ex2Utils.ERR_FORM_FORMAT)) {
                return Ex2Utils.ERR_FORM;
            }
            if (result.equals(Ex2Utils.ERR_CYCLE_FORM)) {
                return Ex2Utils.ERR_CYCLE;
            }
            if (result instanceof Double) {
                return result.toString();
            }

            // Fallback for unexpected result types
            return Ex2Utils.ERR_FORM;
        } catch (Exception e) {
            return Ex2Utils.ERR_FORM;
        } finally {
            visitedCells.remove(cellName);
        }
    }

    public int xCell(String cellName) {
        if (cellName == null || cellName.isEmpty()) {
            return -1;
        }
        cellName = cellName.toUpperCase(); // להמיר לאותיות גדולות
        return cellName.charAt(0) - 'A';
    }

    private int yCell(String cellName) {
        if (cellName.length() < 2) {
            return -1;
        }
        return Integer.parseInt(cellName.substring(1)) - 1;
    }
}
