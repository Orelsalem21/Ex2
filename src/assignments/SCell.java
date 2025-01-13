import java.util.ArrayList;
import java.util.Stack;

public class SCell implements Cell {
    private String line;
    private String value = "";
    private int type;
    //1 - Text, 2 - Number, 3 - Form, -2 - Err form, -1 - Err cycle\Err
    private Ex2Sheet sheet;
    public CellEntry entry;
    private int order;
    private boolean isVisited = false;
    private boolean isCalculating = false;
    private int calculatedOrder = -2;

    public SCell(String s, Ex2Sheet sheet) {
        // Add your code here
        this.sheet = sheet;
        setData(s);
    }

    public void setValue(String v){
        this.value = v;
    }

    public void setEntry(CellEntry e){
        this.entry = e;
    }

    //@Override
    @Override
    public String toString() {
        return this.line;
    }

    @Override
    public void setData(String s) {
        line = s;
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

    @Override
    public void setOrder(int t) {
        this.order = t;
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    public void resetVisited() {
        isVisited = false;
        isCalculating = false;
        calculatedOrder = -2;
    }

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

        // Validate characters and cell references
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

        // Check brackets count and order
        int bracketsCount = 0;
        char c0 = str.charAt(0);
        char cEnd = str.charAt(str.length() - 1);

        if ((isOp(c0) && c0 != '-') || isOp(cEnd) || isLetter(cEnd)) {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c == '(') {
                bracketsCount++;
            } else if (c == ')') {
                bracketsCount--;
            }

            // Check for invalid brackets order at any point
            if (bracketsCount < 0) {
                return false;
            }

            // Check for operators around brackets
            if (i < str.length() - 1) {
                char next = str.charAt(i + 1);
                if (isOp(c) && (next == ')' || isOp(next))) {
                    return false;
                }
            }
        }

        // Final brackets count must be 0
        return bracketsCount == 0;
    }
    public double computeForm(String expression) throws ErrorForm, ErrorCycle {
        expression = expression.replaceAll(" ", "").toUpperCase();
        if (expression.isEmpty()) {
            throw new ErrorForm("EmptyExpression");
        }
        if (expression.charAt(0) == '=') {
            expression = expression.substring(1);
        }

        // Handle nested brackets recursively
        while (expression.contains("(")) {
            int openIndex = expression.lastIndexOf("("); // Start from innermost brackets
            int closeIndex = correctClosedBracket(expression, openIndex);
            if (closeIndex == -1) {
                throw new ErrorForm("InvalidBrackets");
            }

            // Calculate sub-expression inside brackets
            String subExpression = expression.substring(openIndex + 1, closeIndex);
            double result = computeForm(subExpression);

            // Replace bracketed expression with result
            expression = expression.substring(0, openIndex) + result +
                    expression.substring(closeIndex + 1);
        }

        if (noOps(expression)) {
            boolean mins = false;
            if (expression.charAt(0) == '-') {
                mins = true;
                expression = expression.substring(0);
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

            try {
                return Double.parseDouble(expression);
            } catch (NumberFormatException e) {
                throw new ErrorForm("InvalidNumber");
            }
        }

        // Handle arithmetic operations in order of precedence
        int subIndex = findLastIndex(expression, "-");
        if (subIndex != -1) {
            return computeForm(expression.substring(0, subIndex)) -
                    computeForm(expression.substring(subIndex + 1));
        }

        int additionIndex = findLastIndex(expression, "+");
        if (additionIndex != -1) {
            return computeForm(expression.substring(0, additionIndex)) +
                    computeForm(expression.substring(additionIndex + 1));
        }

        int multiIndex = findLastIndex(expression, "*");
        if (multiIndex != -1) {
            return computeForm(expression.substring(0, multiIndex)) *
                    computeForm(expression.substring(multiIndex + 1));
        }

        int divisionIndex = findLastIndex(expression, "/");
        if (divisionIndex != -1) {
            double divisor = computeForm(expression.substring(divisionIndex + 1));
            if (divisor == 0) {
                return Double.POSITIVE_INFINITY;
            }
            return computeForm(expression.substring(0, divisionIndex)) / divisor;
        }

        throw new ErrorForm("InvalidExpression");
    }

    // Helper method to find the last occurrence of an operator outside of parentheses
    private int findLastIndex(String expression, String operator) {
        int counter = 0;
        for (int i = expression.length() - 1; i >= 0; i--) {
            char c = expression.charAt(i);
            if (c == ')') counter++;
            if (c == '(') counter--;
            if (counter == 0 && expression.substring(i, i + 1).equals(operator)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isOp(char c) {
        String ops = "+-/*";
        return ops.contains(String.valueOf(c));
    }

    public static boolean noOps(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (isOp(str.charAt(i))) {
                if (str.charAt(i) != '-') {
                    return false;
                }
            }
        }
        if(str.indexOf("-") != str.lastIndexOf("-")){
            return false;
        }
        if(str.contains("-")){
            if(str.indexOf("-") != 0){
                return false;
            }
        }
        return true;
    }

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

    public static boolean validChars(char c){
        String validChars = "+-*/.()";
        return validChars.indexOf(c) != -1;
    }

    public static boolean isLetter(char c){
        String ABC = "ABCDEFHIGKLMNOPQRSTUVWXYZ";
        return ABC.indexOf(c) != -1;
    }

    public static boolean isDigit(char c){
        String digits = "0123456789";
        return digits.indexOf(c) != -1;
    }

    public static int closestOpOrBrackets(String str,int start){
        int i;
        for (i = start; i < str.length(); i++) {
            if(isOp(str.charAt(i)) || str.charAt(i) == ')'){
                return i;
            }
        }
        return i;
    }

    public boolean isNumber(String s){
        try {
            Double.parseDouble(s);
            return true;
        }catch (NumberFormatException _){
            return false;
        }
    }

    public boolean isText(String str){
        if(!this.isNumber(str) && !this.isForm(str)){
            return true;
        }
        return false;
    }

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

    public class ErrorForm extends Exception {
        public ErrorForm(String errorMessage) {
            super(errorMessage);
        }
    }

    public class ErrorCycle extends Exception {
        public ErrorCycle(String errorMessage) {
            super(errorMessage);
        }
    }
}