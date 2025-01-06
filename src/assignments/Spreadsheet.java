package assignments;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Spreadsheet implements Sheet {
    private Cell[][] cells;
    private int width;
    private int height;

    public Spreadsheet(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                cells[i][j] = new SCell("");
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
        int x = cellNameToX(cellName);
        int y = cellNameToY(cellName);
        if (isIn(x, y)) {
            return cells[y][x];
        }
        return null;
    }

    @Override
    public String value(int x, int y) {
        return eval(x, y);
    }

    @Override
    public void set(int x, int y, String s) {
        if (isIn(x, y)) {
            cells[y][x] = new SCell(s);
        }
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
    public String eval(int x, int y) {
        if (!isIn(x, y)) {
            return Ex2Utils.ERR_FORM;
        }
        Cell cell = cells[y][x];
        if (cell.getType() == Ex2Utils.FORM) {
            Set<String> visitedCells = new HashSet<>();
            String cellValue = evaluateFormula(cell.getData(), visitedCells, x, y);
            if (cellValue.startsWith("שגיאה:")) {
                return cellValue;
            } else {
                return cellValue;
            }
        }
        return cell.getData();
    }

    private String evaluateFormula(String formula, Set<String> visitedCells, int x, int y) {
        String cellName = (char) ('A' + x) + String.valueOf(y + 1);
        if (visitedCells.contains(cellName)) {
            // מזהה הפניה מעגלית ומחזיר הודעת שגיאה
            return "שגיאה: הפניה מעגלית";
        }
        visitedCells.add(cellName);

        try {
            Double result = SCell.computeForm(formula, this, visitedCells);
            // מחזיר את התוצאה כמחרוזת או שגיאת נוסחה במידת הצורך
            return result != null ? result.toString() : "שגיאה: נוסחה";
        } catch (Exception e) {
            // מחזיר שגיאת נוסחה במקרה של כל שגיאה אחרת
            return "שגיאה: נוסחה";
        } finally {
            // מסיר את התא מהמערך של תאים שנבדקו, בין אם היתה שגיאה או לא
            visitedCells.remove(cellName);
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
                depths[y][x] = calculateDepth(x, y);
            }
        }
        return depths;
    }

    private int calculateDepth(int x, int y) {
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
            return 0;
        }
        visitedCells.add(cellName);

        int maxDepth = 0;
        // כאן צריך להוסיף לוגיקה לניתוח הנוסחה ומציאת העומק המקסימלי
        // זה תלוי באופן שבו הנוסחאות מיוצגות ומחושבות

        visitedCells.remove(cellName);
        return maxDepth + 1;
    }

    @Override
    public void load(String fileName) throws IOException {
        // יש להוסיף כאן לוגיקה לטעינת גיליון מקובץ
    }

    @Override
    public void save(String fileName) throws IOException {
        // יש להוסיף כאן לוגיקה לשמירת הגיליון לקובץ
    }

    private int cellNameToX(String cellName) {
        return cellName.charAt(0) - 'A';
    }

    private int cellNameToY(String cellName) {
        return Integer.parseInt(cellName.substring(1)) - 1;
    }
}