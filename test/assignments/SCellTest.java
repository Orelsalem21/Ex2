package assignments;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the SCell class.
 */
class SCellTest {

    /**
     * Tests the isFormula method for identifying formulas.
     */
    @Test
    void testIsFormula() {
        assertTrue(SCell.isFormula("=A1+B2"));
        assertFalse(SCell.isFormula("123.45"));  // A number should not be considered a formula
    }

    /**
     * Tests the computeForm method to evaluate formulas.
     */
    @Test
    void testComputeForm() {
        // Test a simple formula for addition
        assertEquals(5.0, SCell.computeForm("=2+3", null, null));

        // Test a formula with a reference to another cell
        assertEquals(9.0, SCell.computeForm("=A1*3", null, null));  // Assuming cell A1 contains "3"
    }

    /**
     * Tests the setData and getData methods for setting and retrieving data in a cell.
     */
    @Test
    void testSetData() {
        SCell cell = new SCell("=A1+2");
        assertEquals("=A1+2", cell.getData());  // Ensure that the data was correctly set

        cell.setData("123.45");
        assertEquals("123.45", cell.getData());  // Ensure that the new value was correctly set
    }

    /**
     * Tests the setType and getType methods to set and get the type of cell.
     */
    @Test
    void testTypeSetting() {
        SCell cell = new SCell("=A1+2");
        assertEquals(Ex2Utils.FORM, cell.getType());  // Verify that the type is correctly set to FORM

        cell.setData("123.45");
        assertEquals(Ex2Utils.NUMBER, cell.getType());  // Verify that the type changes to NUMBER
    }
}
