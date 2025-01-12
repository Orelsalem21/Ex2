package assignments;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

public class SCellTest {

    @Test
    public void testDetermineType() {
        SCell numericCell = new SCell("123");
        assertEquals(Ex2Utils.NUMBER, numericCell.getType());

        SCell formulaCell = new SCell("=A1+5");
        assertEquals(Ex2Utils.FORM, formulaCell.getType());

        SCell textCell = new SCell("Hello");
        assertEquals(Ex2Utils.TEXT, textCell.getType());

        SCell emptyCell = new SCell("");
        assertEquals(Ex2Utils.TEXT, emptyCell.getType());
    }

    @Test
    public void testIsNumeric() {
        assertTrue(SCell.isNumeric("123"));
        assertTrue(SCell.isNumeric("123.456"));
        assertFalse(SCell.isNumeric("ABC"));
        assertFalse(SCell.isNumeric("123ABC"));
    }

    @Test
    public void testIsFormula() {
        assertTrue(SCell.isFormula("=A1+5"));
        assertTrue(SCell.isFormula("=123"));
        assertFalse(SCell.isFormula("123"));
        assertFalse(SCell.isFormula("Hello"));
    }

    @Test
    public void testComputeFormulaSimple() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "5"); // תא A1
        SCell formulaCell = new SCell("=A1");
        Object result = formulaCell.computeFormula("=A1", sheet);
        assertEquals(5.0, result);
    }

    @Test
    public void testComputeFormulaComplex() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "5"); // תא A1
        sheet.set(1, 0, "10"); // תא B1
        SCell formulaCell = new SCell("=A1+B1");
        Object result = formulaCell.computeFormula("=A1+B1", sheet);
        assertEquals(15.0, result);
    }

    @Test
    public void testComputeFormulaWithCycle() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=B1"); // תא A1
        sheet.set(1, 0, "=A1"); // תא B1
        SCell formulaCell = new SCell("=A1");
        Object result = formulaCell.computeFormula("=A1", sheet);
        assertEquals(Ex2Utils.ERR_CYCLE_FORM, result);
    }

    @Test
    public void testComputeInvalidFormula() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        SCell formulaCell = new SCell("=A1+");
        Object result = formulaCell.computeFormula("=A1+", sheet);
        assertEquals(Ex2Utils.ERR_FORM_FORMAT, result);
    }

    @Test
    public void testEvaluateExpression() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "5"); // תא A1
        sheet.set(1, 0, "10"); // תא B1

        Stack<String> computationPath = new Stack<>();
        Set<String> visitedCells = new HashSet<>();
        String data = "A1";
        Object result = SCell.computeFormulaWithCycleDetection("=A1+B1", sheet, visitedCells, computationPath, data);
        assertEquals(15.0, result);
    }

    @Test
    public void testEmptyCell() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        SCell emptyCell = new SCell("");
        assertEquals(Ex2Utils.TEXT, emptyCell.getType());
    }
}
