import java.util.ArrayList;

/**
 * Represents a single cell in a spreadsheet. This cell can store raw data and computed values,
 * and supports various types including text, numbers, and formulas.
 */
public class SCell implements Cell {

    private String line; // Raw data in the cell
    private String value = ""; // Evaluated value of the cell
    private int type; // Type of the cell: 1-Text, 2-Number, 3-Form, -2-Error Form, -1-Error Cycle/Error
    private Ex2Sheet sheet; // The spreadsheet this cell belongs to
    public CellEntry entry; // The cell entry associated with this cell
    private int order; // Order of computation
    private boolean isVisited = false; // Used for cycle detection
    private boolean isCalculating = false; // Indicates if the cell is being computed
    private int calculatedOrder = -2; // Cached computation order

    /**
     * Constructs an SCell with the specified data and associated sheet.
     * @param s The initial data to be stored in the cell.
     * @param sheet The spreadsheet this cell belongs to.
     */
    public SCell(String s, Ex2Sheet sheet) {
        this.sheet = sheet;
        setData(s);
    }


    /**
     * Sets the evaluated value of the cell.
     * @param v The value to set.
     */
    public void setValue(String v) {
        this.value = v;
    }

    /**
     * Associates a CellEntry with this cell.
     * @param e The CellEntry to associate.
     */
    public void setEntry(CellEntry e) {
        this.entry = e;
    }


    /**
     * Returns the string representation of the cell.
     * @return The raw data stored in the cell.
     */
    @Override
    public String toString() {
        return this.line;
    }

    /**
     * Sets the raw data of the cell.
     * @param s The data to set.
     */
    @Override
    public void setData(String s) {
        line = s;
    }

    /**
     * Retrieves the raw data of the cell.
     * @return The raw data as a string.
     */
    @Override
    public String getData() {
        return line;
    }

    /**
     * Gets the type of the cell.
     * @return The type of the cell as an integer.
     */
    @Override
    public int getType() {
        return type;
    }

    /**
     * Sets the type of the cell.
     * @param t The type to set.
     */
    @Override
    public void setType(int t) {
        type = t;
    }

    /**
     * Sets the computation order of the cell.
     * @param t The computation order to set.
     */
    @Override
    public void setOrder(int t) {
        this.order = t;
    }

    /**
     * Retrieves the computation order of the cell.
     * @return The computation order as an integer.
     */
    @Override
    public int getOrder() {
        return this.order;
    }


    /**
     * Resets the state of the cell used for cycle detection and calculation.
     * This includes resetting the visited flag, calculation flag, and cached order.
     */
    public void resetVisited() {
        isVisited = false;
        isCalculating = false;
        calculatedOrder = -2;
    }

    /**
     * Detects if there is a cyclic dependency starting from this cell.
     * @param path A list representing the current path of cell references.
     * @return True if a cycle is detected, false otherwise.
     */
    public boolean detectCycle(ArrayList<String> path) {
        if (isCalculating) {
            return true;
        }
        if (isVisited) {
            return false;
        }

        isCalculating = true;
        if (entry != null) {
            path.add(this.entry.getIndex());
        }

        ArrayList<SCell> refs = getReferences(this.getData());
        for (SCell ref : refs) {
            if (ref != null && ref.detectCycle(path)) {
                return true;
            }
        }

        if (entry != null) {
            path.remove(this.entry.getIndex());
        }
        isCalculating = false;
        isVisited = true;
        return false;
    }

