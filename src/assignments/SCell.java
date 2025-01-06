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

    private static boolean isNumber(String text) {
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

    private static boolean isFormula(String text) {
        return text != null && text.startsWith("=");
    }

    public static Double computeForm(String formula, Sheet sheet, Set<String> visitedCells) {
        if (!isFormula(formula)) {
            return null;
        }
        String expr = formula.substring(1).trim();

        // אם זו הפניה ישירה לתא
        if (expr.matches("[A-Z][0-9]+")) {
            return getCellValue(expr, sheet, visitedCells);
        }

        try {
            return Double.parseDouble(expr);
        } catch (NumberFormatException e) {
            // המשך לבדיקות הקיימות
        }

        if (expr.startsWith("*")) {
            String innerExpr = expr.substring(1);
            Double innerResult = evaluateExpression(innerExpr, sheet, visitedCells);
            return innerResult != null ? Math.abs(innerResult) : null;
        }

        return evaluateExpression(expr, sheet, visitedCells);
    }

    private static Double evaluateExpression(String expr, Sheet sheet, Set<String> visitedCells) {
        // בדיקה אם יש רק הפניה לתא
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
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                while (!ops.isEmpty() && precedence(c) <= precedence(ops.peek())) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(c);
            } else if (Character.isLetter(c)) {
                StringBuilder sb = new StringBuilder();
                while (i < expr.length() && (Character.isLetterOrDigit(expr.charAt(i)))) {
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
        int col = cellRef.charAt(0) - 'A';
        int row = Integer.parseInt(cellRef.substring(1)) - 1;
        String value = sheet.value(col, row);

        // בדוק אם הערך קיים ותקין
        if (value != null && !value.isEmpty()) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                // אם לא מספר, בדוק אם זו נוסחה
                if (isFormula(value)) {
                    return computeForm(value, sheet, visitedCells);
                }
            }
        }
        return null;
    }
}