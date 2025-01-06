package assignments;

public class CellEntry implements Index2D {
    private int x; // X coordinate (column index)
    private int y; // Y coordinate (row index)

    // Constructor to initialize the cell's coordinates
    public CellEntry(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean isValid() {
        return x >= 0 && y >= 0; // Assuming valid coordinates are non-negative
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }
}