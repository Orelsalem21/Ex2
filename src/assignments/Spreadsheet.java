package assignments;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class Spreadsheet implements Sheet {
    private Cell[][] cells;
    private int width;
    private int height;

    public Spreadsheet(int x, int y) {
        this.width = x;
        this.height = y;
        this.cells = new Cell[y][x];

        for (int i = 0; i < y; i++) {
            for (int j = 0; j < x; j++) {
                cells[i][j] = new SCell(Ex2Utils.EMPTY_CELL);
            }
        }
    }

    @Override
    public Cell get(int x, int y) {
        if (isIn(x, y)) {
            return cells[y][x];
        }
        return null;
    }

    @Override
    public Cell get(String cellName) {
        int x = xCell(cellName);
        int y = yCell(cellName);

        if (isIn(x, y)) {
            return cells[y][x];
        }
        return null;
    }

    @Override
    public void set(int x, int y, String c) {
        if (isIn(x, y)) {
            cells[y][x] = new SCell(c);
        }
    }

    public void set(int x, int y, Cell c) {
        if (isIn(x, y)) {
            cells[y][x] = c;
        }
    }

    public int xCell(String cellName) {
        if (cellName == null || cellName.isEmpty()) {
            return -1;
        }
        cellName = cellName.toUpperCase();
        return cellName.charAt(0) - 'A';
    }

    public int yCell(String cellName) {
        if (cellName == null || cellName.isEmpty()) {
            return -1;
        }
        try {
            cellName = cellName.toUpperCase(); // המרת אותיות קטנות לגדולות
            return Integer.parseInt(cellName.substring(1)) - 1; // חישוב המספר
        } catch (NumberFormatException e) {
            return -1;
        }
    }


    @Override
    public String eval(int x, int y) {
        if (!isIn(x, y)) {
            return Ex2Utils.ERR_FORM;
        }

        Cell cell = cells[y][x];

        switch (cell.getType()) {
            case Ex2Utils.FORM:
                Set<String> visitedCells = new HashSet<>();
                SCell scell = new SCell(cell.getData());
                Object result = scell.computeFormula(cell.getData(), this);

                if (result instanceof Double) {
                    return result.toString();
                } else if (result instanceof Integer && (int) result == Ex2Utils.ERR_FORM_FORMAT) {
                    return Ex2Utils.ERR_FORM;
                } else if (result instanceof Integer && (int) result == Ex2Utils.ERR_CYCLE_FORM) {
                    return Ex2Utils.ERR_CYCLE;
                }

                return Ex2Utils.ERR_FORM;

            case Ex2Utils.NUMBER:
            case Ex2Utils.TEXT:
                return cell.getData();

            default:
                return Ex2Utils.ERR_FORM;
        }
    }

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

    @Override
    public int[][] depth() {
        int[][] depths = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                depths[y][x] = calculateCellDepth(x, y);
            }
        }
        return depths;
    }

    private int calculateCellDepth(int x, int y) {
        Cell cell = cells[y][x];

        if (cell.getType() != Ex2Utils.FORM) {
            return 0;
        }

        Set<String> visitedCells = new HashSet<>();
        return calculateFormulaDepth(cell.getData(), visitedCells, x, y);
    }

    private int calculateFormulaDepth(String formula, Set<String> visitedCells, int x, int y) {
        String cellName = (char) ('A' + x) + String.valueOf(y + 1);

        if (visitedCells.contains(cellName)) {
            return Ex2Utils.ERR_CYCLE_FORM;
        }
        visitedCells.add(cellName);

        if (!SCell.isFormula(formula)) {
            visitedCells.remove(cellName);
            return 0;
        }

        String expr = formula.substring(1).trim();
        int maxDepth = 0;

        for (int i = 0; i < expr.length(); i++) {
            if (Character.isLetter(expr.charAt(i))) {
                StringBuilder cellRef = new StringBuilder();
                while (i < expr.length() && (Character.isLetterOrDigit(expr.charAt(i)))) {
                    cellRef.append(expr.charAt(i++));
                }
                i--;

                int refX = xCell(cellRef.toString());
                int refY = yCell(cellRef.toString());

                if (isIn(refX, refY)) {
                    int cellDepth = calculateCellDepth(refX, refY);

                    if (cellDepth == Ex2Utils.ERR_CYCLE_FORM) {
                        visitedCells.remove(cellName);
                        return Ex2Utils.ERR_CYCLE_FORM;
                    }

                    maxDepth = Math.max(maxDepth, cellDepth);
                }
            }
        }

        visitedCells.remove(cellName);
        return maxDepth + 1;
    }

    @Override
    public boolean isIn(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public void load(String fileName) throws IOException {
        // Not implemented
    }

    @Override
    public void save(String fileName) throws IOException {
        // Not implemented
    }

    @Override
    public String value(int x, int y) {
        return eval(x, y);
    }
}
