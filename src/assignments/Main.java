package assignments;

import assignments.Spreadsheet;

public class Main {
    public static void main(String[] args) {
        // יצירת אובייקט של Spreadsheet בגודל 5x5
        Spreadsheet spreadsheet = new Spreadsheet(5, 5);

        // הזנת הערכים
        spreadsheet.set(0, 0, "10");     // A1 = 10
        spreadsheet.set(1, 0, "=A1");    // B1 = A1
        spreadsheet.set(2, 0, "=B1*2");  // C1 = B1 * 2

        // חישוב עומק
        int[][] depths = spreadsheet.depth();

        // הדפסת תוצאות העומק
        System.out.println("Depth values for the spreadsheet:");
        for (int y = 0; y < depths.length; y++) {
            for (int x = 0; x < depths[0].length; x++) {
                System.out.print("Cell (" + (char) ('A' + x) + (y + 1) + "): Depth = " + depths[y][x] + " | ");
            }
            System.out.println();
        }

        // הדפסת תוצאות הערכים
        System.out.println("\nEvaluated values for the spreadsheet:");
        String[][] values = spreadsheet.eval();
        for (int y = 0; y < values.length; y++) {
            for (int x = 0; x < values[0].length; x++) {
                System.out.print("Cell (" + (char) ('A' + x) + (y + 1) + "): Value = " + values[y][x] + " | ");
            }
            System.out.println();
        }
    }
}
