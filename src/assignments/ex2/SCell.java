package assignments.ex2;

import assignments.Ex2Utils;

/**
 * SCell class represents a cell in a spreadsheet.
 * It can contain numbers, text or formulas.
 */
public class SCell implements Cell {
    private String line;      // The content of the cell
    private int type;        // The type of content (number, text, formula)
    private int order;       // The order of the cell in formula evaluation

    /**
     * Constructor for SCell
     * @param s The initial content of the cell
     */
    public SCell(String s) {
        setData(s);
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return getData();
    }

    @Override
    public void setData(String s) {
        if (s == null) {
            line = Ex2Utils.EMPTY_CELL;
            type = Ex2Utils.NUMBER;  // Default type
            return;
        }
        line = s.trim();  // Remove leading/trailing whitespace

        // Determine the type of content
        if (line.isEmpty()) {
            type = Ex2Utils.NUMBER;
        } else if (line.startsWith("=")) {
            type = Ex2Utils.FORM;
        } else {
            try {
                Double.parseDouble(line);
                type = Ex2Utils.NUMBER;
            } catch (NumberFormatException e) {
                type = Ex2Utils.TEXT;
            }
        }
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
        order = t;
    }

    /**
     * Checks if the cell contains a number
     * @return true if the content is a valid number
     */
    public boolean isNumber() {
        if (type != Ex2Utils.NUMBER) {
            return false;
        }
        try {
            Double.parseDouble(line);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if the cell contains text
     * @return true if the content is text
     */
    public boolean isText() {
        return type == Ex2Utils.TEXT;
    }

    /**
     * Checks if the cell contains a formula
     * @return true if the content is a formula
     */
    public boolean isForm() {
        return type == Ex2Utils.FORM;
    }

    /**
     * Computes the result of the formula in the cell
     * @return the result of the formula computation
     */
    public String computeForm() {
        if (!isForm()) {
            return Ex2Utils.ERR_FORM;
        }

        String formula = line.substring(1).trim(); // Remove the '=' sign

        try {
            // Split the formula into parts
            String[] parts = formula.split(" ");
            if (parts.length != 3) {
                return Ex2Utils.ERR_FORM;
            }

            // Validate operator
            boolean validOperator = false;
            for (String op : Ex2Utils.M_OPS) {
                if (op.equals(parts[1])) {
                    validOperator = true;
                    break;
                }
            }
            if (!validOperator) {
                return Ex2Utils.ERR_FORM;
            }

            // Parse the operands
            double num1 = Double.parseDouble(parts[0]);
            double num2 = Double.parseDouble(parts[2]);
            String operator = parts[1];

            // Perform the calculation
            double result;
            switch (operator) {
                case "+":
                    result = num1 + num2;
                    break;
                case "-":
                    result = num1 - num2;
                    break;
                case "*":
                    result = num1 * num2;
                    break;
                case "/":
                    if (Math.abs(num2) < Ex2Utils.EPS) {  // Using epsilon for floating point comparison
                        return Ex2Utils.ERR_FORM;
                    }
                    result = num1 / num2;
                    break;
                default:
                    return Ex2Utils.ERR_FORM;
            }

            // Format the result
            if (Math.abs(result - Math.round(result)) < Ex2Utils.EPS) {
                return String.valueOf(Math.round(result));
            } else {
                // Round to appropriate precision using EPS
                double rounded = Math.round(result / Ex2Utils.EPS) * Ex2Utils.EPS;
                return String.format("%.3f", rounded);
            }

        } catch (NumberFormatException e) {
            return Ex2Utils.ERR_FORM;
        }
    }
}
