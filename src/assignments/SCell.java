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

    public static boolean isNumeric(String text) {
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

    public static boolean isFormula(String text) {
        // בודק רק אם זה מתחיל ב-= ויש עוד תווים
        return text != null && text.startsWith("=") && text.length() > 1;
    }

    private static boolean isValidFormula(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        if (isNumeric(text)) {
            return true;
        }

        return text.matches("([A-Za-z]+[0-9]+|[0-9]+)([-+*/]([A-Za-z]+[0-9]+|[0-9]+))*");
    }

    public Object computeFormula(String form, Sheet sheet) {
        try {
            String expr = form.substring(1).trim();

            // בדיקה אם יש רק אות (כמו =B)
            if (expr.matches("[A-Za-z]+")) {
                return Ex2Utils.ERR_FORM_FORMAT;
            }

            if (isNumeric(expr)) {
                return Double.parseDouble(expr);
            }

            if (expr.matches("[A-Za-z][0-9]+") || isValidFormula(expr)) {
                Set<String> visitedCells = new HashSet<>();
                Stack<String> computationPath = new Stack<>();
                Object result = computeFormulaWithCycleDetection(form, sheet, visitedCells, computationPath);

                if (result instanceof Integer) {
                    return result;
                }
                return result;
            }

            return Ex2Utils.ERR_FORM_FORMAT;
        } catch (Exception e) {
            return Ex2Utils.ERR_FORM_FORMAT;
        }
    }

    public static Object computeFormulaWithCycleDetection(String form, Sheet sheet,
                                                          Set<String> visitedCells, Stack<String> computationPath) {
        if (!isFormula(form)) {
            return Ex2Utils.ERR_FORM_FORMAT;
        }

        String expr = form.substring(1).trim();

        if (expr.matches("[A-Za-z][0-9]+")) {
            String currentCell = expr.toUpperCase();
            if (computationPath.contains(currentCell)) {
                return Ex2Utils.ERR_CYCLE_FORM;
            }
        }

        try {
            if (isNumeric(expr)) {
                return Double.parseDouble(expr);
            }

            Object result = evaluateExpr(expr, sheet, visitedCells, computationPath);
            if (result == null) {
                return Ex2Utils.ERR_FORM_FORMAT;
            }
            if (result instanceof Double && ((Double) result).equals((double) Ex2Utils.ERR_CYCLE_FORM)) {
                return Ex2Utils.ERR_CYCLE_FORM;
            }
            return result;
        } catch (Exception e) {
            return Ex2Utils.ERR_FORM_FORMAT;
        }
    }

    private static Double evaluateExpr(String expr, Sheet sheet,
                                       Set<String> visitedCells, Stack<String> computationPath) {
        Stack<Double> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

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
            } else if (isOperator(c)) {
                while (!operators.isEmpty() && getPrecedence(c) <= getPrecedence(operators.peek())) {
                    values.push(computeOperation(operators.pop(), values.pop(), values.pop()));
                }
                operators.push(c);
            } else if (Character.isLetter(c)) {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && Character.isLetterOrDigit(expr.charAt(i))) {
                    sb.append(expr.charAt(i++));
                }
                i--;
                String cellRef = sb.toString();
                Double cellValue = getCellValue(cellRef, sheet, visitedCells, computationPath);
                if (cellValue == null) return null;
                if (cellValue.equals((double)Ex2Utils.ERR_CYCLE_FORM)) {
                    return cellValue;
                }
                values.push(cellValue);
            }
        }

        while (!operators.isEmpty()) {
            values.push(computeOperation(operators.pop(), values.pop(), values.pop()));
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

    private static Double computeOperation(char op, Double b, Double a) {
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
            default:
                return null;
        }
    }

    private static Double getCellValue(String cellRef, Sheet sheet,
                                       Set<String> visitedCells, Stack<String> computationPath) {
        if (cellRef == null || cellRef.isEmpty()) {
            return null;
        }

        String upperCellRef = cellRef.toUpperCase();
        if (computationPath.contains(upperCellRef)) {
            return (double)Ex2Utils.ERR_CYCLE_FORM;
        }

        computationPath.push(upperCellRef);

        try {
            int col = Character.toUpperCase(cellRef.charAt(0)) - 'A';
            int row = Integer.parseInt(cellRef.substring(1));

            String value = sheet.value(col, row);
            if (value == null || value.isEmpty()) {
                return null;
            }

            if (isFormula(value)) {
                Object result = computeFormulaWithCycleDetection(value, sheet, visitedCells, computationPath);
                if (result instanceof Double) {
                    return (Double) result;
                }
                return null;
            }

            if (isNumeric(value)) {
                return Double.parseDouble(value);
            }
            return null;

        } catch (Exception e) {
            return null;
        } finally {
            computationPath.pop();
        }
    }
}