package assignments;

import java.util.HashSet;
import java.util.Set;

// Add your documentation below:
public class CellEntry implements Index2D {

    private int x; // X coordinate (column index)
    private int y; // Y coordinate (row index)
    private String rawValue; // Original cell value
    private int type; // Cell type
    private Set<String> referencedCells; // Cells referenced in formula
    private Object computedValue; // Computed value of the cell
    private SCell scell; // Add SCell instance

    public CellEntry(int x, int y) {
        this.x = x;
        this.y = y;
        this.rawValue = Ex2Utils.EMPTY_CELL;
        this.type = Ex2Utils.TEXT;
        this.referencedCells = new HashSet<>();
        this.scell = new SCell(""); // Initialize SCell
    }

    public void setValue(String value) {
        this.rawValue = value;

        if (value == null || value.isEmpty()) {
            this.type = Ex2Utils.TEXT;
            this.computedValue = null;
        } else if (SCell.isNumeric(value)) {
            this.type = Ex2Utils.NUMBER;
            this.computedValue = Double.parseDouble(value);
        } else if (SCell.isFormula(value)) {
            this.type = Ex2Utils.FORM;
            this.computedValue = null;
            parseReferencedCells(value);
        } else {
            this.type = Ex2Utils.TEXT;
            this.computedValue = value;
        }
    }

    private void parseReferencedCells(String formula) {
        referencedCells.clear();
        String expr = formula.substring(1).trim();
        for (int i = 0; i < expr.length(); i++) {
            if (Character.isLetter(expr.charAt(i))) {
                StringBuilder cellRef = new StringBuilder();
                while (i < expr.length() && Character.isLetterOrDigit(expr.charAt(i))) {
                    cellRef.append(expr.charAt(i++));
                }
                i--;
                String ref = cellRef.toString().toUpperCase();
                referencedCells.add(ref);
            }
        }
    }

    public Object compute(Sheet sheet) {
        if (type != Ex2Utils.FORM) {
            return computedValue;
        }

        scell.setData(rawValue);
        Object result = scell.computeFormula(rawValue, sheet);

        if (result instanceof Integer && (int) result == Ex2Utils.ERR_FORM_FORMAT) {
            computedValue = Ex2Utils.ERR_FORM;
        } else if (result instanceof Integer && (int) result == Ex2Utils.ERR_CYCLE_FORM) {
            computedValue = Ex2Utils.ERR_CYCLE;
        } else {
            computedValue = result;
        }

        return computedValue;
    }

    @Override
    public boolean isValid() {
        return x >= 0 && y >= 0;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }
}
