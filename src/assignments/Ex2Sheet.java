package assignments;

import java.io.IOException;

/**
 * Represents the spreadsheet and its operations. Updated to compute formulas.
 */
public class Ex2Sheet implements Sheet {
    private Cell[][] table;

    public Ex2Sheet(int x, int y) {
        table = new Cell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell(Ex2Utils.EMPTY_CELL); // Initialize each cell as an empty SCell
            }
        }
        eval();
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
    public Cell get(String cords) {
        int x = xCell(cords);
        int y = yCell(cords);
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
        SCell c = new SCell(s);
        table[x][y] = c;
    }

    @Override
    public void eval() {
        // Placeholder for evaluating all cells
    }

    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && yy >= 0 && xx < width() && yy < height();
    }

    @Override
    public int[][] depth() {
        int[][] ans = new int[width()][height()];
        return ans;
    }

    @Override
    public void load(String fileName) throws IOException {
        // Implement load logic
    }

    @Override
    public void save(String fileName) throws IOException {
        // Implement save logic
    }

    @Override
    public String eval(int x, int y) {
        Cell cell = get(x, y);
        if (cell == null) return Ex2Utils.EMPTY_CELL;

        if (cell.getType() == Ex2Utils.FORM) {
            Double result = SCell.computeForm(cell.getData());
            return result != null ? result.toString() : Ex2Utils.ERR_FORM;
        }
        return cell.getData();
    }

    private int xCell(String cellName) {
        if (cellName.length() < 2) return -1;
        return cellName.charAt(0) - 'A';
    }

    private int yCell(String cellName) {
        if (cellName.length() < 2) return -1;
        return Integer.parseInt(cellName.substring(1)) - 1;
    }
}
