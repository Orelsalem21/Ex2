// Add your documentation below:

public class CellEntry  implements Index2D {
    private String index;

    public CellEntry(String index) {
        this.index = index;
    }
    public String getIndex() {
        return index;
    }
    public void setIndex(String index) {
        this.index = index;
    }
    @Override
    public boolean isValid() {
        if(this.index == null|| this.index.isEmpty()) {
            return false;
        }
        if(this.index.length() > 3 || this.index.length() < 2) {
            return false;
        }
        if(!isLetter(this.index.charAt(0))){
            return false;
        }
        try{
            int num = Integer.parseInt(this.index.substring(1));
            return num >= 0 && num <= 99;
        }catch(NumberFormatException e){
            return false;
        }
    }

    @Override
    public int getX() {
        String[] arr = Ex2Utils.ABC;
        for(int i = 0; i < arr.length; i++){
            if(String.valueOf(this.index.charAt(0)).equals(arr[i])) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getY() {
        try{
            return Integer.parseInt(this.index.substring(1));
        }
        catch(NumberFormatException e){
            return -1;
        }
    }
    @Override
    public String toString() {
        return this.index;
    }

    public static boolean isLetter(char c){
        String s = String.valueOf(c);
        for (String str : Ex2Utils.ABC){
            if(str.equals(s)){
                return true;
            }
        }
        return false;
    }


}
