import java.util.ArrayList;
import java.util.Stack;

/**
 * Represents a cell in the spreadsheet with additional functionality for handling formulas and cycles.
 * Supports various cell types (Text, Number, Formula) and handles references and cycle detection.
 */
public class SCell implements Cell {
    private String line;
    private String value = "";
    private int type;
    // 1 - Text, 2 - Number, 3 - Form, -2 - Err form, -1 - Err cycle\Err
    private Ex2Sheet sheet;
    public CellEntry entry;
    private int order;
    private boolean isVisited = false;
    private boolean isCalculating = false;
    private int calculatedOrder = -2;

    /**
     * Constructs an SCell with the specified data and associated sheet.
     *
     * @param s the initial data for the cell.
     * @param sheet the associated spreadsheet.
     */
    public SCell(String s, Ex2Sheet sheet) {
        this.sheet = sheet;
        setData(s);
    }

    /**
     * Sets the value of the cell.
     *
     * @param v the value to set.
     */
    public void setValue(String v){
        this.value = v;
    }

    /**
     * Sets the entry associated with the cell.
     *
     * @param e the CellEntry to associate with the cell.
     */
    public void setEntry(CellEntry e){
        this.entry = e;
    }

    /**
     * Returns a string representation of the cell's data.
     *
     * @return the string data of the cell.
     */
    @Override
    public String toString() {
        return this.line;
    }

    /**
     * Sets the data for the cell.
     *
     * @param s the data to set.
     */
    @Override
    public void setData(String s) {
        line = s;
    }

    /**
     * Retrieves the data of the cell.
     *
     * @return the data of the cell.
     */
    @Override
    public String getData() {
        return line;
    }

    /**
     * Retrieves the type of the cell (Text, Number, Formula, Error).
     *
     * @return the type of the cell.
     */
    @Override
    public int getType() {
        return type;
    }

    /**
     * Sets the type of the cell.
     *
     * @param t the type to set.
     */
    @Override
    public void setType(int t) {
        type = t;
    }

    /**
     * Sets the order of the cell.
     *
     * @param t the order to set.
     */
    @Override
    public void setOrder(int t) {
        this.order = t;
    }

    /**
     * Retrieves the order of the cell.
     *
     * @return the order of the cell.
     */
    @Override
    public int getOrder() {
        return this.order;
    }

    /**
     * Resets the visited state of the cell, allowing it to be recalculated.
     */
    public void resetVisited() {
        isVisited = false;
        isCalculating = false;
        calculatedOrder = -2;
    }

    /**
     * Detects if there is a cycle in the cell's references.
     *
     * @param path the current path of cell references.
     * @return true if a cycle is detected, false otherwise.
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
     * Calculates the order of the cell's references for evaluation.
     *
     * @return the calculated order, or -1 if a cycle is detected, or -2 if an error occurs.
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
     * Determines if the given string is a valid formula.
     *
     * @param str the string to check.
     * @return true if the string is a valid formula, false otherwise.
     */
    public boolean isForm(String str) {
        if (str.isEmpty() || str.length() == 1) {
            return false;
        }
        str = str.replaceAll(" ", "");
        str = str.toUpperCase();

        if (str.charAt(0) != '=') {
            return false;
        }

        if (str.indexOf("=") != str.lastIndexOf("=")) {
            return false;
        }

        str = str.substring(1);

        // בדיקה שהנוסחה לא מסתיימת באופרטור
        if (str.isEmpty() || isOp(str.charAt(str.length() - 1))) {
            return false;
        }

        // בדיקה לסוגריים פתוחים או לא מאוזנים
        if (str.contains("(")) {
            String noQuotes = str.replaceAll("[^()]", "");
            if (noQuotes.length() % 2 != 0) {
                return false;
            }
        }

        // Validate characters and cell references
        for (int i = 0; i < str.length(); i++) {
            char currentChar = str.charAt(i);

            // בדיקת תווים לא חוקיים
            if (!validChars(currentChar) && !isLetter(currentChar) && !isDigit(currentChar)
                    && currentChar != '(' && currentChar != ')') {
                return false;
            }

            // בדיקת תקינות תאים
            if (isLetter(currentChar)) {
                int index = closestOpOrBrackets(str, i);
                if (index == -1 || index == i) {
                    return false;
                }
                String cellRef = str.substring(i, index);
                CellEntry current = new CellEntry(cellRef);
                if (!current.isValid()) {
                    return false;
                }
                i = index - 1;  // קפיצה קדימה לסוף הפניית התא
                continue;
            }

            // בדיקת אופרטורים רצופים
            if (isOp(currentChar) && i < str.length() - 1) {
                char nextChar = str.charAt(i + 1);
                if (isOp(nextChar) && currentChar != '-') {
                    return false;
                }
            }
        }

        // בדיקת סוגריים וסדר פעולות
        int bracketsCount = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c == '(') {
                bracketsCount++;
                // בדיקה לסוגריים ריקים
                if (i < str.length() - 1 && str.charAt(i + 1) == ')') {
                    return false;
                }
            } else if (c == ')') {
                bracketsCount--;
                // בדיקה לסוגריים לא מאוזנים
                if (bracketsCount < 0) {
                    return false;
                }
            }

