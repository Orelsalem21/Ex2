package assignments;

import java.util.HashSet;

public class Main {

    public static void main(String[] args) {
        // Initialize a spreadsheet for testing
        Spreadsheet spreadsheet = new Spreadsheet(9, 17);

        // Step 1: Set value to A1
        System.out.println("Setting cell A1 to '=A1'...");
        spreadsheet.set(0, 0, "=A1");

        // Step 2: Evaluate A1
        System.out.println("Evaluating cell A1...");
        String result = spreadsheet.eval(0, 0);
        result = convertErrorToText(result);
        System.out.println("Result for A1: " + result);

        // Step 3: Check the entire structure
        System.out.println("Spreadsheet structure:");
        String[][] evalResults = spreadsheet.eval();
        for (int y = 0; y < spreadsheet.height(); y++) {
            for (int x = 0; x < spreadsheet.width(); x++) {
                String cellValue = convertErrorToText(evalResults[y][x]);
                System.out.print(cellValue + "\t");
            }
            System.out.println();
        }
    }

    /**
     * Converts numeric error codes to their text representation.
     *
     * @param value The value to check.
     * @return The text representation of the error if applicable, otherwise the original value.
     */
    private static String convertErrorToText(String value) {
        if (value.equals("-1.0")) {
            return Ex2Utils.ERR_CYCLE; // Convert to ERR_CYCLE!
        }
        return value; // Return original value if not an error
    }
}
