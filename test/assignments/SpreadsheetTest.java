package assignments;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Spreadsheet class.
 */
class SpreadsheetTest {

    /**
     * Tests the evaluation of formulas in cells.
     */
    @Test
    void testEvaluateFormula() {
        // Create a new 3x3 spreadsheet
        Spreadsheet sheet = new Spreadsheet(3, 3);

        // Add a simple addition formula
        sheet.set(0, 0, "=1+2");
        assertEquals("3.0", sheet.eval(0, 0));  // Check if the formula evaluates correctly

        // Add a formula with a reference to another cell
        sheet.set(0, 1, "=A1*3");
        assertEquals("9.0", sheet.eval(0, 1));  // Check if the formula with a reference works
    }

    /**
     * Tests the isNumber method of SCell.
     */
    @Test
    void testIsNumber() {
        assertTrue(SCell.isNumber("123.45"));
        assertFalse(SCell.isNumber("=A1+2"));  // Formula should not be considered a number
    }

    /**
     * Tests the isFormula method of SCell.
     */
    @Test
    void testIsFormula() {
        assertTrue(SCell.isFormula("=A1+B2"));
        assertFalse(SCell.isFormula("123.45"));  // A number should not be considered a formula
    }

    /**
     * Tests the set and get value methods in the Spreadsheet.
     */
    @Test
    void testSetGetValue() {
        Spreadsheet sheet = new Spreadsheet(3, 3);
        sheet.set(1, 1, "=A1+2");
        assertEquals("=A1+2", sheet.get(1, 1).getData());  // Verify if the value was correctly set and retrieved
    }

    /**
     * Tests the calculation of depth for formulas.
     */
    @Test
    void testDepthCalculation() {
        Spreadsheet sheet = new Spreadsheet(3, 3);
        sheet.set(0, 0, "=1+2");
        sheet.set(0, 1, "=A1*3");
        sheet.set(1, 1, "=B1+4");

        int[][] depth = sheet.depth();
        assertEquals(1, depth[0][0]);  // Verify depth of simple formula
        assertEquals(2, depth[1][1]);  // Verify depth of a formula depending on another cell
    }

    // Add more test cases as needed
}
