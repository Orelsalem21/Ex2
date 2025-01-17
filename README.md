# Ex2 - Advanced Spreadsheet Application

## Author
- **ID**: 208748368

## Overview
This project implements a spreadsheet application for managing data, computations, and formulas within a 2D table structure. Each cell in the spreadsheet can hold text, numbers, or formulas, and the application supports dynamic updates and error detection.

## Features
- **Formula Parsing**: Supports arithmetic operations (`+`, `-`, `*`, `/`), parentheses, and cell references.
- **Error Handling**: Detects invalid formulas and cyclic dependencies, marking cells with error codes (`ERR_FORM`, `ERR_CYCLE`).
- **Graphical User Interface (GUI)**: Interactive GUI for real-time cell editing and visualization.
- **Customizable Dimensions**: Adjustable spreadsheet size defined by constants.
- **File Persistence**: Load and save spreadsheet states.

## Components
- **`SCell`**: Represents individual cells, managing data types and dependencies.
- **`Ex2Sheet`**: Core spreadsheet logic, managing cells and formula evaluations.
- **`CellEntry`**: Processes and validates cell references (e.g., `A1`, `B2`).
- **`Ex2Utils`**: Utility constants and helper functions for parsing and evaluation.
- **`StdDrawEx2`**: Provides graphical rendering for the spreadsheet.

## Usage
### Spreadsheet Operations
- **Set Cell Value**:
   ```java
   Ex2Sheet sheet = new Ex2Sheet();
   sheet.set(0, 0, "=A1+5");
   ```
- **Get Cell Value**:
   ```java
   String value = sheet.value(0, 0);
   ```
- **Save/Load**:
   ```java
   sheet.save("spreadsheet.txt");
   sheet.load("spreadsheet.txt");
   ```

## Testing
Comprehensive tests are provided in `Ex2Tests.java`, covering valid inputs, invalid inputs, and edge cases for formula evaluation and error detection.

---

