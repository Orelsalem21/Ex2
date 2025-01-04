package assignments.ex2;

import java.io.*;
import java.util.*;

public class Ex2Sheet implements Sheet {
    private final Cell[][] table;

    // Constructor for Ex2Sheet
    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell("");
            }
        }
        eval(); // Initial evaluation
    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        Cell c = get(x, y);
        return (c != null) ? c.toString() : Ex2Utils.EMPTY_CELL;
    }

    @Override
    public Cell get(int x, int y) {
        return isIn(x, y) ? table[x][y] : null;
    }

    @Override
    public Cell get(String cords) {
        // Parse coordinates manually since Ex2Utils cannot be edited
        int x = parseX(cords);
        int y = parseY(cords);
        return isIn(x, y) ? get(x, y) : null;
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
    public void eval() {
        int[][] dd = depth();
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                if (!String.valueOf(dd[x][y]).equals(Ex2Utils.ERR_CYCLE)) {
                    String evaluatedValue = eval(x, y);

                    // Optionally, update the cell's value based on the evaluation
                    if (evaluatedValue != null) {
                        set(x, y, evaluatedValue);
                    }
                }
            }
        }
    }

    @Override
    public boolean isIn(int x, int y) {
        return x >= 0 && x < width() && y >= 0 && y < height();
    }

    @Override
    public int[][] depth() {
        int[][] ans = new int[width()][height()];
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                ans[x][y] = computeDepth(x, y, new HashSet<>());
            }
        }
        return ans;
    }

    @Override
    public void load(String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            for (int i = 0; i < width(); i++) {
                String line = reader.readLine();
                if (line == null) break;
                String[] cells = line.split(",");
                for (int j = 0; j < Math.min(cells.length, height()); j++) {
                    set(i, j, cells[j]);
                }
            }
        }
    }

    @Override
    public void save(String fileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (int i = 0; i < width(); i++) {
                List<String> row = new ArrayList<>();
                for (int j = 0; j < height(); j++) {
                    row.add(value(i, j));
                }
                writer.write(String.join(",", row));
                writer.newLine();
            }
        }
    }

    @Override
    public String eval(int x, int y) {
        Cell cell = get(x, y);
        return (cell != null) ? cell.toString() : Ex2Utils.EMPTY_CELL;
    }

    // Helper method to compute depth with cycle detection
    private int computeDepth(int x, int y, Set<Cell> visited) {
        Cell cell = get(x, y);
        if (cell == null || cell.getType() == Ex2Utils.TEXT || cell.getType() == Ex2Utils.NUMBER) {
            return 0;
        }
        if (visited.contains(cell)) {
            return -1; // Replace with the correct integer error code based on the context, e.g., -1
        }
        visited.add(cell);
        return 1; // Placeholder for now
    }

    // Manual parsing for coordinates
    private int parseX(String cords) {
        if (cords == null || cords.isEmpty() || !Character.isLetter(cords.charAt(0))) return -1;
        return cords.charAt(0) - 'A';
    }

    private int parseY(String cords) {
        try {
            return Integer.parseInt(cords.substring(1)) - 1; // Assume 1-based indexing
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
