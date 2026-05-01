// Map.java
import java.util.Random;

public class Map {
    private int[][] grid = {
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
        {1,0,1,1,1,1,1,0,1,1,1,1,1,0,1,1,1,1,1,0,1,1,1,1,1,1,1,0,0,1},
        {1,0,1,0,0,0,1,0,1,0,0,0,1,0,1,0,0,0,1,0,1,0,0,0,0,0,1,0,0,1},
        {1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,0,1,1,1,0,1,0,0,1},
        {1,0,1,0,1,0,0,0,1,0,1,0,0,0,1,0,1,0,0,0,1,0,0,0,1,0,1,0,0,1},
        {1,0,1,0,1,1,1,1,1,0,1,1,1,1,1,0,1,1,1,1,1,1,1,0,1,0,1,1,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,0,0,0,0,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,0,1,1,1,1,0,1},
        {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,1},
        {1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,0,1,0,1},
        {1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,1,0,1},
        {1,0,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,0,1,0,1,0,1},
        {1,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,1,0,0,0,1},
        {1,0,1,0,1,0,1,1,1,1,1,1,1,0,1,0,1,1,1,1,1,1,0,1,0,1,1,1,0,1},
        {1,0,0,0,1,0,1,0,0,0,0,0,1,0,1,0,0,0,0,0,0,1,0,0,0,0,0,1,0,1},
        {1,1,1,1,1,0,1,0,1,1,1,0,1,0,1,1,1,1,1,1,0,1,1,1,1,1,0,1,0,1},
        {1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,1},
        {1,0,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1,1,0,1,1,1,1,1,0,1,1,1,0,1},
        {1,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,1,0,1},
        {1,0,1,0,1,1,1,1,1,0,1,0,1,1,1,1,0,1,1,1,1,1,0,1,1,1,0,1,0,1},
        {1,0,0,0,1,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1},
        {1,0,1,1,1,1,1,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,1,1,1,1,1},
        {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
    };

    public Map() {
        applyDecay(0.02, 12345L);
    }

    private void applyDecay(double chance, long seed) {
        Random rnd = new Random(seed);
        int h = grid.length;
        int w = grid[0].length;
        for (int y = 1; y < h-1; y++) {
            for (int x = 1; x < w-1; x++) {
                if (rnd.nextDouble() < chance) {
                    if (grid[y][x] == 1) {
                        int free = countFreeNeighbors(x, y);
                        if (free >= 2) grid[y][x] = 0;
                    } else {
                        int walls = countWallNeighbors(x, y);
                        if (walls >= 3) grid[y][x] = 1;
                    }
                }
            }
        }
    }

    private int countFreeNeighbors(int x, int y) {
        int c = 0;
        for (int yy = y-1; yy <= y+1; yy++) {
            for (int xx = x-1; xx <= x+1; xx++) {
                if (xx == x && yy == y) continue;
                if (grid[yy][xx] == 0) c++;
            }
        }
        return c;
    }

    private int countWallNeighbors(int x, int y) {
        int c = 0;
        for (int yy = y-1; yy <= y+1; yy++) {
            for (int xx = x-1; xx <= x+1; xx++) {
                if (xx == x && yy == y) continue;
                if (grid[yy][xx] == 1) c++;
            }
        }
        return c;
    }

    public boolean isWall(int x, int y) {
        if (y < 0 || y >= grid.length || x < 0 || x >= grid[0].length) return true;
        return grid[y][x] == 1;
    }

    public double castRay(double x, double y, double angle) {
        double dist = 0;
        while (dist < 100) {
            double testX = x + Math.cos(angle) * dist;
            double testY = y + Math.sin(angle) * dist;
            if (isWall((int)testX, (int)testY)) break;
            dist += 0.05;
        }
        return dist;
    }

    public int getWidth() { return grid[0].length; }
    public int getHeight() { return grid.length; }
    public int[][] getGrid() { return grid; }

    public void applyDecayWithSeed(double chance, long seed) {
        applyDecay(chance, seed);
    }
}
