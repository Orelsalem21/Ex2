import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Ex2Sheet implements Sheet {
    private Cell[][] table;
    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for(int i=0;i<x;i=i+1) {
            for(int j=0;j<y;j=j+1) {
                table[i][j] = new SCell("",this);
                table[i][j].setType(Ex2Utils.TEXT);
                ((SCell)table[i][j]).setEntry(new CellEntry(Ex2Utils.ABC[i]+j));
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
        SCell c = (SCell)get(x,y);
        if(c!=null) {
            if(c.getType() == Ex2Utils.ERR_FORM_FORMAT){
                return Ex2Utils.ERR_FORM;
            } else if (c.getType() == Ex2Utils.ERR_CYCLE_FORM) {
                return Ex2Utils.ERR_CYCLE;
            }
            ans = c.toString();
            if(c.getType() == Ex2Utils.FORM){
                try {
                    ans = String.valueOf(c.computeForm(ans));
                }catch (SCell.ErrorForm e) {
                    ans = Ex2Utils.ERR_FORM;
                }catch (SCell.ErrorCycle e) {
                    ans = Ex2Utils.ERR_CYCLE;
                }
            }else if(c.getType() == Ex2Utils.NUMBER){
                ans = Double.parseDouble(ans) + "";
            } else if (c.getType() == Ex2Utils.TEXT) {
                ans = ans;// :)
            }
        }
        return ans;
    }

    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    @Override
    public Cell get(String cords) {
        Cell ans = null;
        CellEntry ce = new CellEntry(cords);
        if(ce.isValid()){
            int x = ce.getX();
            int y = ce.getY();
            ans = get(x,y);
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
        SCell c = new SCell(s,this);
        c.setEntry(new CellEntry(Ex2Utils.ABC[x]+y));
        table[x][y] = c;
    }
    @Override
    public void eval() {
        // Reset cells first
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (table[i][j] != null) {
                    SCell cell = (SCell)table[i][j];
                    cell.resetVisited();
                    if (cell.getType() == Ex2Utils.ERR_CYCLE_FORM) {
                        cell.setType(Ex2Utils.FORM);
                    }
                }
            }
        }

        int[][] dd = depth();

        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (dd[i][j] == -1) {
                    SCell cell = (SCell)get(i,j);
                    cell.setType(Ex2Utils.ERR_CYCLE_FORM);
                    cell.setValue(Ex2Utils.ERR_CYCLE);
                }
            }
        }

        for (int depth = 0; depth <= getMaxDepth(dd); depth++) {
            for (int i = 0; i < width(); i++) {
                for (int j = 0; j < height(); j++) {
                    if (dd[i][j] == depth) {
                        evaluateCell(i, j);
                    }
                }
            }
        }
    }
    private int getMaxDepth(int[][] depths) {
        int max = 0;
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (depths[i][j] > max) {
                    max = depths[i][j];
                }
            }
        }
        return max;
    }

    private void evaluateCell(int x, int y) {
        SCell cell = (SCell)get(x, y);
        if (cell == null) return;

        String str = cell.getData();
        if (str.isEmpty()) {
            cell.setType(Ex2Utils.TEXT);
            return;
        }

        if (str.charAt(0) == '=') {
            if (cell.isForm(str)) {
                ArrayList<String> cyclePath = new ArrayList<>();
                if (cell.detectCycle(cyclePath)) {
                    cell.setType(Ex2Utils.ERR_CYCLE_FORM);
                    cell.setValue(Ex2Utils.ERR_CYCLE);
                } else {
                    try {
                        String result = eval(x, y);
                        cell.setValue(result);
                        if(result.equals(Ex2Utils.ERR_FORM)){
                            cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                        }
                        else {
                            cell.setType(Ex2Utils.FORM);
                        }
                    } catch (Exception e) {
                        cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                        cell.setValue(Ex2Utils.ERR_FORM);
                    }
                }
            } else {
                cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                cell.setValue(Ex2Utils.ERR_FORM);
            }
        } else if (cell.isNumber(str)) {
            cell.setValue(Double.parseDouble(str) + "");
            cell.setType(Ex2Utils.NUMBER);
        } else {
            cell.setValue(str);
            cell.setType(Ex2Utils.TEXT);
        }
    }

    @Override
    public boolean isIn(int xx, int yy) {
        boolean ans = xx>=0 && yy>=0;
        if(xx > this.width() || yy > this.height()){
            ans = false;
        }
        return ans;
    }

    @Override
    public int[][] depth() {
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                if (table[i][j] != null) {
                    ((SCell)table[i][j]).resetVisited();
                }
            }
        }

        int[][] ans = new int[width()][height()];

        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                SCell cell = (SCell)get(i, j);
                if (cell != null) {
                    // calc order for this cell
                    int order = cell.calcOrder();
                    ans[i][j] = order;
                    // Update this cell order
                    cell.setOrder(order);
                }
            }
        }
        System.out.println(Arrays.deepToString(ans));
        return ans;
    }
    public void clearTable(){
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                set(i,j,"");
                get(i,j).setType(Ex2Utils.TEXT);
            }
        }
    }
    @Override
    public void load(String fileName) throws IOException {
        try {
            clearTable();
            File file = new File(fileName);
            Scanner myReader = new Scanner(file);
            if(!myReader.hasNext()){
                return;
            }
            String line = myReader.nextLine();
            while (myReader.hasNextLine() && line != null) {
                line = myReader.nextLine();
                line = line.replaceAll(" ","");
                String[] arr = line.split(",");
                if (arr.length < 3) {
                    continue;
                }
                try {
                    int x = Integer.parseInt(arr[0]);
                    int y = Integer.parseInt(arr[1]);
                    this.set(x, y, arr[2]);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            }
            myReader.close();
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
        eval();
    }


    @Override
    public void save(String fileName) throws IOException {
        try {
            File newFile = new File(fileName);
            FileWriter myWriter = new FileWriter(newFile);
            myWriter.write("FirstLine\n");
            for (int i = 0; i < width(); i++) {
                for (int j = 0; j < height(); j++) {
                    SCell cell = (SCell) get(i, j);
                    if (cell != null && !cell.getData().isEmpty()) {
                        myWriter.write(i + "," + j + "," + cell.getData() + "\n");
                    }
                }
            }
            myWriter.close();
        }catch (Exception e){
            throw new IOException(e.getMessage());
        }

        // Add your code here

        /////////////////////
    }

    @Override
    public String eval(int x, int y) {
        String ans = null;
        SCell cell = (SCell)get(x, y);
        if (cell != null) {
            ans = cell.toString();
            try {
                ans = String.valueOf(cell.computeForm(ans));
            } catch (SCell.ErrorForm e) {
                ans = Ex2Utils.ERR_FORM;
            } catch (SCell.ErrorCycle e) {
                ans = Ex2Utils.ERR_CYCLE;
            }
            cell.setValue(ans);
        }
        return ans;
    }
}