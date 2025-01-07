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

    // Public methods (override from Cell interface)

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

    // Private helper methods

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

    public static Double computeForm(String formula, Sheet sheet, Set<String> visitedCells) {
        if (!isFormula(formula)) {
            return null;
        }
        String expr = formula.substring(1);

        if (expr.matches("[A-Z][0-9]+")) {
            return getCellValue(expr, sheet, visitedCells);
        }

        try {
            return Double.parseDouble(expr);
        } catch (NumberFormatException e) {
            // Continue to parse further
        }

        return evaluateExpression(expr, sheet, visitedCells);
    }

    private static Double evaluateExpression(String expr, Sheet sheet, Set<String> visitedCells) {
        Stack<Double> values = new Stack<>();
        Stack<Character> ops = new Stack<>();
        boolean unaryMinus = true; // Track if we are processing a unary minus

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
                    // Handle unary minus
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
                Double cellValue = getCellValue(cellRef, sheet, visitedCells);
                if (cellValue == null) {
                    throw new IllegalArgumentException("Invalid cell reference or circular dependency detected: " + cellRef);
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
        if (visitedCells.contains(cellRef)) {
            throw new IllegalArgumentException("Circular dependency detected: " + cellRef);
        }

        int col = cellRef.charAt(0) - 'A';
        int row = Integer.parseInt(cellRef.substring(1)) ; // Adjust row index correctly

        if (row < 0 || col < 0) {
            throw new IllegalArgumentException("Invalid cell reference: " + cellRef);
        }

        String value = sheet.value(col, row);
        if (value != null && !value.isEmpty()) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                if (isFormula(value)) {
                    visitedCells.add(cellRef);
                    Double result = computeForm(value, sheet, visitedCells);
                    visitedCells.remove(cellRef);
                    return result;
                }
            }
        }
        return null;
    }
}