            // בדיקת תקינות אופרטורים ליד סוגריים
            if (i < str.length() - 1) {
                char next = str.charAt(i + 1);
                if (isOp(c) && (next == ')' || isOp(next))) {
                    return false;
                }
            }
        }

        // בדיקה סופית לסוגריים
        if (bracketsCount != 0) {
            return false;
        }

        // בדיקה שהנוסחה לא מסתיימת באופן לא תקין
        String trimmed = str.trim();
        if (trimmed.endsWith("(") || trimmed.endsWith("+") ||
                trimmed.endsWith("-") || trimmed.endsWith("*") ||
                trimmed.endsWith("/")) {
            return false;
        }

        return true;
    }

    /**
     * Computes the result of a formula expression.
     *
     * @param expression the formula to compute.
     * @return the result of the formula.
     * @throws ErrorForm if the formula is invalid.
     * @throws ErrorCycle if a cycle is detected in the formula.
     */
    public double computeForm(String expression) throws ErrorForm, ErrorCycle {
        expression = expression.replaceAll(" ", "").toUpperCase();
        if (expression.isEmpty()) {
            throw new ErrorForm("EmptyExpression");
        }
        if (expression.charAt(0) == '=') {
            expression = expression.substring(1);
        }

        // Handle binary operations first (before brackets)
        int lastIndex = -1;
        char lastOp = ' ';

        // Find last valid operator (ignoring operators inside brackets)
        int bracketCount = 0;
        for (int i = expression.length() - 1; i >= 0; i--) {
            char c = expression.charAt(i);
            if (c == ')') bracketCount++;
            if (c == '(') bracketCount--;
            if (bracketCount == 0 && (c == '+' || c == '-' || c == '*' || c == '/')) {
                // For minus, check if it's a binary operator and not unary
                if (c == '-' && i > 0 && !isOp(expression.charAt(i-1))) {
                    lastIndex = i;
                    lastOp = c;
                    break;
                }
                // For other operators
                if (c != '-') {
                    lastIndex = i;
                    lastOp = c;
                    break;
                }
            }
        }

        // Handle binary operations
        if (lastIndex >= 0) {
            String leftPart = expression.substring(0, lastIndex);
            String rightPart = expression.substring(lastIndex + 1);

            // If left part is empty (like in case of "10-3"), treat it as "0"
            if (leftPart.isEmpty()) leftPart = "0";

            switch(lastOp) {
                case '+': return computeForm(leftPart) + computeForm(rightPart);
                case '-': return computeForm(leftPart) - computeForm(rightPart);
                case '*': return computeForm(leftPart) * computeForm(rightPart);
                case '/':
                    double divisor = computeForm(rightPart);
                    if (divisor == 0) return Double.POSITIVE_INFINITY;
                    return computeForm(leftPart) / divisor;
            }
        }

        // Handle brackets
        if (expression.contains("(")) {
            int openIndex = expression.indexOf("(");
            int closeIndex = correctClosedBracket(expression, openIndex);
            if (closeIndex == -1) {
                throw new ErrorForm("InvalidBrackets");
            }

            String before = expression.substring(0, openIndex);
            String inside = expression.substring(openIndex + 1, closeIndex);
            String after = expression.substring(closeIndex + 1);

            double resultInside = computeForm(inside);

            // Handle negative before parentheses
            if (before.endsWith("-")) {
                resultInside = -resultInside;
                before = before.substring(0, before.length() - 1);
            }

            expression = before + resultInside + after;
            return computeForm(expression);
        }

        if (noOps(expression)) {
            // Handle single negative number or negative cell reference
            if (expression.charAt(0) == '-') {
                String rest = expression.substring(1);
                if (isLetter(rest.charAt(0))) {
                    SCell cell = (SCell) this.sheet.get(rest);
                    if (cell == null || cell.getData().isEmpty()) {
                        throw new ErrorForm("NoCellFound");
                    }

                    ArrayList<String> cyclePath = new ArrayList<>();
                    if (cell.detectCycle(cyclePath)) {
                        throw new ErrorCycle("ErrorCycle");
                    }

                    int cellType = cell.getType();
                    if (cellType == Ex2Utils.ERR_CYCLE_FORM) {
                        throw new ErrorCycle("ErrorCycle");
                    } else if (cellType == Ex2Utils.ERR_FORM_FORMAT) {
                        throw new ErrorForm("ErrorForm");
                    } else if (cellType == Ex2Utils.TEXT) {
                        throw new ErrorForm("TextCell");
                    }

                    if (cellType == Ex2Utils.NUMBER) {
                        return -Double.parseDouble(cell.getData());
                    } else {
                        return -cell.computeForm(cell.getData());
                    }
                }
                try {
                    return -Double.parseDouble(rest);
                } catch (NumberFormatException e) {
                    throw new ErrorForm("InvalidNumber");
                }
            }

            if (isLetter(expression.charAt(0))) {
                SCell cell = (SCell) this.sheet.get(expression);
                if (cell == null || cell.getData().isEmpty()) {
                    throw new ErrorForm("NoCellFound");
                }

                ArrayList<String> cyclePath = new ArrayList<>();
                if (cell.detectCycle(cyclePath)) {
                    throw new ErrorCycle("ErrorCycle");
                }

                int cellType = cell.getType();
                if (cellType == Ex2Utils.ERR_CYCLE_FORM) {
                    throw new ErrorCycle("ErrorCycle");
                } else if (cellType == Ex2Utils.ERR_FORM_FORMAT) {
                    throw new ErrorForm("ErrorForm");
                } else if (cellType == Ex2Utils.TEXT) {
                    throw new ErrorForm("TextCell");
                }

                if (cellType == Ex2Utils.NUMBER) {
                    return Double.parseDouble(cell.getData());
                } else {
                    return cell.computeForm(cell.getData());
                }
            }

            try {
                return Double.parseDouble(expression);
            } catch (NumberFormatException e) {
                throw new ErrorForm("InvalidNumber");
            }
        }

        throw new ErrorForm("InvalidExpression");
    }
    private int findLastIndex(String expression, String operator) {
        for (int i = expression.length() - 1; i >= 0; i--) {
            if (expression.charAt(i) == operator.charAt(0)) {
                if (operator.equals("-") && i == 0) {
                    continue;
                }
                return i;
            }
        }
        return -1;
    }
    private int findLastOperator(String expression, char operator) {
        int bracketCount = 0;
        int lastIndex = -1;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (c == '(') {
                bracketCount++;
            } else if (c == ')') {
                bracketCount--;
            } else if (bracketCount == 0 && c == operator) {
                // For minus, make sure it's not a unary minus
                if (operator == '-') {
                    // Check if it's not at the start and not after another operator
                    if (i > 0 && !isOp(expression.charAt(i-1))) {
                        lastIndex = i;
                    }
                } else {
                    lastIndex = i;
                }
            }
        }
        return lastIndex;
    }

    /**
     * Checks if a character is a valid operator.
     *
     * @param c the character to check.
     * @return true if the character is a valid operator, false otherwise.
     */
    public static boolean isOp(char c) {
        String ops = "+-/*";
        return ops.contains(String.valueOf(c));
    }

    /**
     * Determines if a string contains no operators.
     *
     * @param str the string to check.
     * @return true if the string contains no operators, false otherwise.
     */
    public static boolean noOps(String str) {
        if (str.isEmpty()) return true;

        for (int i = 0; i < str.length(); i++) {
            if (isOp(str.charAt(i)) && str.charAt(i) != '-') {
                return false;
            }
        }

        if (str.charAt(0) == '-') {
            return noOps(str.substring(1));
        }

        return true;
    }

    /**
     * Finds the matching closing bracket for an open bracket.
     *
     * @param str the string containing the expression.
     * @param index the index of the opening bracket.
     * @return the index of the matching closing bracket, or -1 if not found.
     */
    public static int correctClosedBracket(String str,int index){
        int count = 0;
        for (int i = index; i < str.length(); i++) {
            if(str.charAt(i) == '('){
                count++;
            } else if (str.charAt(i) == ')') {
                count--;
            }
            if(count == 0){
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if a character is a valid operation character.
     *
     * @param c the character to check.
     * @return true if the character is a valid operation character.
     */
    public static boolean validChars(char c){
        String validChars = "+-*/.";
        return validChars.indexOf(c) != -1;
    }

    /**
     * Checks if a character is a valid letter (A-Z).
     *
     * @param c the character to check.
     * @return true if the character is a valid letter.
     */
    public static boolean isLetter(char c){
        String ABC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        return ABC.indexOf(c) != -1;
    }

    /**
     * Checks if a character is a valid digit (0-9).
     *
     * @param c the character to check.
     * @return true if the character is a valid digit.
     */
    public static boolean isDigit(char c){
        String digits = "0123456789";
        return digits.indexOf(c) != -1;
    }

    /**
     * Finds the closest operation or bracket in the string.
     *
     * @param str the string to check.
     * @param start the starting index to search from.
     * @return the index of the closest operation or bracket.
     */
    public static int closestOpOrBrackets(String str,int start){
        int i;
        for (i = start; i < str.length(); i++) {
            if(isOp(str.charAt(i)) || str.charAt(i) == ')'){
                return i;
            }
        }
        return i;
    }

    /**
     * Checks if the string represents a valid number.
     *
     * @param s the string to check.
     * @return true if the string represents a number, false otherwise.
     */
    public boolean isNumber(String s){
        try {
            Double.parseDouble(s);
            return true;
        }catch (NumberFormatException _){
            return false;
        }
    }

    /**
     * Checks if the string represents text (non-numeric and non-formula).
     *
     * @param str the string to check.
     * @return true if the string represents text.
     */
    public boolean isText(String str){
        if(!this.isNumber(str) && !this.isForm(str)){
            return true;
        }
        return false;
    }

    /**
     * Retrieves all cell references in the given string.
     *
     * @param str the string containing cell references.
     * @return a list of cells that are referenced in the string.
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
     * Custom exception for form errors.
     */
    public class ErrorForm extends Exception {
        public ErrorForm(String errorMessage) {
            super(errorMessage);
        }
    }

    /**
     * Custom exception for cycle errors.
     */
    public class ErrorCycle extends Exception {
        public ErrorCycle(String errorMessage) {
            super(errorMessage);
        }
    }
}
