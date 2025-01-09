package assignments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SCellTest {
    private SCell cell;
    private Sheet mockSheet;

    @BeforeEach
    void setUp() {
        cell = new SCell("");
        mockSheet = new Spreadsheet(10, 10); // יצירת גיליון עם 10x10 תאים לדוגמה

        // הגדרת תאים בגיליון
        mockSheet.set(0, 0, "5"); // A1
        mockSheet.set(0, 3, "7"); // A4
        mockSheet.set(1, 0, "10"); // B1
        mockSheet.set(1, 3, "3"); // B4
    }

    @Test
    void testComputeFormulaValidSimple() {
        // בדיקות פורמולות חוקיות ופשוטות
        assertEquals(3.0, cell.computeFormula("=1+2", null), "Expected 1+2 to evaluate to 3.0");
        assertEquals(7.0, cell.computeFormula("=1+2*3", null), "Expected 1+2*3 to evaluate to 7.0");
        assertEquals(9.0, cell.computeFormula("=(1+2)*3", null), "Expected (1+2)*3 to evaluate to 9.0");
        assertEquals(8.0, cell.computeFormula("=(1+2)*3-1", null), "Expected (1+2)*3-1 to evaluate to 8.0");

        // בדיקות תאים עם פורמולות חוקיות
        assertEquals(12.0, cell.computeFormula("=A1+A4", mockSheet), "Expected A1+A4 to evaluate to 12.0");
        assertEquals(12.0, cell.computeFormula("=a1+A4", mockSheet), "Expected a1+A4 to evaluate to 12.0");
        assertEquals(8.0, cell.computeFormula("=A1+b4", mockSheet), "Expected A1+b4 to evaluate to 8.0");
    }

    @Test
    void testComputeFormulaInvalid() {
        // בדיקות פורמולות לא חוקיות
        assertEquals(Ex2Utils.ERR_FORM_FORMAT, cell.computeFormula("=5**", null), "Expected 5** to be an invalid formula");
        assertEquals(Ex2Utils.ERR_FORM_FORMAT, cell.computeFormula("=()", null), "Expected () to be an invalid formula");
        assertEquals(Ex2Utils.ERR_FORM_FORMAT, cell.computeFormula("=1+=2", null), "Expected 1+=2 to be an invalid formula");
    }

    @Test
    void testCycleDetection() {
        // בדיקת תלות מעגלית בין שני תאים
        mockSheet.set(0, 0, "=A2"); // A1 מפנה ל-A2
        mockSheet.set(0, 1, "=A1"); // A2 מפנה ל-A1

        assertEquals(Ex2Utils.ERR_CYCLE, cell.computeFormula("=A1", mockSheet),
                "Expected a circular dependency error for A1");
        assertEquals(Ex2Utils.ERR_CYCLE, cell.computeFormula("=A2", mockSheet),
                "Expected a circular dependency error for A2");
    }

    @Test
    void testSelfReferenceDetection() {
        // בדיקת תא שמפנה לעצמו
        mockSheet.set(1, 0, "=B1"); // B1 מפנה לעצמו

        assertEquals(Ex2Utils.ERR_CYCLE, cell.computeFormula("=B1", mockSheet),
                "Expected a circular dependency error for self-reference in B1");

        // בדיקת תא שמפנה לעצמו בצורה case insensitive
        mockSheet.set(2, 0, "=c1"); // C1 מפנה לעצמו באותיות קטנות
        assertEquals(Ex2Utils.ERR_CYCLE, cell.computeFormula("=C1", mockSheet),
                "Expected a circular dependency error for case-insensitive self-reference in C1");
    }

    @Test
    void testMultipleCellCycleDetection() {
        // בדיקת תלות מעגלית בין שלושה תאים
        mockSheet.set(0, 0, "=B1"); // A1 מפנה ל-B1
        mockSheet.set(1, 0, "=C1"); // B1 מפנה ל-C1
        mockSheet.set(2, 0, "=A1"); // C1 מפנה ל-A1

        assertEquals(Ex2Utils.ERR_CYCLE, cell.computeFormula("=A1", mockSheet),
                "Expected a circular dependency error for three-cell cycle starting at A1");
        assertEquals(Ex2Utils.ERR_CYCLE, cell.computeFormula("=B1", mockSheet),
                "Expected a circular dependency error for three-cell cycle starting at B1");
        assertEquals(Ex2Utils.ERR_CYCLE, cell.computeFormula("=C1", mockSheet),
                "Expected a circular dependency error for three-cell cycle starting at C1");

        // בדיקת תלות מעגלית בין ארבעה תאים
        mockSheet.set(0, 1, "=A2"); // A1 מפנה ל-A2
        mockSheet.set(0, 2, "=A3"); // A2 מפנה ל-A3
        mockSheet.set(0, 3, "=A4"); // A3 מפנה ל-A4
        mockSheet.set(0, 0, "=A1"); // A4 מפנה ל-A1

        assertEquals(Ex2Utils.ERR_CYCLE, cell.computeFormula("=A1", mockSheet),
                "Expected a circular dependency error for four-cell cycle");
    }

    @Test
    void testCycleDetectionInFormulas() {
        // בדיקת תלות מעגלית בתוך נוסחה מורכבת
        mockSheet.set(0, 0, "=A2+5"); // A1 = A2+5
        mockSheet.set(0, 1, "=A1*2"); // A2 = A1*2

        assertEquals(Ex2Utils.ERR_CYCLE, cell.computeFormula("=A1", mockSheet),
                "Expected a circular dependency error in complex formula A1");
        assertEquals(Ex2Utils.ERR_CYCLE, cell.computeFormula("=A2", mockSheet),
                "Expected a circular dependency error in complex formula A2");

        // בדיקת תלות מעגלית עם אופרטורים נוספים
        mockSheet.set(1, 0, "=B2/2"); // B1 = B2/2
        mockSheet.set(1, 1, "=B1-3"); // B2 = B1-3

        assertEquals(Ex2Utils.ERR_CYCLE, cell.computeFormula("=B1", mockSheet),
                "Expected a circular dependency error in formula with division");
        assertEquals(Ex2Utils.ERR_CYCLE, cell.computeFormula("=B2", mockSheet),
                "Expected a circular dependency error in formula with subtraction");
    }

    @Test
    void testDetermineType() {
        assertEquals(Ex2Utils.NUMBER, new SCell("123").getType(), "Expected type to be NUMBER for input 123");
        assertEquals(Ex2Utils.FORM, new SCell("=1+2").getType(), "Expected type to be FORM for input =1+2");
        assertEquals(Ex2Utils.TEXT, new SCell("hello").getType(), "Expected type to be TEXT for input hello");
        assertEquals(Ex2Utils.FORM, new SCell("=A1+A4").getType(), "Expected type to be FORM for input =A1+A4");
        assertEquals(Ex2Utils.TEXT, new SCell("=A1+A4.2").getType(), "Expected type to be TEXT for invalid formula");
    }
}