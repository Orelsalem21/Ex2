import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Ex2Sheet class and its related functionalities.
 */
class Ex2Tests {

    /**
     * Tests the isForm() method for validating formulas.
     * Includes cases for valid and invalid formulas.
     */
    @Test
    void testIsNumber() {
        SCell cell = new SCell("", new Ex2Sheet());
        assertTrue(cell.isNumber("1.0"));
        assertTrue(cell.isNumber("-1.0"));
        assertFalse(cell.isNumber("1.0a"));
        assertFalse(cell.isNumber("a1"));
        assertFalse(cell.isNumber(""));
    }

    @Test
    void testIsForm() {
        SCell cell = new SCell("Hey", new Ex2Sheet());
        String[] validFormulas = {
                "=1", "=1+2", "=(1+2)", "=2*3", "=(2*3)", "=(1+2)*3", "=A1", "=A2+3", "=(1+2)*((3))-1",
                "=(2+A3)/A2", "=(A1*2)", "=A1+A2*B3", "=C1/D2", "=(A1+2)*(B3-C4)"
        };
        for (String form : validFormulas) {
            assertTrue(cell.isForm(form));
        }

        String[] invalidFormulas = {
                "=a", "=AB", "=@2", "=2+)", "=(3+1*2)-", "==1", "=1+", "=A1+2+3)", "=(1+A2"
        };
        for (String form : invalidFormulas) {
            assertFalse(cell.isForm(form));
        }
    }

    /**
     * Tests the isIn() method to check if a given cell is within bounds.
     */
    @Test
    void testISIn() throws Exception {
        Ex2Sheet sheet = new Ex2Sheet();
        int x = 8, y = 15;
        assertTrue(sheet.isIn(x, y));
        x = -1;
        y = 5;
        assertFalse(sheet.isIn(x, y));
        x = 15;
        y = -7;
        assertFalse(sheet.isIn(x, y));
        x = 25;
        y = 100;
        assertFalse(sheet.isIn(x, y));
    }

    /**
     * Tests the value() method for computing cell values.
     */
    @Test
    void testValue() throws Exception {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "=15+2");
        sheet.eval();
        assertEquals("17.0", sheet.value(0, 0));
        sheet.set(0, 0, "=15G+2");
        sheet.eval();
        assertEquals(Ex2Utils.ERR_FORM, sheet.value(0, 0));
        sheet.set(0, 0, "=18/9*3");
        sheet.eval();
        assertEquals("6.0", sheet.value(0, 0));
    }

    /**
     * Tests the isValid() method for validating cell entries.
     */
    @Test
    void testIsVaild() throws Exception {
        CellEntry entry = new CellEntry("A40");
        assertTrue(entry.isValid());
        entry.setIndex("Z30");
        assertTrue(entry.isValid());
        entry.setIndex("G400");
        assertFalse(entry.isValid());
        entry.setIndex("B");
        assertFalse(entry.isValid());
    }

    /**
     * Tests the getX() and getY() methods of CellEntry.
     */
    @Test
    void testGetXY() throws Exception {
        CellEntry entry = new CellEntry("A40");
        assertEquals(0, entry.getX());
        assertEquals(40, entry.getY());
        entry.setIndex("Z30");
        assertEquals(25, entry.getX());
        assertEquals(30, entry.getY());
    }

    /**
     * Tests the behavior of the value() method when dealing with numeric cells.
     */
    @Test
    void testValueForNumbers() throws Exception {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "1.0");
        sheet.eval();
        assertEquals("1.0", sheet.value(0, 0), "Cell value mismatch");
    }

    /**
     * Tests the evaluation and value computation for various cell types.
     */
    @Test
    void testEvaluateCellAndValue() throws Exception {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "1.0");
        sheet.eval();
        assertEquals(Ex2Utils.NUMBER, sheet.get(0, 0).getType(), "Expected cell type to be NUMBER");
        assertEquals("1.0", sheet.value(0, 0), "Expected cell value to be '1.0'");
    }

    /**
     * Tests cell types for different kinds of values (text, formulas, errors, etc.).
     */
    @Test
    void testAllCellTypes() {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "1.0");
        sheet.set(1, 1, "=1+1");
        sheet.set(2, 2, "=1+X");
        sheet.set(3, 3, "Hello");

        sheet.eval();

        assertEquals(Ex2Utils.NUMBER, sheet.get(0, 0).getType(), "Cell (0,0) should be NUMBER");
        assertEquals(Ex2Utils.FORM, sheet.get(1, 1).getType(), "Cell (1,1) should be FORM");
        assertEquals(Ex2Utils.ERR_FORM_FORMAT, sheet.get(2, 2).getType(), "Cell (2,2) should be ERR_FORM_FORMAT");
        assertEquals(Ex2Utils.TEXT, sheet.get(3, 3).getType(), "Cell (3,3) should be TEXT");
    }

    /**
     * Tests cyclic references in the spreadsheet.
     */
    @Test
    void testSimpleCyclicReference() {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(2, 0, "=D0");
        sheet.eval();
        sheet.set(3, 0, "=C0");
        sheet.eval();

        assertEquals(Ex2Utils.ERR_CYCLE_FORM, sheet.get(2, 0).getType(), "C0 should detect cycle");
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.value(2, 0), "C0 should show ERR_CYCLE");
        assertEquals(Ex2Utils.ERR_CYCLE_FORM, sheet.get(3, 0).getType(), "D0 should detect cycle");
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.value(3, 0), "D0 should show ERR_CYCLE");
    }
    @Test
    void testDepthCalculation() {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "1");
        sheet.set(1, 1, "=A0+1");
        sheet.set(2, 2, "=B1+2");
        sheet.eval();

        int[][] depth = sheet.depth();
        assertEquals(0, depth[0][0], "Cell A0 should have depth 0");
        assertEquals(1, depth[1][1], "Cell B1 should have depth 1");
        assertEquals(2, depth[2][2], "Cell C2 should have depth 2");
    }
    @Test
    void testUpdateDependentCells() {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "1.0");
        sheet.set(1, 1, "=A0+1");
        sheet.eval();

        sheet.set(0, 0, "2.0");
        sheet.eval();

        assertEquals("3.0", sheet.value(1, 1), "Dependent cell B1 was not updated correctly");
    }
    @Test
    void testGetReferences() {
        SCell cell = new SCell("=A1+B2", new Ex2Sheet());
        ArrayList<SCell> refs = cell.getReferences(cell.getData());
        assertEquals(2, refs.size(), "Expected 2 references in the formula");
    }

    @Test
    void testSaveAndLoad() throws Exception {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "1.0");
        sheet.set(1, 1, "=A0+1");
        sheet.set(2, 2, "Hello");

        String fileName = "test_sheet.txt";
        sheet.save(fileName);

        Ex2Sheet loadedSheet = new Ex2Sheet();
        loadedSheet.load(fileName);

        assertEquals(sheet.value(0, 0), loadedSheet.value(0, 0), "Loaded value of A0 does not match");
        assertEquals(sheet.value(1, 1), loadedSheet.value(1, 1), "Loaded value of B1 does not match");
        assertEquals(sheet.value(2, 2), loadedSheet.value(2, 2), "Loaded value of C2 does not match");
    }

}
