# Ex2 - Spreadsheet Project

## Author
- **ID**: [208748368]

## Overview
This project implements a basic spreadsheet application, where each cell can store a number, a text string, or a formula. The formulas support basic arithmetic operations (addition, subtraction, multiplication, and division), and can reference other cells in the spreadsheet. The project demonstrates the implementation of core spreadsheet features, including formula evaluation, dependency tracking, and circular reference detection.

The spreadsheet logic is designed to handle dynamic updates, allowing cells to be evaluated based on the content of other cells. Error handling for invalid formulas and circular references is included, ensuring the integrity of the data.

## Functions
- **`eval(int x, int y)`**: Computes the value of a cell at position `(x, y)`. Supports formulas and handles errors like circular references.
- **`evalAll()`**: Evaluates all the cells in the spreadsheet and returns the computed values in a 2D array.
- **`depth()`**: Computes the dependency depth of each cell. Returns `0` for text or number cells, and `-1` for cells with circular dependencies.
- **`set(int x, int y, String s)`**: Sets the value of a cell at position `(x, y)` to the provided string. This string can be a number, text, or formula.
- **`get(int x, int y)`**: Retrieves the cell at position `(x, y)`.

### Internal Functions
- **`isNumber(String text)`**: Determines if the provided text is a valid number. Used to validate if a cell contains a numeric value.
- **`isFormula(String text)`**: Checks if the provided text is a valid formula. Formulas begin with an equals sign (`=`).
- **`findMainOperator(String formula)`**: Finds the main arithmetic operator in a formula (e.g., `+`, `-`, `*`, `/`), handling nested parentheses.
- **`applyOperation(double left, double right, char operator)`**: Applies the given operator to two operands. Used to evaluate basic arithmetic operations in formulas.

## Testing
Comprehensive unit tests are provided in the `SpreadsheetTest.java` and `SCellTest.java` files. The tests cover a range of cases, including:
- Valid formulas and arithmetic operations.
- Circular reference detection and error handling.
- Dependency depth calculation.
- Validation for valid numbers and formulas.

---

This project is a strong demonstration of core object-oriented design principles and recursion. It showcases how to handle complex data relationships within a grid structure, ensuring robustness and reliability when dealing with dynamic content like formulas and cell references.
