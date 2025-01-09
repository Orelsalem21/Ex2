package assignments.ex2;

import assignments.Cell;
import assignments.Ex2Utils;
import assignments.SCell;
import assignments.Sheet;

public class Ex2Sheet implements Sheet {
    private Cell[][] table;

    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for(int i=0;i<x;i=i+1) {
            for(int j=0;j<y;j=j+1) {
                table[i][j] = new SCell(Ex2Utils.EMPTY_CELL);
            }
        }
        eval();
    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        String ans = Ex2Utils.EMPTY_CELL;
        Cell c = get(x,y);
        if(c!=null) {ans = c.toString();}
        return ans;
    }

    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    @Override
    public Cell get(String cords) {
        Cell ans = null;
        if (cords != null && !cords.isEmpty()) {
            try {
                // מחלץ את האות (העמודה) והמספר (השורה)
                String column = cords.replaceAll("[0-9]", "").toUpperCase();
                String row = cords.replaceAll("[A-Za-z]", "");

                // מחשב את אינדקס העמודה
                int x = -1;
                for (int i = 0; i < Ex2Utils.ABC.length; i++) {
                    if (Ex2Utils.ABC[i].equals(column)) {
                        x = i;
                        break;
                    }
                }

                // מחשב את אינדקס השורה (מחסיר 1 כי המספור מתחיל מ-1)
                int y = Integer.parseInt(row) - 1;

                // בודק אם הקואורדינטות בתחום התקין
                if (isIn(x, y)) {
                    ans = get(x, y);
                }
            } catch (Exception e) {
                // במקרה של שגיאה בפרסור מחזיר null
                ans = null;
            }
        }
        return ans;
    }

    @Override
    public int width() {
        return table.length;
    }

    @Override
    public int height() {
        return table[0].length;
    }

    @Override
    public void set(int x, int y, String s) {
        Cell c = new SCell(s);
        table[x][y] = c;
    }

    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && yy >= 0 && xx < width() && yy < height();
    }

    // שאר המחלקה נשארת כפי שהיא...
}
