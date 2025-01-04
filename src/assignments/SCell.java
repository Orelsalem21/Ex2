package assignments;

/**
 * Represents a single cell (`SCell`) in the spreadsheet managed by the `Ex2Sheet` class.
 */
public class SCell implements Cell {
    private String line;
    private int type;
    private int order;

    /**
     * Constructs a new SCell with the given data.
     * @param s The data to initialize the cell.
     */
    public SCell(String s) {
        setData(s);
    }

    /**
     * Returns the computational order (dependency depth) of this cell.
     * @return The order of the cell.
     */
    @Override
    public int getOrder() {
        return order;
    }

    /**
     * Converts the cell's data to a string representation.
     * @return The string representation of the cell's data.
     */
    @Override
    public String toString() {
        return getData();
    }

    /**
     * Sets the data of this cell and determines its type.
     * @param s The data to set.
     */
    @Override
    public void setData(String s) {
        if (s == null) {
            line = "";
            type = Ex2Utils.TEXT;
        } else {
            line = s.trim();
            if (isNumber(line)) {
                type = Ex2Utils.NUMBER;
            } else if (isForm(line)) {
                type = Ex2Utils.FORM;
            } else {
                type = Ex2Utils.TEXT;
            }
        }
    }

    /**
     * Gets the data stored in this cell.
     * @return The cell's data.
     */
    @Override
    public String getData() {
        return line;
    }

    /**
     * Gets the type of this cell.
     * @return The type of the cell.
     */
    @Override
    public int getType() {
        return type;
    }

    /**
     * Sets the type of this cell.
     * @param t The type to set.
     */
    @Override
    public void setType(int t) {
        type = t;
    }

    /**
     * Sets the computational order (dependency depth) of this cell.
     * @param t The order to set.
     */
    @Override
    public void setOrder(int t) {
        this.order = t;
    }

    /**
     * Checks if the given string is a valid number.
     * @param text The string to check.
     * @return True if the string is a valid number, false otherwise.
     */
    public static boolean isNumber(String text) {
        if (text == null || text.isBlank()) return false;
        try {
            Double.parseDouble(text.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if the given string is valid text (non-number, non-formula).
     * @param text The string to check.
     * @return True if the string is valid text, false otherwise.
     */
    public static boolean isText(String text) {
        if (text == null || text.isBlank()) return false;
        if (text.startsWith("=")) return false; // Exclude formulas
        return !isNumber(text); // Exclude numbers
    }

    /**
     * Checks if the given string is a valid formula.
     * @param text The string to check.
     * @return True if the string is a valid formula, false otherwise.
     */
    public static boolean isForm(String text) {
        if (text == null || text.isBlank()) return false;
        if (!text.startsWith("=")) return false;

        String formulaContent = text.substring(1).trim();
        if (formulaContent.isEmpty()) return false;

        if (isNumber(formulaContent)) return true;

        // Reject invalid formulas like "=()" or "=1+"
        if (formulaContent.startsWith("(") && formulaContent.endsWith(")") && formulaContent.length() == 2) return false;

        // Updated regex to handle more complex formulas
        String regex = "\\d+|\\(.*\\)|[A-Z][0-9]+|\\d+([+\\-*/]\\d+)*";
        return formulaContent.matches(regex);
    }

    /**
     * Computes the result of a given formula.
     * @param form The formula to compute.
     * @return The computed result as a Double, or null if invalid.
     */
    public static Double computeForm(String form) {
        if (form == null || form.isBlank() || !form.startsWith("=")) {
            return null;
        }

        String formulaContent = form.substring(1).trim();

        if (isNumber(formulaContent)) {
            return Double.parseDouble(formulaContent);
        }

        if (formulaContent.startsWith("(") && formulaContent.endsWith(")")) {
            return computeForm("=" + formulaContent.substring(1, formulaContent.length() - 1).trim());
        }

        int operatorIndex = getMainOperatorIndex(formulaContent);
        if (operatorIndex == -1) {
            return null;
        }

        char operator = formulaContent.charAt(operatorIndex);
        String left = formulaContent.substring(0, operatorIndex).trim();
        String right = formulaContent.substring(operatorIndex + 1).trim();

        Double leftValue = computeForm("=" + left);
        Double rightValue = computeForm("=" + right);

        if (leftValue == null || rightValue == null) {
            return null;
        }

        // Compute based on the operator
        return switch (operator) {
            case '+' -> leftValue + rightValue;
            case '-' -> leftValue - rightValue;
            case '*' -> leftValue * rightValue;
            case '/' -> rightValue != 0 ? leftValue / rightValue : null;
            default -> null;
        };
    }

    /**
     * Finds the index of the main operator in a formula string.
     * @param formula The formula string to analyze.
     * @return The index of the main operator, or -1 if none found.
     */
    private static int getMainOperatorIndex(String formula) {
        int depth = 0;
        int mainOperatorIndex = -1;

        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if ((c == '+' || c == '-' || c == '*' || c == '/') && depth == 0) {
                mainOperatorIndex = i;
            }
        }

        return mainOperatorIndex;
    }
}
