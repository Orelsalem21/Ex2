package assignments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class SpreadsheetTest {
    private Spreadsheet spreadsheet;

    @BeforeEach
    public void setUp() {
        // Initialize spreadsheet with dimensions from Ex2Utils
        spreadsheet = new Spreadsheet(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Test
    public void testCellNameConversion() {
        // Test valid cell name conversions
        Assertions.assertEquals(0, spreadsheet.cellNameToX("A1"), "Column A should convert to index 0");
        Assertions.assertEquals(0, spreadsheet.cellNameToY("A1"), "Row 1 should convert to index 0");

        Assertions.assertEquals(5, spreadsheet.cellNameToX("F1"), "Column F should convert to index 5");
        Assertions.assertEquals(12, spreadsheet.cellNameToX("M1"), "Column M should convert to index 12");

        // Test invalid cell names
        Assertions.assertEquals(-1, spreadsheet.cellNameToX("AA1"), "Columns beyond Z should return -1");
        Assertions.assertEquals(-1, spreadsheet.cellNameToX("@1"), "Invalid column should return -1");
    }

    @Test
    public void testEvalSimpleCases() {
        // Test basic cell evaluation
        spreadsheet.set(0, 0, "123");
        Assertions.assertEquals("123", spreadsheet.eval(0, 0), "Simple number should be evaluated directly");

        spreadsheet.set(1, 1, "Hello");
        Assertions.assertEquals("Hello", spreadsheet.eval(1, 1), "Text should be evaluated directly");
    }

    @Test
    public void testEvalFormulaCases() {
        // Prepare sheet for formula evaluation
        spreadsheet.set(0, 0, "10");   // A1 = 10
        spreadsheet.set(1, 0, "5");    // B1 = 5
        spreadsheet.set(2, 0, "=A1+B1"); // C1 = A1 + B1

        // Evaluate formula
        Assertions.assertEquals("15", spreadsheet.eval(2, 0), "Simple formula should be correctly evaluated");
    }

    @Test
    public void testDepthCalculation() {
        // Set up cells with dependencies
        spreadsheet.set(0, 0, "10");     // A1 = 10
        spreadsheet.set(1, 0, "=A1");    // B1 = A1
        spreadsheet.set(2, 0, "=B1");    // C1 = B1

        // Calculate depth
        int[][] depths = spreadsheet.depth();

        // Verify depth calculation
        Assertions.assertEquals(0, depths[0][0], "A1 (direct value) should have depth 0");
        Assertions.assertEquals(1, depths[0][1], "B1 (depends on A1) should have depth 1");
        Assertions.assertEquals(2, depths[0][2], "C1 (depends on B1) should have depth 2");
    }


    @Test
    public void testComplexFormulaEvaluation() {
        // Set up complex formula scenario
        spreadsheet.set(0, 0, "2");    // A1
        spreadsheet.set(1, 0, "3");    // B1
        spreadsheet.set(2, 0, "=A1+B1*2"); // C1 = A1 + B1 * 2

        // Evaluate complex formula
        Assertions.assertEquals("8", spreadsheet.eval(2, 0), "Complex formula with multiplication should be correctly evaluated");
    }

    @Test
    public void testCircularReferenceHandling() {
        // Create circular reference
        spreadsheet.set(0, 0, "=A1"); // Circular reference

        // Evaluate should return error
        Assertions.assertEquals(Ex2Utils.ERR_FORM, spreadsheet.eval(0, 0), "Circular reference should return error form");
    }

    @Test
    public void testEvalAllMethod() {
        // Prepare various cells
        spreadsheet.set(0, 0, "10");
        spreadsheet.set(1, 0, "=A1+5");

        // Evaluate all cells
        String[][] results = spreadsheet.eval();

        // Check specific known cells
        Assertions.assertEquals("10", results[0][0], "First cell should be evaluated correctly");
        Assertions.assertEquals("15", results[0][1], "Dependent cell should be evaluated correctly");
    }

    @Test
    public void testDimensionsAndBoundaries() {
        // Check predefined dimensions
        Assertions.assertEquals(Ex2Utils.WIDTH, spreadsheet.width(), "Width should match Ex2Utils constant");
        Assertions.assertEquals(Ex2Utils.HEIGHT, spreadsheet.height(), "Height should match Ex2Utils constant");

        // Test boundary conditions
        Assertions.assertTrue(spreadsheet.isIn(0, 0), "First cell should be in bounds");
        Assertions.assertTrue(spreadsheet.isIn(Ex2Utils.WIDTH - 1, Ex2Utils.HEIGHT - 1), "Last cell should be in bounds");

        Assertions.assertFalse(spreadsheet.isIn(-1, 0), "Negative x should be out of bounds");
        Assertions.assertFalse(spreadsheet.isIn(0, -1), "Negative y should be out of bounds");
        Assertions.assertFalse(spreadsheet.isIn(Ex2Utils.WIDTH, 0), "Out of width should be out of bounds");
        Assertions.assertFalse(spreadsheet.isIn(0, Ex2Utils.HEIGHT), "Out of height should be out of bounds");
    }
}
