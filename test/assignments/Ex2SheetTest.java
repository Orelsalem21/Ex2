package assignments;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;

public class Ex2SheetTest {

    @Test
    public void testSetAndGet() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(2, 3, "Hello");
        assertEquals("Hello", sheet.get(2, 3).getData());
    }

    @Test
    public void testEval() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(1, 1, "123");
        sheet.set(2, 2, "=A2+5");
        assertEquals("123", sheet.eval(1, 1));
        assertEquals(Ex2Utils.ERR_FORM, sheet.eval(2, 2));
    }

    @Test
    public void testIsIn() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        assertTrue(sheet.isIn(0, 0));
        assertTrue(sheet.isIn(2, 2));
        assertFalse(sheet.isIn(3, 3));
    }

    @Test
    public void testDepth() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "5");
        sheet.set(1, 1, "=A1+2");
        sheet.set(2, 2, "=B2*3");
        int[][] depths = sheet.depth();
        assertEquals(0, depths[0][0]);
        assertEquals(1, depths[1][1]);
        assertEquals(2, depths[2][2]);
    }

    @Test
    public void testSaveAndLoad() throws IOException {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "5");
        sheet.set(1, 1, "Test");
        sheet.set(2, 2, "=A1+B2");

        String fileName = "test_sheet.txt";
        sheet.save(fileName);

        Ex2Sheet loadedSheet = new Ex2Sheet();
        loadedSheet.load(fileName);

        assertEquals("5", loadedSheet.get(0, 0).getData());
        assertEquals("Test", loadedSheet.get(1, 1).getData());
        assertEquals("=A1+B2", loadedSheet.get(2, 2).getData());

        // Clean up the test file
        new File(fileName).delete();
    }

    @Test
    public void testCircularReference() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=B1");
        sheet.set(1, 0, "=A1");
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.eval(0, 0));
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.eval(1, 0));
    }

    @Test
    public void testInvalidFormula() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        sheet.set(0, 0, "=A1+");
        assertEquals(Ex2Utils.ERR_FORM, sheet.eval(0, 0));
    }

    @Test
    public void testEmptyCell() {
        Ex2Sheet sheet = new Ex2Sheet(3, 3);
        assertEquals(Ex2Utils.EMPTY_CELL, sheet.eval(0, 0));
    }
}
