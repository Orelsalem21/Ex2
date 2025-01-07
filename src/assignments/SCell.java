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

    /**
     * Gets the data stored in the cell.
     *
     * @return the data stored in the cell (text, number, or formula).
     */
    @Override
    public String getData() {
        return data;
    }

    /**
     * Sets the data for the cell and determines the type (text, number, or formula).
     *
     * @param s The new data to set for the cell.
     */
    @Override
    public void setData(String s) {
        this.data = s;
        this.type = isNumber(s) ? Ex2Utils.NUMBER : (isFormula(s) ? Ex2Utils.FORM : Ex2Utils.TEXT);
    }

    /**
     * Gets the type of the cell.
     *
     * @return the type of the cell (number, formula, or text).
     */
    @Override
    public int getType() {
        return type;
    }

    /**
     * Sets the type of the cell.
     *
     * @param t The new type of the cell (number, formula, or text).
     */
    @Override
    public void setType(int t) {
        this.type = t;
    }

    /**
     * Gets the order of evaluation for the cell.
     *
     * @return the evaluation order.
     */
    @Override
    public int getOrder() {
        return order;
    }

    /**
     * Sets the order of evaluation for the cell.
     *
     * @param order The new order for evaluation.
     */
    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Returns the string representation of the cell's data.
     *
     * @return the data stored in the cell as a string.
     */
    @Override
    public String toString() {
        return this.data;
    }

    // Private helper methods

    /**
     * Checks if the provided string is a valid number.
     *
     * @param text The string to check.
     * @return true if the string represents a number, false otherwise.
     */
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
    /**
     * Checks if the provided string represents text (not a number or formula).
     *
     * @param text The string to check.
     * @return true if the string represents text, false otherwise.
     */
    public static boolean isText(String text) {
        // Check if the string is not null and is not a number or formula
        return text != null && !isNumber(text) && !isFormula(text);
    }
    /**
     * Checks if the provided string is a valid formula (starts with "=").
     *
     * @param text The string to check.
     * @return true if the string is a formula, false otherwise.
     */
    static boolean isFormula(String text) {
        return text != null && text.startsWith("=");
    }

    // Formula evaluation methods

    /**
     * Computes the result of a formula, given the formula string and a reference to the sheet.
     *
     * @param formula The formula to compute.
     * @param sheet The sheet that contains the cell references.
     * @param visitedCells A set of cells that have already been evaluated to prevent circular dependencies.
     * @return The computed result of the formula.
     */
    public static Double computeForm(String formula, Sheet sheet, Set<String> visitedCells) {
        if (!isFormula(formula)) {
            return null;
        }
        String expr = formula.substring(1).trim();

        if (expr.matches("[A-Z][0-9]+")) {
            return getCellValue(expr, sheet, visitedCells);
        }

        try {
            return Double.parseDouble(expr);
        } catch (NumberFormatException e) {
            // Continue to parse further
        }

        // Check if the expression starts with a unary minus
        if (expr.startsWith("-")) {
            String innerExpr = expr.substring(1);
            Double innerResult = evaluateExpression(innerExpr, sheet, visitedCells);
            return innerResult != null ? -innerResult : null;
        }

        return evaluateExpression(expr, sheet, visitedCells);
    }

    /**
     * Evaluates a mathematical expression represented as a string.
     *
     * @param expr The expression to evaluate.
     * @param sheet The sheet that contains the cell references.
     * @param visitedCells A set of cells that have already been evaluated to prevent circular dependencies.
     * @return The result of the evaluated expression.
     */
    private static Double evaluateExpression(String expr, Sheet sheet, Set<String> visitedCells) {
        if (expr.matches("[A-Z][0-9]+")) {
            return getCellValue(expr, sheet, visitedCells);
        }

        Stack<Double> values = new Stack<>();
        Stack<Character> ops = new Stack<>();

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (Character.isWhitespace(c)) continue;

            if (Character.isDigit(c) || c == '.') {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    sb.append(expr.charAt(i++));
                }
                i--;
                values.push(Double.parseDouble(sb.toString()));
            } else if (c == '(') {
                ops.push(c);
            } else if (c == ')') {
                while (!ops.isEmpty() && ops.peek() != '(') {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                if (!ops.isEmpty()) ops.pop();
            } else if ("+-*/".indexOf(c) != -1) {
                while (!ops.isEmpty() && precedence(c) <= precedence(ops.peek())) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(c);
            } else if (Character.isLetter(c)) {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && Character.isLetterOrDigit(expr.charAt(i))) {
                    sb.append(expr.charAt(i++));
                }
                i--;
                String cellRef = sb.toString();
                Double cellValue = getCellValue(cellRef, sheet, visitedCells);
                if (cellValue == null) return null;
                values.push(cellValue);
            }
        }

        while (!ops.isEmpty()) {
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        }

        return values.isEmpty() ? null : values.pop();
    }

    /**
     * Determines the precedence of an operator.
     *
     * @param op The operator to check.
     * @return The precedence of the operator.
     */
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

    /**
     * Applies an operator to two operands and returns the result.
     *
     * @param op The operator to apply.
     * @param b The second operand.
     * @param a The first operand.
     * @return The result of the operation.
     */
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

    /**
     * Retrieves the value of a cell given its reference.
     *
     * @param cellRef The reference to the cell (e.g., "A1").
     * @param sheet The sheet containing the cell.
     * @param visitedCells A set of cells that have already been evaluated to prevent circular dependencies.
     * @return The value of the cell, or null if the value cannot be determined.
     */
    private static Double getCellValue(String cellRef, Sheet sheet, Set<String> visitedCells) {
        int col = cellRef.charAt(0) - 'A';
        int row = Integer.parseInt(cellRef.substring(1));
        String value = sheet.value(col, row);
        if (row < 1 || row > 99) {
            throw new IllegalArgumentException("ERR_FORM Invalid cell reference:.");
        }

        if (value != null && !value.isEmpty()) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                if (isFormula(value)) {
                    return computeForm(value, sheet, visitedCells);
                }
            }
        }
        return null;
    }
}
