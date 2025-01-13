import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class Ex2Tests {
    @Test
    void testIsForm() {
        SCell cell = new SCell("Hey", new Ex2Sheet());
        String[] validFormulas = {
                "=1", "=1+2", "=(1+2)", "=2*3", "=(2*3)", "=(1+2)*3", "=A1", "=A2+3", "=(1+2)*((3))-1",
                "=(2+A3)/A2", "=(A1*2)", "=A1+A2*B3", "=C1/D2", "=(A1+2)*(B3-C4)", "=(2)", "=0.5", "=0+1",
                "=(1+2)/3", "=10-3", "=1.2+0.8", "=-1", "=-(A1+B2)", "=1*2+3", "=((1*2)+3)", "=2/3", "=4/2",
                "=4/2+1", "=A2-1", "=B3+B4", "=2*A1", "=(B2+1)/C3", "=(A1)+(A2)", "=(2)+(3*2)", "=1+2+3",
                "=1+2+3+4", "=(1*2)*3", "=1*2*3*4", "=(1+2)*2/2", "=(1/2)*(2/3)", "=1.2+3.4", "=1.2-0.4",
                "=1.2*2.0", "=2.2/2.0", "=2.0*(2.0/2.0)", "=((1.2*2)/2)", "=(2*A1)", "=1+((2*3)-1)", "=1+2-3",
                "=1*2+3-4", "=1+2*3-4/5", "=0+1+2", "=1-2-3", "=1*2*3", "=1/2/3", "=(A1)+(B2)", "=1+(2/3)",
                "=A1*(2/3)", "=1*2+3*4", "=(1+2)*(3+4)", "=(1+(2*3))", "=1/2+3*4", "=(1/2)+(3/4)", "=((1/2)+(3/4))",
                "=1.5+2.5-1.0", "=1.1*2.2/2.0", "=1.2/2.0+3.4", "=(1.2*2)+(2.2/2.0)", "=A1+(B2*C3)", "=(A1)+(B2+C3)",
                "=1.2+(A1)", "=1+(A1*B2)", "=A1*A2/B3", "=(1+A1)", "=1+A1", "=(1*A1)", "=A1+(2/3)", "=1+(A2)",
                "=2*(A1+A2)", "=(A1)*(B2)", "=(1+A1)*2", "=1+2+3+(A1)", "=A1+B2*(C3)", "=A1+A2+A3", "=B1*B2*B3",
                "=(1)+(2*3)", "=(1*A1)+(2*B2)", "=1+(A1/A2)", "=A1+A2-A3", "=(A1/A2)*B3", "=(1.2)+(2.2)", "=1+(2*A1)",
                "=1/(A1+A2)", "=A1/A2+(2/3)", "=A1*A2/B3", "=A1/(2*A2)", "=1*(A1/A2)", "=(1/2)*(A1/A2)",
        };
        for (String form : validFormulas) {
            assertTrue(cell.isForm(form));
        }
        String[] invalidFormulas = {
                "=a", "=AB", "=@2", "=2+)", "=(3+1*2)-", "==1", "=1+", "=A1+2+3)", "=(1+A2", "=A1+(2-3",
                "=1*2+", "=(1/2", "=1/(2", "=1*2/3+)", "=1+2-3/", "=(1+2)*3-)", "=1-2+", "=(A1+A2-", "=1+2+3)+4",
                "=(1*A2", "=A1/(A2", "=A1-(A2", "=A1+(2", "=1+", "=1/2/", "=(1/(2)", "=1*A2/", "=1+A2-", "=a100", "=5+a400+2",
                "=(1+2)-(3+", "=1/(A2", "=1+(A2", "=1-(A2", "=1+(2+A2", "=1+A2/(A3", "=(A1)+(B2)-", "=A1-(B2", "=(1+A2",
                "=(1+A2-", "=(1+2)*(3)+)", "=1-2/3-", "=1/(2+3-", "=1+(A2/(B3", "=1-2/(3", "=1*2/3+", "=(1/A2", "=1-(A2",
                "=1+(A2-", "=1+(A2)+", "=1+(A2*(B3", "=1/A2*(B3", "=(A1)/(B2-", "=A1/(B2+(C3", "=(1+A2-(3+", "=(A1+A2-(3+",
                "=(1+2)-(3+", "=1+A2+", "=1+A2-", "=(A1-(B2", "=1+(A2-(B3", "=(1-(A2)-(B3", "=1/A2+(2/3-", "=1/(A2+(B3",
                "=A1/A2*(B3-", "=1+A2-", "=A1+A2+(3+", "=(A1-(A2)+(3+", "=A1-(A2)+)", "=1+A2/(B3-", "=(1+(A2+(B3-", "=1+(2*(A2)",
                "=1+(A2-(B3+", "=1+(2*(A2)-(B3+", "=1+(A2)+3*", "=1+(A2/(B3+(C4", "=(A1)-(A2)+(3+", "=(1/(A2)+(B3-", "=A1/A2/(B3-",
                "=1-(A2+(B3-", "=1/(A2+(B3)+)", "=1+(A2-(B3-(C4", "=A1+A2+(B3+(C4-", "=1/(A2)+3*(B3", "=1/(A2)-(B3+(C4", "=A1-(A2+(B3-",
                "=(1/(A2)+(B3)-(C4", "=1/(A2)+(3/A3", "=1/A2/(B3-(C4"
        };
        for (String form : invalidFormulas) {
            assertFalse(cell.isForm(form));
        }
    }

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
        entry.setIndex("$15");
        assertFalse(entry.isValid());
        entry.setIndex("A##");
        assertFalse(entry.isValid());
        entry.setIndex("B-1");
        assertFalse(entry.isValid());
    }

    @Test
    void testGetXY() throws Exception {
        CellEntry entry = new CellEntry("A40");
        assertEquals(0, entry.getX());
        assertEquals(40, entry.getY());
        entry.setIndex("Z30");
        assertEquals(25, entry.getX());
        assertEquals(30, entry.getY());
        entry.setIndex("G15");
        assertEquals(6, entry.getX());
        assertEquals(15, entry.getY());
    }

    @Test
    void testValueForNumbers() throws Exception {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(0, 0, "1.0");
        sheet.eval();
        assertEquals("1.0", sheet.value(0, 0), "Cell value mismatch");
        assertEquals(Ex2Utils.NUMBER, sheet.get(0, 0).getType(), "Cell type mismatch for NUMBER");
    }

    @Test
    void testEvaluateCellAndValue() throws Exception {
        Ex2Sheet sheet = new Ex2Sheet();

        // Step 1: Set a numeric value in a cell
        sheet.set(0, 0, "1.0");
        sheet.eval(); // Evaluate the sheet

        // Step 2: Verify the cell type
        assertEquals(Ex2Utils.NUMBER, sheet.get(0, 0).getType(), "Expected cell type to be NUMBER");

        // Step 3: Verify the cell value
        assertEquals("1.0", sheet.value(0, 0), "Expected cell value to be '1.0'");

        // Step 4: Add a formula and verify
        sheet.set(1, 1, "=1+1");
        sheet.eval();
        assertEquals(Ex2Utils.FORM, sheet.get(1, 1).getType(), "Expected cell type to be FORM");
        assertEquals("2.0", sheet.value(1, 1), "Expected cell value to be '2.0'");

        // Step 5: Add an invalid formula and verify
        sheet.set(2, 2, "=1+X");
        sheet.eval();
        assertEquals(Ex2Utils.ERR_FORM_FORMAT, sheet.get(2, 2).getType(), "Expected cell type to be ERR_FORM_FORMAT");
        assertEquals(Ex2Utils.ERR_FORM, sheet.value(2, 2), "Expected cell value to be ERR_FORM");
    }

    @Test
    void testAllCellTypes() {
        Ex2Sheet sheet = new Ex2Sheet();
        // Set example values
        sheet.set(0, 0, "1.0");
        sheet.set(1, 1, "=1+1");
        sheet.set(2, 2, "=1+X");
        sheet.set(3, 3, "Hello");

        sheet.eval();

        // Check types
        assertEquals(Ex2Utils.NUMBER, sheet.get(0, 0).getType(), "Cell (0,0) should be NUMBER");
        assertEquals(Ex2Utils.FORM, sheet.get(1, 1).getType(), "Cell (1,1) should be FORM");
        assertEquals(Ex2Utils.ERR_FORM_FORMAT, sheet.get(2, 2).getType(), "Cell (2,2) should be ERR_FORM_FORMAT");
        assertEquals(Ex2Utils.TEXT, sheet.get(3, 3).getType(), "Cell (3,3) should be TEXT");
    }

    @Test
    void testCellTypesAndConstants() {
        Ex2Sheet sheet = new Ex2Sheet();

        // Set example values
        sheet.set(0, 0, "1.0");
        sheet.set(1, 1, "=1+1");
        sheet.set(2, 2, "=1+X");
        sheet.set(3, 3, "Hello");

        sheet.eval();

        // Check types
        assertEquals(Ex2Utils.NUMBER, sheet.get(0, 0).getType(), "Expected type NUMBER for value '1.0'");
        assertEquals(Ex2Utils.FORM, sheet.get(1, 1).getType(), "Expected type FORM for value '=1+1'");
        assertEquals(Ex2Utils.ERR_FORM_FORMAT, sheet.get(2, 2).getType(), "Expected type ERR_FORM_FORMAT for value '=1+X'");
        assertEquals(Ex2Utils.TEXT, sheet.get(3, 3).getType(), "Expected type TEXT for value 'Hello'");
    }

    @Test
    void testCellValuesAndTypes() {
        Ex2Sheet sheet = new Ex2Sheet();

        // Set example cells
        sheet.set(0, 0, "1.0"); // Number
        sheet.set(1, 1, "=1+1"); // Formula
        sheet.set(2, 2, "=1+X"); // Invalid formula
        sheet.set(3, 3, "Hello"); // Text

        sheet.eval();

        // Check values
        assertEquals("1.0", sheet.value(0, 0), "Value mismatch for cell (0, 0)");
        assertEquals("2.0", sheet.value(1, 1), "Value mismatch for cell (1, 1)");
        assertEquals(Ex2Utils.ERR_FORM, sheet.value(2, 2), "Value mismatch for cell (2, 2)");
        assertEquals("Hello", sheet.value(3, 3), "Value mismatch for cell (3, 3)");

        // Check types
        assertEquals(Ex2Utils.NUMBER, sheet.get(0, 0).getType(), "Type mismatch for cell (0, 0)");
        assertEquals(Ex2Utils.FORM, sheet.get(1, 1).getType(), "Type mismatch for cell (1, 1)");
        assertEquals(Ex2Utils.ERR_FORM_FORMAT, sheet.get(2, 2).getType(), "Type mismatch for cell (2, 2)");
        assertEquals(Ex2Utils.TEXT, sheet.get(3, 3).getType(), "Type mismatch for cell (3, 3)");
    }
    @Test
    void testSimpleCyclicReference() {
        Ex2Sheet sheet = new Ex2Sheet();

        // Test Case 1: Create cyclic reference
        sheet.set(2, 0, "=D0");  // Set C0=D0
        sheet.eval();
        sheet.set(3, 0, "=C0");  // Set D0=C0
        sheet.eval();

        // Check cycle detection
        assertEquals(Ex2Utils.ERR_CYCLE_FORM, sheet.get(2, 0).getType(), "C0 should detect cycle");
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.value(2, 0), "C0 should show ERR_CYCLE");
        assertEquals(Ex2Utils.ERR_CYCLE_FORM, sheet.get(3, 0).getType(), "D0 should detect cycle");
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.value(3, 0), "D0 should show ERR_CYCLE");

        // Test Case 2: Fix cycle and verify recovery
        sheet.set(2, 0, "42");  // Fix C0 with a number
        sheet.eval();
        assertEquals(Ex2Utils.NUMBER, sheet.get(2, 0).getType(), "C0 should be number type");
        assertEquals("42.0", sheet.value(2, 0), "C0 should show 42.0");
        assertEquals(Ex2Utils.FORM, sheet.get(3, 0).getType(), "D0 should be formula type");
        assertEquals("42.0", sheet.value(3, 0), "D0 should show 42.0");
    }
    @Test
    void testNumberType() {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(5, 0, "6");
        sheet.eval();
        assertEquals(Ex2Utils.NUMBER, sheet.get(5, 0).getType(), "Should be NUMBER type");
        assertEquals("6.0", sheet.value(5, 0), "Value should be 6.0");
    }
    @Test
    void testNumberDisplay() {
        Ex2Sheet sheet = new Ex2Sheet();
        sheet.set(5, 0, "9");
        sheet.eval();
        Cell cell = sheet.get(5, 0);
        assertEquals(Ex2Utils.NUMBER, cell.getType(), "Cell type should be NUMBER");
        assertEquals("9.0", sheet.value(5, 0), "Value should be 9.0");
        assertNotEquals(Ex2Utils.TEXT, cell.getType(), "Cell should not be TEXT type");
    }
    @Test
    void testBracketOperations() {
        Ex2Sheet sheet = new Ex2Sheet();

        // Test basic bracket operations
        sheet.set(0, 0, "=(5*8)");
        sheet.eval();
        assertEquals("40.0", sheet.value(0, 0), "Basic bracket multiplication failed");

        sheet.set(0, 1, "=(10/2)");
        sheet.eval();
        assertEquals("5.0", sheet.value(0, 1), "Basic bracket division failed");

        // Test nested brackets
        sheet.set(1, 0, "=((2+3)*4)");
        sheet.eval();
        assertEquals("20.0", sheet.value(1, 0), "Nested brackets calculation failed");

        sheet.set(1, 1, "=(2*(3+4))");
        sheet.eval();
        assertEquals("14.0", sheet.value(1, 1), "Complex nested brackets failed");
    }

    @Test
    void testInvalidBrackets() {
        Ex2Sheet sheet = new Ex2Sheet();

        // Test unbalanced brackets
        sheet.set(0, 0, "=(5*8");
        sheet.eval();
        assertEquals(Ex2Utils.ERR_FORM, sheet.value(0, 0), "Unclosed bracket not detected");

        sheet.set(0, 1, "=5*8)");
        sheet.eval();
        assertEquals(Ex2Utils.ERR_FORM, sheet.value(0, 1), "Unopened bracket not detected");

        // Test empty brackets
        sheet.set(1, 0, "=()");
        sheet.eval();
        assertEquals(Ex2Utils.ERR_FORM, sheet.value(1, 0), "Empty brackets not detected");

        // Test incorrect bracket order
        sheet.set(1, 1, "=)(5*8");
        sheet.eval();
        assertEquals(Ex2Utils.ERR_FORM, sheet.value(1, 1), "Incorrect bracket order not detected");
    }

    @Test
    void testComplexBracketExpressions() {
        Ex2Sheet sheet = new Ex2Sheet();

        // Test multiple operations with brackets
        sheet.set(0, 0, "=(2+3)*(4+5)");
        sheet.eval();
        assertEquals("45.0", sheet.value(0, 0), "Complex bracket expression failed");

        // Test mixed operations
        sheet.set(0, 1, "=(10/(2+3))");
        sheet.eval();
        assertEquals("2.0", sheet.value(0, 1), "Mixed operations with brackets failed");

        // Test multiple levels of nesting
        sheet.set(1, 0, "=(2*(3+(4*(5+1))))");
        sheet.eval();
        assertEquals("54.0", sheet.value(1, 0), "Multiple nested brackets failed");
    }

    @Test
    void testDivisionByZeroWithBrackets() {
        Ex2Sheet sheet = new Ex2Sheet();

        // Test division by zero inside brackets
        sheet.set(0, 0, "=(10/0)");
        sheet.eval();
        assertEquals(Double.POSITIVE_INFINITY+"", sheet.value(0, 0), "Division by zero in brackets not handling correctly");

        // Test complex expression resulting in division by zero
        sheet.set(0, 1, "=(5/(2-2))");
        sheet.eval();
        assertEquals(Double.POSITIVE_INFINITY+"", sheet.value(0, 1), "Complex division by zero not handling correctly");
    }

    @Test
    void testBracketsWithCellReferences() {
        Ex2Sheet sheet = new Ex2Sheet();

        // Setup cells with values
        sheet.set(0, 0, "5");
        sheet.set(0, 1, "3");
        sheet.eval();

        // Test brackets with cell references
        sheet.set(1, 0, "=(A0+A1)");
        sheet.eval();
        assertEquals("8.0", sheet.value(1, 0), "Basic cell reference in brackets failed");

        // Test complex expression with cell references and brackets
        sheet.set(1, 1, "=(A0*(A1+2))");
        sheet.eval();
        assertEquals("25.0", sheet.value(1, 1), "Complex cell reference with brackets failed");
    }
}
