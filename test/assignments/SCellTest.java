package assignments;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SCellTest {
    @Test
    public void testSCellFormulae() {  // או כל שם אחר של פונקציית טסט קיימת
        Sheet sheet = new Ex2Sheet(5, 5);

        // בודק הפנייה לתא במצב ERR_CYCLE
        sheet.set(1, 1, "=E1");  // E1=E1 -> ERR_CYCLE
        sheet.set(2, 1, "=E1");  // F1 מפנה ל-E1
        String result = sheet.value(2, 1);
        assertEquals(Ex2Utils.ERR_FORM, result);
   //* @Test
    //*public void testCyclicReferenceScenarios() {
        //*Sheet sheet = new Ex2Sheet(5, 5);

        // Test Case 1: הפניה לתא שהוא ERR_CYCL
        sheet.set(1, 1, "=E1");  // E1=E1 -> יהיה ERR_CYCL
        sheet.set(2, 1, "=E1");  // F1 מפנה לתא עם ERR_CYCL
        assertEquals(Ex2Utils.ERR_FORM, sheet.value(2, 1));  // צריך להיות ERR_FORM

        // Test Case 2: הפניה לתא עם ערך תקין
        sheet.set(2, 2, "5.0");    // שמים בF2 מספר תקין
        sheet.set(1, 2, "=F2");    // E2 מפנה לF2
        assertEquals("5.0", sheet.value(1, 2));  // צריך לקבל את המספר

        // Test Case 3: הפניה עצמית ישירה
        sheet.set(1, 3, "=E3");    // E3=E3
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.value(1, 3));  // צריך להיות ERR_CYCLE

        // Test Case 2: הפניה לתא עם ערך תקין
        sheet.set(2, 2, "5.0");  // F2 contains valid number
        sheet.set(1, 2, "=F2");  // E2 points to valid number
        assertEquals("5.0", sheet.value(1, 2));  // should return the number

        // Test Case 3: הפניה עצמית ישירה
        sheet.set(3, 3, "=G3");  // G3=G3
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.value(3, 3));  // should be ERR_CYCLE

        // Test Case 4: הפניה מעגלית בין תאים
        sheet.set(1, 4, "=F4");
        sheet.set(2, 4, "=E4");
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.value(1, 4));  // should be ERR_CYCLE
    }

   //* @Test
    public void testReferenceToErrorCell() {
        Sheet sheet = new Ex2Sheet(5, 5);

        // Setup: תא אחד במצב ERR_CYCL
        sheet.set(1, 1, "=E1");  // E1=E1 -> ERR_CYCL
        assertEquals(Ex2Utils.ERR_CYCLE, sheet.value(1, 1));

        // Test: הפניה לתא שהוא במצב ERR_CYCL
        sheet.set(2, 1, "=E1");  // F1 points to E1 (which is ERR_CYCL)
        assertEquals(Ex2Utils.ERR_FORM, sheet.value(2, 1));  // should be ERR_FORM
    }
}
