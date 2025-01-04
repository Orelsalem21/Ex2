package assignments.ex2;

/**
 * SCell represents a single cell in a spreadsheet.
 * It supports storing data, identifying its type, and processing formulas.
 */
class SCell implements Cell {
    private String line; // Stores the content of the cell
    private int type;    // Represents the type of the cell (e.g., number, formula, text)
    private int order;   // Represents the processing order of the cell

    /**
     * Constructor for SCell.
     * Initializes the cell with the given content.
     * @param s The content of the cell.
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
        line = s;
        type = determineType(s);
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

    /**
     * Determines the type of the cell based on its content.
     * @param s The content to analyze.
     * @return The type of the cell: 0 (empty), 1 (number), 2 (formula), 3 (text).
     */
    private int determineType(String s) {
        if (s == null || s.isEmpty()) return 0; // Empty cell
        if (s.startsWith("=")) return 2;       // Formula
        try {
            Double.parseDouble(s);             // Check if it's a number
            return 1;
        } catch (NumberFormatException e) {
            return 3;                          // Otherwise, it's text
        }
    }

    /**
     * Evaluates the cell content if it is a formula.
     * For now, it supports basic arithmetic operations.
     * @return The evaluated result as a string, or the content if not a formula.
     */
    public String evaluate() {
        if (type != 2) return line; // Not a formula
        try {
            String formula = line.substring(1); // Remove '='
            // Placeholder: basic evaluation logic (can be extended)
            double result = evalSimpleFormula(formula);
            return String.valueOf(result);
        } catch (Exception e) {
            return "Error in formula";
        }
    }

    /**
     * Simple evaluation for basic arithmetic formulas.
     * @param formula The formula to evaluate (e.g., "1+2*3").
     * @return The result of the formula.
     */
    private double evalSimpleFormula(String formula) {
        // Use an external utility for full evaluation or implement a parser here.
        try {
            return ((Number) new javax.script.ScriptEngineManager()
                    .getEngineByName("JavaScript")
                    .eval(formula)).doubleValue();
        } catch (javax.script.ScriptException e) {
            throw new RuntimeException("Failed to evaluate formula", e);
        }
    }
}
