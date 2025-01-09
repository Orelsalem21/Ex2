package assignments.ex2;

import java.io.*;

class Spreadsheet implements Sheet {
    private Cell[][] cells;
    private int width;
    private int height;
    private final DepthCalculator depthCalculator;

    public Spreadsheet(int x, int y) {
        this.width = x;
        this.height = y;
        this.cells = new Cell[y][x];
        this.depthCalculator = new DepthCalculator();
        initializeCells();
    }

    private void initializeCells() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                cells[i][j] = new SCell(Ex2Utils.EMPTY_CELL);
            }
        }
    }


    @Override
    public Cell get(String cellName) {
        int[] coords = getCellCoordinates(cellName);
        return get(coords[0], coords[1]);
    }

    private int[] getCellCoordinates(String cellName) {
        if (cellName == null || cellName.isEmpty()) {
            return new int[]{-1, -1};
        }

        int x = Character.toUpperCase(cellName.charAt(0)) - 'A';
        int y;
        try {
            y = Integer.parseInt(cellName.substring(1)) - 1;
        } catch (NumberFormatException e) {
            y = -1;
        }

        return new int[]{x, y};
    }

    @Override
    public void set(int x, int y, String c) {
        if (isIn(x, y)) {
            cells[y][x] = new SCell(c);
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
                Object result = new SCell(cell.getData()).computeFormula(cell.getData(), this);
                return (result instanceof Double) ? result.toString() : Ex2Utils.ERR_FORM;
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
        return depthCalculator.calculateAllDepths();
    }

    private class DepthCalculator {
        private int[][] depths;
        private boolean[][] visited;

        public int[][] calculateAllDepths() {
            depths = new int[height][width];
            visited = new boolean[height][width];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (!visited[y][x]) {
                        calculateCellDepth(x, y);
                    }
                }
            }
            return depths;
        }

        private int calculateCellDepth(int x, int y) {
            if (!isIn(x, y) || visited[y][x]) {
                return 0;
            }

            visited[y][x] = true;
            Cell cell = cells[y][x];

            if (cell.getType() != Ex2Utils.FORM) {
                depths[y][x] = 0;
                return 0;
            }

            depths[y][x] = calculateFormulaDepth(cell.getData());
            return depths[y][x];
        }

        private int calculateFormulaDepth(String formula) {
            String expr = formula.substring(1).trim();
            int maxDepth = 0;

            for (int i = 0; i < expr.length(); i++) {
                if (Character.isLetter(expr.charAt(i))) {
                    StringBuilder cellRef = new StringBuilder();
                    while (i < expr.length() && Character.isLetterOrDigit(expr.charAt(i))) {
                        cellRef.append(expr.charAt(i++));
                    }
                    i--;

                    int[] coords = getCellCoordinates(cellRef.toString());
                    if (isIn(coords[0], coords[1])) {
                        maxDepth = Math.max(maxDepth,
                                calculateCellDepth(coords[0], coords[1]));
                    }
                }
            }

            return maxDepth + 1;
        }
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
    public String value(int x, int y) {
        return eval(x, y);
    }

    @Override
    public void load(String fileName) throws IOException {
        // Not implemented
    }

    @Override
    public void save(String fileName) throws IOException {
        // Not implemented
    }
}
