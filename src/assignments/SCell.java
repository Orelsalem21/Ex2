package assignments;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class SCell implements Cell {
    private String data;
    private int type;
    private int order;

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
        this.type = determineType(s);
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

    private int determineType(String s) {
        if (s == null || s.isEmpty()) {
            return Ex2Utils.TEXT;
        }
        if (isNumeric(s)) {
            return Ex2Utils.NUMBER;
        }
        if (isFormula(s)) {
            return Ex2Utils.FORM;
        }
        return Ex2Utils.TEXT;
    }

    public static boolean isFormula(String text) {
        if (text == null || text.isEmpty()) return false;
        if (!text.startsWith("=")) return false;
        String formulaBody = text.substring(1).trim();
        return isValidFormula(formulaBody);
    }

    public static boolean isNumeric(String text) {
        if (text == null || text.isEmpty()) return false;
        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isValidFormula(String formulaBody) {
        if (isNumeric(formulaBody)) return true;

        if (formulaBody.matches("^\\((.+)\\)$")) {
            return isValidFormula(formulaBody.substring(1, formulaBody.length() - 1));
        }

        if (formulaBody.matches(".+[+\\-*/].+")) {
            int mainOperatorIndex = findMainOperatorIndex(formulaBody);
            if (mainOperatorIndex == -1) return false;

            String left = formulaBody.substring(0, mainOperatorIndex).trim();
            String right = formulaBody.substring(mainOperatorIndex + 1).trim();

            return isValidFormula(left) && isValidFormula(right);
        }

        return formulaBody.matches("^[A-Za-z]+[0-9]+$");
    }

    private static boolean isValidCellReference(String cellRef) {
        if (cellRef == null || cellRef.length() < 2) return false;

        char col = Character.toUpperCase(cellRef.charAt(0));
        String rowStr = cellRef.substring(1);

        if (col < 'A' || col > 'Z') return false;

        try {
            int row = Integer.parseInt(rowStr);
            return row >= 0 && row <= 99;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static int findMainOperatorIndex(String formula) {
        int depth = 0;
        for (int i = 0; i < formula.length(); i++) {
            char c = formula.charAt(i);
            if (c == '(') depth++;
            if (c == ')') depth--;
            if (depth == 0 && isOperator(c)) {
                return i;
            }
        }
        return -1;
    }

    public Object computeFormula(String form, Sheet sheet) {
        if (!isFormula(form)) {
            return Ex2Utils.ERR_FORM_FORMAT;
        }

        try {
            Set<String> visitedCells = new HashSet<>();
            Stack<String> computationPath = new Stack<>();
            Object result = computeFormulaWithCycleDetection(form, sheet, visitedCells, computationPath);

            if (result == null) {
                return Ex2Utils.ERR_FORM_FORMAT;
            } else if (result.equals(Ex2Utils.ERR_CYCLE_FORM)) {
                return Ex2Utils.ERR_CYCLE;
            }

            return result;
        } catch (Exception e) {
            return Ex2Utils.ERR_FORM_FORMAT;
        }
    }

    private static String convertCellReferenceToId(String cellRef) {
        // מקבל הפניה כמו "D2" ומחזיר אותה בפורמט תקין
        cellRef = cellRef.toUpperCase();
        if (!isValidCellReference(cellRef)) {
            return null;
        }
        return cellRef;  // אם ההפניה תקינה, מחזירים אותה כמו שהיא
    }

    private static Object computeFormulaWithCycleDetection(String form, Sheet sheet,
                                                           Set<String> visitedCells, Stack<String> computationPath) {
        if (!isFormula(form)) {
            return Ex2Utils.ERR_FORM_FORMAT;
        }

        String expr = form.substring(1).trim();

        if (expr.matches("^[A-Za-z]+[0-9]+$")) {
            String cellRef = convertCellReferenceToId(expr);

            if (cellRef == null) {
                return Ex2Utils.ERR_FORM_FORMAT;
            }

            if (!computationPath.isEmpty() &&
                    (cellRef.equals(computationPath.peek()) || computationPath.contains(cellRef))) {
                return Ex2Utils.ERR_CYCLE_FORM;
            }

            computationPath.push(cellRef);

            Cell referencedCell = sheet.get(cellRef);
            if (referencedCell == null || referencedCell.getData() == null) {
                computationPath.pop();
                return Ex2Utils.ERR_FORM_FORMAT;
            }

            String cellData = referencedCell.getData();
            Object result;

            if (isFormula(cellData)) {
                result = computeFormulaWithCycleDetection(cellData, sheet, visitedCells, computationPath);
            } else if (isNumeric(cellData)) {
                try {
                    result = Double.parseDouble(cellData);
                } catch (NumberFormatException e) {
                    result = Ex2Utils.ERR_FORM_FORMAT;
                }
            } else {
                result = Ex2Utils.ERR_FORM_FORMAT;
            }

            computationPath.pop();
            return result;
        }

        if (isNumeric(expr)) {
            try {
                return Double.parseDouble(expr);
            } catch (NumberFormatException e) {
                return Ex2Utils.ERR_FORM_FORMAT;
            }
        }

        try {
            Double result = evaluateExpr(expr, sheet, visitedCells, computationPath);
            return result != null ? result : Ex2Utils.ERR_FORM_FORMAT;
        } catch (Exception e) {
            return Ex2Utils.ERR_FORM_FORMAT;
        }
    }

    private static Double evaluateExpr(String expr, Sheet sheet,
                                       Set<String> visitedCells, Stack<String> computationPath) {
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();
        StringBuilder number = new StringBuilder();

        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);

            if (Character.isWhitespace(c)) continue;

            if (Character.isDigit(c) || c == '.') {
                number.append(c);
                if (i == expr.length() - 1 || !Character.isDigit(expr.charAt(i + 1))) {
                    values.push(Double.parseDouble(number.toString()));
                    number.setLength(0);
                }
            }
            else if (Character.isLetter(c)) {
                StringBuilder cellRef = new StringBuilder();
                cellRef.append(c);
                while (i + 1 < expr.length() &&
                        (Character.isLetter(expr.charAt(i + 1)) || Character.isDigit(expr.charAt(i + 1)))) {
                    cellRef.append(expr.charAt(++i));
                }

                String ref = convertCellReferenceToId(cellRef.toString());
                if (ref == null) return null;

                if (!computationPath.isEmpty() &&
                        (ref.equals(computationPath.peek()) || computationPath.contains(ref))) {
                    return null;
                }

                computationPath.push(ref);

                Cell cell = sheet.get(ref);
                if (cell != null && cell.getData() != null) {
                    if (isNumeric(cell.getData())) {
                        values.push(Double.parseDouble(cell.getData()));
                    } else if (isFormula(cell.getData())) {
                        Object result = computeFormulaWithCycleDetection(cell.getData(), sheet, visitedCells, computationPath);
                        if (result instanceof Double) {
                            values.push((Double) result);
                        } else {
                            computationPath.pop();
                            return null;
                        }
                    } else {
                        computationPath.pop();
                        return null;
                    }
                } else {
                    computationPath.pop();
                    return null;
                }
                computationPath.pop();
            }
            else if (c == '(') {
                operators.push(c);
            }
            else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    if (values.size() < 2) return null;
                    Double b = values.pop();
                    Double a = values.pop();
                    values.push(applyOperator(operators.pop(), a, b));
                }
                if (!operators.isEmpty()) {
                    operators.pop();
                }
            }
            else if (isOperator(c)) {
                while (!operators.isEmpty() && operators.peek() != '(' &&
                        getPrecedence(operators.peek()) >= getPrecedence(c)) {
                    if (values.size() < 2) return null;
                    Double b = values.pop();
                    Double a = values.pop();
                    values.push(applyOperator(operators.pop(), a, b));
                }
                operators.push(c);
            }
        }

        if (number.length() > 0) {
            values.push(Double.parseDouble(number.toString()));
        }

        while (!operators.isEmpty()) {
            if (values.size() < 2) return null;
            Double b = values.pop();
            Double a = values.pop();
            values.push(applyOperator(operators.pop(), a, b));
        }

        return values.isEmpty() ? null : values.pop();
    }

    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private static int getPrecedence(char op) {
        if (op == '+' || op == '-') return 1;
        if (op == '*' || op == '/') return 2;
        return 0;
    }

    private static Double applyOperator(char op, Double a, Double b) {
        if (a == null || b == null) return null;
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': return b != 0 ? a / b : null;
            default: return null;
        }
    }
}