    /**
     * Calculates the computation order of this cell based on its dependencies.
     * The order is determined recursively by analyzing dependent cells.
     * @return The computation order, or -1 if a cycle is detected, or -2 for errors.
     */
    public int calcOrder() {
        if (calculatedOrder != -2) {
            return calculatedOrder;
        }

        if (isCalculating) {
            return -1;
        }

        String str = this.getData();
        if (str == null || str.isEmpty() || type == Ex2Utils.TEXT || type == Ex2Utils.NUMBER) {
            calculatedOrder = 0;
            return 0;
        }
        if (type == Ex2Utils.ERR_FORM_FORMAT) {
            calculatedOrder = -2;
            return -2;
        }
        if (type == Ex2Utils.ERR_CYCLE_FORM) {
            calculatedOrder = -1;
            return -1;
        }

        isCalculating = true;
        ArrayList<SCell> refs = getReferences(str);
        ArrayList<Integer> depends = new ArrayList<>();

        for (SCell ref : refs) {
            if (ref == null) {
                calculatedOrder = -2;
                isCalculating = false;
                return -2;
            }
            int depend = ref.calcOrder();
            if (depend == -1) {
                calculatedOrder = -1;
                isCalculating = false;
                return -1;
            }
            if (depend == -2) {
                calculatedOrder = -2;
                isCalculating = false;
                return -2;
            }
            depends.add(depend);
        }

        isCalculating = false;

        if (depends.isEmpty()) {
            calculatedOrder = 0;
            return 0;
        }

        int max = 0;
        for (Integer depend : depends) {
            if (depend > max) {
                max = depend;
            }
        }
        calculatedOrder = max + 1;
        return max + 1;
    }

    /**
     * Validates if the given string represents a valid formula.
     * A formula must start with '=' and follow syntax rules for numbers, operators, and cell references.
     * @param str The string to validate.
     * @return True if the string is a valid formula, false otherwise.
     */
    public boolean isForm(String str) {
        if (str.isEmpty() || str.length() == 1) {
            return false;
        }
        str = str.replaceAll(" ", "").toUpperCase();
        if (str.charAt(0) != '=') {
            return false;
        }

        if (str.indexOf("=") != str.lastIndexOf("=")) {
            return false;
        }
        str = str.substring(1);
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException _) {
        }
        for (int i = 0; i < str.length(); i++) {
            if (!validChars(str.charAt(i)) && !isLetter(str.charAt(i)) && !isDigit(str.charAt(i))) {
                return false;
            }
            if (isLetter(str.charAt(i))) {
                int index = closestOpOrBrackets(str, i);
                if (index == -1) {
                    return false;
                }
                CellEntry current = new CellEntry(str.substring(i, index));
                if (!current.isValid()) {
                    return false;
                }
            }
        }

