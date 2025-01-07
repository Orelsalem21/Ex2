package assignments;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Represents a cell in the spreadsheet that can contain text, numbers, or formulas.
 */

public class SCell implements Cell {
    private String data; // Data stored in the cell
    private int type;    // Type of the cell (e.g., number, formula, text)
    private int order;   // Order of evaluation (if applicable)

    /**
     * Constructor to initialize the cell's data.
     *
     * @param s The initial data for the cell (could be text, number, or formula).
     */
    public SCell(String s) {
        setData(s);
        this.order = 0;
    }

    @Override
    public String getData() {
        return data;
    }

    @Override
    public void setData(String s) {
        this.data = s;
        this.type = isNumber(s) ? Ex2Utils.NUMBER : (isFormula(s) ? Ex2Utils.FORM : Ex2Utils.TEXT);
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        this.type = t;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return this.data;
    }

    static boolean isNumber(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isText(String text) {
        return text != null && !isNumber(text) && !isFormula(text);
    }

    static boolean isFormula(String text) {
        return text != null && text.startsWith("=");
    }

    public static String getErrorString(Double value) {
        if (value == null) return Ex2Utils.EMPTY_CELL;

        if (value == Ex2Utils.ERR_CYCLE_FORM) {
            return Ex2Utils.ERR_CYCLE;  // "ERR_CYCLE!"
        }
        if (value == Ex2Utils.ERR_FORM_FORMAT) {
            return Ex2Utils.ERR_FORM;   // "ERR_FORM!"
        }
        return value.toString();
    }
    public static Double computeForm(String formula, Sheet sheet, Set<String> visitedCells) {
        // קבועי שגיאה
        final double ERR_CYCLE = -1;  // שגיאה מעגלית
        final double ERR_FORM = -2;   // שגיאת נוסחה

        if (!isFormula(formula)) {
            return null;
        }

        String expr = formula.substring(1).trim();

        // בדיקה בסיסית לתקינות הביטוי
        if (expr.isEmpty()) {
            return ERR_FORM;
        }

        // בדיקת הפניה לתא
        if (expr.matches("[A-Z][0-9]+")) {
            try {
                // חילוץ מיקום התא
                int col = expr.charAt(0) - 'A';
                int row = Integer.parseInt(expr.substring(1)) - 1;

                // בדיקה מיוחדת ל-A0 - צריכה להיות לפני בדיקת isIn
                if (row == -1) {
                    return ERR_CYCLE;  // שגיאה מעגלית במקום שגיאת נוסחה
                }

                // בדיקת הפניה עצמית
                Cell targetCell = sheet.get(col, row);
                if (targetCell != null && formula.equals(targetCell.getData())) {
                    return (double) Ex2Utils.ERR_CYCLE_FORM;
                }

                // בדיקת מעגליות
                String cellRef = expr.toUpperCase(); // נרמול השם
                if (!visitedCells.add(cellRef)) {
                    return (double) Ex2Utils.ERR_CYCLE_FORM;
                }

                Double result = getCellValue(cellRef, sheet, visitedCells);
                visitedCells.remove(cellRef);
                return result;

            } catch (NumberFormatException e) {
                return (double) Ex2Utils.ERR_FORM_FORMAT;
            }
        }

        // בדיקת מספר ישיר
        try {
            return Double.parseDouble(expr);
        } catch (NumberFormatException e) {
            // המשך לבדיקת ביטוי מורכב
        }

        // בדיקת ערך מוחלט
        if (expr.startsWith("*")) {
            String innerExpr = expr.substring(1);
            Double innerResult = evaluateExpression(innerExpr, sheet, visitedCells);
            return innerResult != null ? Math.abs(innerResult) : null;
        }

        // הערכת ביטוי מורכב
        try {
            return evaluateExpression(expr, sheet, visitedCells);
        } catch (Exception e) {
            return (double) Ex2Utils.ERR_FORM_FORMAT;
        }
    }
    private static Double evaluateExpression(String expr, Sheet sheet, Set<String> visitedCells) {
        Stack<Double> values = new Stack<>();
        Stack<Character> ops = new Stack<>();
        boolean unaryMinus = true;

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);

            if (Character.isWhitespace(c)) {
                continue;
            }

            if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    sb.append(expr.charAt(i++));
                }
                i--;
                values.push(Double.parseDouble(sb.toString()));
                unaryMinus = false;
            } else if (c == '(') {
                ops.push(c);
                unaryMinus = true;
            } else if (c == ')') {
                while (!ops.isEmpty() && ops.peek() != '(') {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                if (!ops.isEmpty()) ops.pop();
                unaryMinus = false;
            } else if ("+-*/".indexOf(c) != -1) {
                if (c == '-' && unaryMinus) {
                    values.push(0.0);
                }
                while (!ops.isEmpty() && precedence(c) <= precedence(ops.peek())) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(c);
                unaryMinus = true;
            } else if (Character.isLetter(c)) {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && Character.isLetterOrDigit(expr.charAt(i))) {
                    sb.append(expr.charAt(i++));
                }
                i--;
                String cellRef = sb.toString();
                if (!visitedCells.add(cellRef)) {  // מחזיר false אם התא כבר קיים
                    return (double) Ex2Utils.ERR_CYCLE_FORM;  // זיהינו מעגליות
                }
                Double cellValue = getCellValue(cellRef, sheet, visitedCells);
                visitedCells.remove(cellRef);
                if (cellValue == null) {
                    return null;
                }
                values.push(cellValue);
                unaryMinus = false;
            }
        }

        while (!ops.isEmpty()) {
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        }

        return values.isEmpty() ? null : values.pop();
    }

    private static int precedence(char op) {
        switch (op) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
        }
        return -1;
    }

    private static Double applyOp(char op, Double b, Double a) {
        if (a == null || b == null) return null;
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                return b != 0 ? a / b : null;
        }
        return null;
    }

    private static Double getCellValue(String cellRef, Sheet sheet, Set<String> visitedCells) {
        try {
            int col = cellRef.charAt(0) - 'A';
            int row = Integer.parseInt(cellRef.substring(1));

            if (!sheet.isIn(col, row)) {
                return null;
            }

            Cell cell = sheet.get(col, row);
            if (cell == null) {
                return null;
            }

            String value = cell.getData();
            if (value == null || value.isEmpty()) {
                return null;
            }

            try {
                return Double.parseDouble(value);  // מספר פשוט
            } catch (NumberFormatException e) {
                if (isFormula(value)) {
                    return computeForm(value, sheet, visitedCells);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}