package assignments;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import java.util.HashSet;

public class SCellTest {

    @Test
    public void testIsNumberFunction() {
        // Fully valid cases
        Assertions.assertTrue(SCell.isNumber("1"), "Positive integer should be valid");
        Assertions.assertTrue(SCell.isNumber("-1.1"), "Negative decimal should be valid");

        // Invalid cases
        Assertions.assertFalse(SCell.isNumber("2a"), "Number with letters is invalid");
        Assertions.assertFalse(SCell.isNumber("{2}"), "Number with curly braces is invalid");
        Assertions.assertFalse(SCell.isNumber("hi"), "Text is not a valid number");
    }

    @Test
    public void testIsTextFunction() {
        // Valid text cases
        Assertions.assertTrue(SCell.isText("2a"), "Combination of number and letters is text");
        Assertions.assertTrue(SCell.isText("{2}"), "Curly braces are considered text");
        Assertions.assertTrue(SCell.isText("hi"), "Regular text should be valid");

        // Invalid text cases
        Assertions.assertFalse(SCell.isText("1"), "Number should not be considered text");
        Assertions.assertFalse(SCell.isText("=1"), "Formula should not be considered text");
    }

    @Test
    public void testIsFormulaFunction() {
        // Valid formulas
        Assertions.assertTrue(SCell.isFormula("=1"), "Number in formula should be valid");
        Assertions.assertTrue(SCell.isFormula("=1.2"), "Decimal number in formula should be valid");
        Assertions.assertTrue(SCell.isFormula("=(0.2)"), "Formula with parentheses should be valid");
        Assertions.assertTrue(SCell.isFormula("=1+2"), "Addition formula should be valid");
        Assertions.assertTrue(SCell.isFormula("=1+2*3"), "Formula with multiplication and addition should be valid");
        Assertions.assertTrue(SCell.isFormula("=(1+2)*((3))-1"), "Complex formula should be valid");
        Assertions.assertTrue(SCell.isFormula("=A1"), "Cell reference should be valid");
        Assertions.assertTrue(SCell.isFormula("=A2+3"), "Formula with cell reference and value should be valid");
        Assertions.assertTrue(SCell.isFormula("=(2+A3)/A2"), "Complex formula with cell references should be valid");

        // Invalid formulas
        Assertions.assertFalse(SCell.isFormula("a"), "Single letter is not a formula");
        Assertions.assertFalse(SCell.isFormula("AB"), "Letter combination is not a formula");
        Assertions.assertFalse(SCell.isFormula("@2"), "Special symbol is not a formula");
        Assertions.assertFalse(SCell.isFormula("2+)"), "Incomplete formula with parentheses is invalid");
        Assertions.assertFalse(SCell.isFormula("(3+1*2)-"), "Formula with operator at the end is invalid");
        Assertions.assertFalse(SCell.isFormula("=()"), "Empty formula is invalid");
        Assertions.assertFalse(SCell.isFormula("=5**"), "Formula with duplicate operators is invalid");
    }

    @Test
    public void testComputeFormFunction() {
        Sheet mockSheet = new Ex2Sheet(); // Mock sheet for tests

        // Simple number tests
        Assertions.assertEquals(1.0, SCell.computeForm("=1", mockSheet, new HashSet<>()), "Simple number formula");
        Assertions.assertEquals(1.2, SCell.computeForm("=1.2", mockSheet, new HashSet<>()), "Decimal number");

        // Arithmetic operation tests
        Assertions.assertEquals(3.0, SCell.computeForm("=1+2", mockSheet, new HashSet<>()), "Simple addition");
        Assertions.assertEquals(5.0, SCell.computeForm("=1+2*2", mockSheet, new HashSet<>()), "Addition and multiplication");

        // Parentheses tests
        Assertions.assertEquals(5.0, SCell.computeForm("=((1+2)*2)-1", mockSheet, new HashSet<>()), "Complex formula with parentheses");
    }

    @Test
    public void testCircularReference() {
        // Circular dependency test
        Sheet mockSheet = new Ex2Sheet();
        mockSheet.set(0, 0, "=A0"); // Self-referencing circular dependency

        // Should return null or handle circular reference
        Assertions.assertNull(SCell.computeForm("=A0", mockSheet, new HashSet<>()), "Circular dependency should return null");
    }
}