        int bracketsCount = 0;
        char c0 = str.charAt(0);
        char cEnd = str.charAt(str.length() - 1);
        if ((isOp(c0) && c0 != '-') || isOp(cEnd) || isLetter(cEnd)) {
            return false;
        }
        for (int i = 0; i < str.length() - 1; i++) {
            if (bracketsCount < 0) {
                return false;
            }
            char c = str.charAt(i);
            char cPlus = str.charAt(i + 1);
            if (isOp(c)) {
                if (isOp(cPlus)) {
                    return false;
                }
                if (!isDigit(cPlus) && !isLetter(cPlus) && cPlus != '(') {
                    return false;
                }
            }
            if (c == '(') {
                if (i != 0) {
                    if (isDigit(str.charAt(i - 1)) || isLetter(str.charAt(i - 1)) || str.charAt(i - 1) == '.') {
                        return false;
                    }
                }
                bracketsCount++;
                if (cPlus == ')') {
                    return false;
                }
            } else if (c == ')') {
                if (isDigit(str.charAt(i + 1)) || isLetter(str.charAt(i + 1)) || str.charAt(i + 1) == '.') {
                    return false;
                }
                bracketsCount--;
            }

        }
        int lastIndex = str.length() - 1;
        if (str.charAt(lastIndex) == '(') {
            bracketsCount++;
        } else if (str.charAt(lastIndex) == ')') {
            bracketsCount--;
        }
        return bracketsCount == 0;
    }


    /**
     * Computes the value of a formula expression recursively.
     * Supports operations such as addition, subtraction, multiplication, and division.
     * Handles references to other cells and checks for cycles and errors.
     *
     * @param expression The formula expression to compute.
     * @return The computed value of the formula.
     * @throws ErrorForm If the formula is invalid or cannot be parsed.
     * @throws ErrorCycle If a cyclic dependency is detected.
     */
    public double computeForm(String expression) throws ErrorForm, ErrorCycle {
        expression = expression.replaceAll(" ", "").toUpperCase();
        if (expression.isEmpty()) {
            return 0;
        }
        if (expression.charAt(0) == '=') {
            expression = expression.substring(1);
        }
        if (noOps(expression)) {
            boolean mins = false;
            if (expression.charAt(0) == '-') {
                mins = true;
                expression = expression.substring(1);
            }
            if (isLetter(expression.charAt(0))) {
                SCell cell = (SCell) this.sheet.get(expression);
                if (cell == null || cell.getData().isEmpty()) {
                    throw new ErrorForm("NoCellFound");
                }

                // First check for cycle
                ArrayList<String> cyclePath = new ArrayList<>();
                if (cell.detectCycle(cyclePath)) {
                    throw new ErrorCycle("ErrorCycle");
                }

                // Check cell type and handle accordingly
                int cellType = cell.getType();
                if (cellType == Ex2Utils.ERR_CYCLE_FORM) {
                    throw new ErrorCycle("ErrorCycle");
                } else if (cellType == Ex2Utils.ERR_FORM_FORMAT) {
                    throw new ErrorForm("ErrorForm");
                } else if (cellType == Ex2Utils.TEXT) {
                    throw new ErrorForm("TextCell");
                }

                // For number or form types, compute the value
                String cellData = cell.getData();
                if (cellData.isEmpty()) {
                    return 0;
                }

                try {
                    double result;
                    if (cellType == Ex2Utils.NUMBER) {
                        result = Double.parseDouble(cellData);
                    } else {
                        result = cell.computeForm(cellData);
                    }
                    return mins ? -result : result;
                } catch (NumberFormatException e) {
                    throw new ErrorForm("InvalidNumber");
                }
            }

            // Handle pure number case
            expression = expression.replace("(", "").replace(")", "");
            try {
                return Double.parseDouble(expression);
            } catch (NumberFormatException e) {
                throw new ErrorForm("InvalidNumber");
            }
        }

        while (expression.contains("(")) {
            int indexOfClosed = correctClosedBracket(expression, expression.indexOf('('));
            String subStrAfterClose = expression.substring(indexOfClosed + 1);
            expression = expression.substring(0, expression.indexOf("(")) +
                    String.valueOf(computeForm(expression.substring(expression.indexOf("(") + 1, indexOfClosed)))
                    + subStrAfterClose;
        }

        int additionIndex = expression.indexOf("+");
        if (additionIndex != -1) {
            return computeForm(expression.substring(0, additionIndex)) + computeForm(expression.substring(additionIndex + 1));
        }

        int subIndex = expression.indexOf("-");
        if (subIndex != -1) {
            if (subIndex == 0 && !isOp(expression.charAt(subIndex + 1))) {
                return -computeForm(expression.substring(subIndex + 1));
            } else if (!isOp(expression.charAt(subIndex - 1))) {
                return computeForm(expression.substring(0, subIndex)) - computeForm(expression.substring(subIndex + 1));
            }
        }

        int multiIndex = expression.indexOf("*");
        if (multiIndex != -1) {
            return computeForm(expression.substring(0, multiIndex)) * computeForm(expression.substring(multiIndex + 1));
        }

        int divisionIndex = expression.indexOf("/");
        if (divisionIndex != -1) {
            double divisor = computeForm(expression.substring(divisionIndex + 1));
            if (divisor == 0) {
                return Double.POSITIVE_INFINITY;
            }
            System.out.println("Computing formula for: " + this.entry.getIndex());
            System.out.println("Expression: " + expression);
            return computeForm(expression.substring(0, divisionIndex)) / divisor;
        }

        throw new ErrorForm("InvalidExpression");
    }

    /**
     * Checks if a character is a valid operator.
     * @param c The character to check.
     * @return True if the character is an operator, false otherwise.
     */
    public static boolean isOp(char c) {
        String ops = "+-/*";
        return ops.contains(String.valueOf(c));
    }

    /**
     * Checks if a string contains no valid operators other than a possible leading '-'.
     * @param str The string to check.
     * @return True if no operators are present, false otherwise.
     */
    public static boolean noOps(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (isOp(str.charAt(i))) {
                if (str.charAt(i) != '-') {
                    return false;
                }
            }
        }
        if (str.indexOf("-") != str.lastIndexOf("-")) {
            return false;
        }
        if (str.contains("-")) {
            if (str.indexOf("-") != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds the index of the closing bracket for a given opening bracket in the string.
     * @param str The string containing brackets.
     * @param index The index of the opening bracket.
     * @return The index of the matching closing bracket, or -1 if unmatched.
     */
    public static int correctClosedBracket(String str, int index) {
        int count = 0;
        for (int i = index; i < str.length(); i++) {
            if (str.charAt(i) == '(') {
                count++;
            } else if (str.charAt(i) == ')') {
                count--;
            }
            if (count == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if the given character is a valid operator or special character.
     * @param c The character to check.
     * @return True if the character is valid, false otherwise.
     */
    public static boolean validChars(char c) {
        String validChars = "+-*/.()";
        return validChars.indexOf(c) != -1;
    }

    /**
     * Checks if the given character is an uppercase English letter.
     * @param c The character to check.
     * @return True if the character is an uppercase letter, false otherwise.
     */
    public static boolean isLetter(char c) {
        String ABC = "ABCDEFHIGKLMNOPQRSTUVWXYZ";
        return ABC.indexOf(c) != -1;
    }

    /**
     * Checks if the given character is a numeric digit.
     * @param c The character to check.
     * @return True if the character is a digit, false otherwise.
     */
    public static boolean isDigit(char c) {
        String digits = "0123456789";
        return digits.indexOf(c) != -1;
    }

    /**
     * Finds the closest operator or closing bracket starting from a given index.
     * @param str The string to search.
     * @param start The starting index for the search.
     * @return The index of the closest operator or bracket, or the string's length if none found.
     */
    public static int closestOpOrBrackets(String str, int start) {
        int i;
        for (i = start; i < str.length(); i++) {
            if (isOp(str.charAt(i)) || str.charAt(i) == ')') {
                return i;
            }
        }
        return i;
    }

    /**
     * Checks if the given string represents a valid number.
     * @param s The string to check.
     * @return True if the string is a number, false otherwise.
     */
    public boolean isNumber(String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException _) {
            return false;
        }
    }

    /**
     * Determines if the given string represents a valid text cell.
     * A text cell is neither a number nor a valid formula.
     * @param str The string to check.
     * @return True if the string is text, false otherwise.
     */
    public boolean isText(String str) {
        if (!this.isNumber(str) && !this.isForm(str)) {
            return true;
        }
        return false;
    }

    /**
     * Extracts a list of referenced cells from a formula string.
     * @param str The formula string to analyze.
     * @return A list of referenced SCell objects.
     */
    public ArrayList<SCell> getReferences(String str) {
        ArrayList<SCell> references = new ArrayList<>();
        if (str == null || str.isEmpty()) {
            return references;
        }

        str = str.replaceAll(" ", "").toUpperCase();
        if (str.charAt(0) == '=') {
            str = str.substring(1);
        }

        for (int i = 0; i < str.length(); i++) {
            if (isLetter(str.charAt(i))) {
                int endIndex = closestOpOrBrackets(str, i);
                String cellRef = str.substring(i, endIndex);
                CellEntry entry = new CellEntry(cellRef);
                if (entry.isValid()) {
                    SCell refCell = (SCell) this.sheet.get(entry.getIndex());
                    if (refCell != null) {
                        references.add(refCell);
                    }
                }
                i = endIndex - 1;
            }
        }
        return references;
    }

    /**
     * Represents an exception indicating an error in formula parsing or evaluation.
     */
    public class ErrorForm extends Exception {
        public ErrorForm(String errorMessage) {
            super(errorMessage);
        }
    }

    /**
     * Represents an exception indicating a cyclic dependency in cell references.
     */
    public class ErrorCycle extends Exception {
        public ErrorCycle(String errorMessage) {
            super(errorMessage);
        }
    }
}