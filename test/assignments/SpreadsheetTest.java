package assignments;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

public class SpreadsheetTest {

    @Test
    public void testSetAndGet() {
        Spreadsheet sheet = new Spreadsheet(5, 5);
        sheet.set(2, 3, "Hello");
        assertEquals("Hello", sheet.get(2, 3).getData());
    }

    @Test
    public void testEvalSimpleCell() {
        Spreadsheet sheet = new Spreadsheet(5, 5);
        sheet.set(1, 1, "123");
        assertEquals("123", sheet.eval(1, 1));
    }

    @Test
    public void testEvalFormula() {
        Spreadsheet sheet = new Spreadsheet(5, 5);
        sheet.set(0, 0, "5");
        sheet.set(1, 0, "=A1+5");
        assertEquals("10.0", sheet.eval(1, 0));
    }

    @Test
    public void testEvalWithError() {
        Spreadsheet sheet = new Spreadsheet(5, 5);
        sheet.set(1, 1, "=A1+");
        assertEquals(Ex2Utils.ERR_FORM, sheet.eval(1, 1));
    }

    @Test
    public void testCircularReference() {
        Spreadsheet sheet = new Spreadsheet(5, 5);
        sheet.set(0, 0, "=B1");
        sheet.set(1, 0, "=A1");
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.eval(0, 0));
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.eval(1, 0));
    }

    @Test
    public void testDepthCalculation() {
        Spreadsheet sheet = new Spreadsheet(5, 5);
        sheet.set(0, 0, "5");
        sheet.set(1, 0, "=A1+2");
        sheet.set(2, 0, "=B1*3");
        int[][] depths = sheet.depth();

        assertEquals(0, depths[0][0]);
        assertEquals(1, depths[1][0]);
        assertEquals(2, depths[2][0]);
    }

    @Test
    public void testSaveAndLoad() throws IOException {
        Spreadsheet sheet = new Spreadsheet(5, 5);
        sheet.set(0, 0, "5");
        sheet.set(1, 1, "Hello");
        sheet.set(2, 2, "=A1+B2");

        String fileName = "test_spreadsheet.txt";
        sheet.save(fileName);

        Spreadsheet loadedSheet = new Spreadsheet(5, 5);
        loadedSheet.load(fileName);

        assertEquals("5", loadedSheet.get(0, 0).getData());
        assertEquals("Hello", loadedSheet.get(1, 1).getData());
        assertEquals("=A1+B2", loadedSheet.get(2, 2).getData());

        // Clean up
        new File(fileName).delete();
    }

    @Test
    public void testInvalidCellAccess() {
        Spreadsheet sheet = new Spreadsheet(3, 3);
        assertNull(sheet.get(5, 5));
        assertEquals(Ex2Utils.ERR_FORM, sheet.eval(5, 5));
    }

    @Test
    public void testEmptyCell() {
        Spreadsheet sheet = new Spreadsheet(3, 3);
        assertEquals(Ex2Utils.EMPTY_CELL, sheet.eval(0, 0));
    }
}
