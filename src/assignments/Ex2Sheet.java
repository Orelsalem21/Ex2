package assignments;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

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
    public boolean isIn(int x, int y) {
        return x >= 0 && x < width() && y >= 0 && y < height();
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
        if (isIn(x, y)) {
            table[x][y] = new SCell(s);
        }
    }

    @Override
    public Cell get(int x, int y) {
        return isIn(x, y) ? table[x][y] : null;
    }

    @Override
    public Cell get(String entry) {
        int x = xCell(entry);
        int y = yCell(entry);
        return isIn(x, y) ? table[x][y] : null;
    }

    @Override
    public String value(int x, int y) {
        return eval(x, y);
    }

    @Override
    public String eval(int x, int y) {
        if (!isIn(x, y)) {
            return Ex2Utils.ERR_FORM;
        }

        Cell cell = get(x, y);
        if (cell == null || cell.getData() == null) {
            return Ex2Utils.EMPTY_CELL;
        }

        if (cell.getType() == Ex2Utils.FORM) {
            Stack<String> computationPath = new Stack<>();
            Set<String> visitedCells = new HashSet<>();
            return evaluateFormula(cell.getData(), visitedCells, computationPath, x, y);
        }
        return cell.getData();
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
    public void save(String fileName) throws IOException {
        // Not implemented in this version
    }

    @Override
    public void load(String fileName) throws IOException {
        // Not implemented in this version
    }

    // Private helper methods
    private int xCell(String cellName) {
        if (cellName == null || cellName.isEmpty()) {
            return -1;
        }
        cellName = cellName.toUpperCase();
        return cellName.charAt(0) - 'A';
    }

    private int yCell(String cellName) {
        if (cellName == null || cellName.length() < 2) {
            return -1;
        }
        try {
            return Integer.parseInt(cellName.substring(1)) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int calculateDepth(int x, int y) {
        Cell cell = get(x, y);
        if (cell == null || cell.getType() != Ex2Utils.FORM) {
            return 0;
        }
        Set<String> visitedCells = new HashSet<>();
        Stack<String> computationPath = new Stack<>();
        return calculateFormulaDepth(cell.getData(), visitedCells, computationPath, x, y);
    }

    private int calculateFormulaDepth(String formula, Set<String> visitedCells, Stack<String> computationPath, int x, int y) {
        String cellName = (char) ('A' + x) + String.valueOf(y + 1);
        cellName = cellName.toUpperCase();

        if (computationPath.contains(cellName)) {
            return Ex2Utils.ERR_CYCLE_FORM;
        }

        computationPath.push(cellName);

        try {
            if (!SCell.isFormula(formula)) {
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
                        Cell refCell = get(refX, refY);
                        if (refCell != null) {
                            int depth = calculateFormulaDepth(refCell.getData(), visitedCells, computationPath, refX, refY);
                            if (depth == Ex2Utils.ERR_CYCLE_FORM) {
                                return Ex2Utils.ERR_CYCLE_FORM;
                            }
                            maxDepth = Math.max(maxDepth, depth);
                        }
                    }
                }
            }

            return maxDepth + 1;
        } finally {
            computationPath.pop();
        }
    }

    private String evaluateFormula(String formula, Set<String> visitedCells, Stack<String> computationPath, int x, int y) {
        String cellName = (char) ('A' + x) + String.valueOf(y + 1);
        cellName = cellName.toUpperCase();

        if (computationPath.contains(cellName)) {
            return Ex2Utils.ERR_CYCLE;
        }
        computationPath.push(cellName);

        try {
            SCell scell = new SCell(formula);
            Object result = scell.computeFormula(formula, this);

            if (result == null) {
                return Ex2Utils.ERR_FORM;
            } else if (result.equals(Ex2Utils.ERR_CYCLE_FORM)) {
                return Ex2Utils.ERR_CYCLE;
            } else if (result.equals(Ex2Utils.ERR_FORM_FORMAT)) {
                return Ex2Utils.ERR_FORM;
            } else if (result instanceof Double) {
                return result.toString();
            }

            return Ex2Utils.ERR_FORM;
        } finally {
            computationPath.pop();
        }
    }
}