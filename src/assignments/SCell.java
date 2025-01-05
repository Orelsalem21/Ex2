package assignments;

/**
 * Represents a single cell (`SCell`) in the spreadsheet.
 */
public class SCell implements Cell {
    private String line; // The data stored in the cell
    private int type;    // The type of the cell (NUMBER, FORMULA, TEXT)
    private int order;   // The computational order (dependency depth) of the cell

    /**
     * Constructs a new SCell with the given data.
     * @param s The data to initialize the cell.
     */
    public SCell(String s) {
        setData(s);
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int t) {
        order = t;
    }

    @Override
    public String toString() {
        return getData();
    }

    @Override
    public void setData(String s) {
        line = (s == null) ? "" : s.trim();
        type = isNumber(line) ? Ex2Utils.NUMBER : (isFormula(line) ? Ex2Utils.FORM : Ex2Utils.TEXT);
    }

    @Override
    public String getData() {
        return line;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        type = t;
    }

    public static boolean isNumber(String text) {
        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

    public static boolean isFormula(String text) {
        return text != null && text.startsWith("=") && text.length() > 1;
    }

    public static Double computeFormula(String formula) {
        if (formula == null || !formula.startsWith("=")) return null;

        String expr = formula.substring(1).trim();

        // Remove unnecessary parentheses
        while (expr.startsWith("(") && expr.endsWith(")")) {
            expr = expr.substring(1, expr.length() - 1).trim();
        }

        if (isNumber(expr)) return Double.parseDouble(expr);

        int depth = 0, mainOpIndex = -1;
        char operator = 0;

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (c == '(') depth++;
            else if (c == ')') depth--;
            else if ("+-*/".indexOf(c) != -1 && depth == 0) {
                mainOpIndex = i;
                operator = c;
                break;
            }
        }

        if (mainOpIndex == -1) return null;

        String left = expr.substring(0, mainOpIndex).trim();
        String right = expr.substring(mainOpIndex + 1).trim();

        Double leftValue = computeFormula("=" + left);
        Double rightValue = computeFormula("=" + right);

        if (leftValue == null || rightValue == null) return null;

        if (operator == '+') return leftValue + rightValue;
        if (operator == '-') return leftValue - rightValue;
        if (operator == '*') return leftValue * rightValue;
        if (operator == '/') return rightValue != 0 ? leftValue / rightValue : null;

        return null;
    }

    /**
     * Wrapper for backward compatibility.
     * Computes the result of a formula using computeFormula.
     * @param formula The formula to compute.
     * @return The computed result as a Double, or null if invalid.
     */
    public static Double computeForm(String formula) {
        return computeFormula(formula);
    }
}
