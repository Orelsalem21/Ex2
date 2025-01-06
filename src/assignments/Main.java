package assignments;

public class Main {
    public static void main(String[] args) {
        // יצירת גיליון אלקטרוני בגודל 5x5
        Ex2Sheet sheet = new Ex2Sheet(5, 5);

        // הזנת ערכים ונוסחאות
        sheet.set(0, 0, "5");           // A1 = 5
        sheet.set(1, 0, "10");          // B1 = 10
        sheet.set(0, 1, "=A1+B1");      // A2 = A1 + B1
        sheet.set(1, 1, "=A2*2");       // B2 = A2 * 2
        sheet.set(2, 0, "=A1+B2");      // C1 = A1 + B2

        // הדפסת ערכים
        System.out.println("A1: " + sheet.value(0, 0));
        System.out.println("B1: " + sheet.value(1, 0));
        System.out.println("A2: " + sheet.value(0, 1));
        System.out.println("B2: " + sheet.value(1, 1));
        System.out.println("C1: " + sheet.value(2, 0));

        // בדיקת הפניה מעגלית
        sheet.set(3, 0, "=D2");         // D1 = D2
        sheet.set(3, 1, "=D1");         // D2 = D1
        System.out.println("D1: " + sheet.value(3, 0));
        System.out.println("D2: " + sheet.value(3, 1));

        // הדפסת כל הגיליון
        String[][] allValues = sheet.eval();
        System.out.println("\nAll values:");
        for (int i = 0; i < allValues.length; i++) {
            for (int j = 0; j < allValues[i].length; j++) {
                System.out.print(allValues[i][j] + "\t");
            }
            System.out.println();
        }
    }
}