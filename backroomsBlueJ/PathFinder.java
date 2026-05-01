// PathFinder.java
import java.awt.Point;
import java.util.*;

public class PathFinder {

    private static class Node implements Comparable<Node> {
        int x, y;
        double g;
        double f;
        Node parent;

        Node(int x, int y, double g, double f, Node parent) {
            this.x = x; this.y = y; this.g = g; this.f = f; this.parent = parent;
        }

        @Override
        public int compareTo(Node o) {
            return Double.compare(this.f, o.f);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Node)) return false;
            Node n = (Node)o;
            return n.x == x && n.y == y;
        }

        @Override
        public int hashCode() {
            return x * 31 + y;
        }
    }

    public static List<Point> findPath(Map map, int sx, int sy, int tx, int ty) {
        List<Point> empty = Collections.emptyList();
        if (map.isWall(tx, ty) || map.isWall(sx, sy)) return empty;

        int w = map.getWidth();
        int h = map.getHeight();

        boolean[][] closed = new boolean[h][w];
        double[][] gScore = new double[h][w];
        for (int i = 0; i < h; i++) Arrays.fill(gScore[i], Double.POSITIVE_INFINITY);

        PriorityQueue<Node> open = new PriorityQueue<>();
        double h0 = heuristic(sx, sy, tx, ty);
        Node start = new Node(sx, sy, 0.0, h0, null);
        open.add(start);
        gScore[sy][sx] = 0.0;

        int[][] dirs = { {1,0}, {-1,0}, {0,1}, {0,-1} };

        while (!open.isEmpty()) {
            Node cur = open.poll();
            if (closed[cur.y][cur.x]) continue;
            if (cur.x == tx && cur.y == ty) {
                LinkedList<Point> path = new LinkedList<>();
                Node n = cur;
                while (n.parent != null) {
                    path.addFirst(new Point(n.x, n.y));
                    n = n.parent;
                }
                return path;
            }
            closed[cur.y][cur.x] = true;

            for (int[] d : dirs) {
                int nx = cur.x + d[0];
                int ny = cur.y + d[1];
                if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                if (map.isWall(nx, ny)) continue;
                if (closed[ny][nx]) continue;

                double tentativeG = cur.g + 1.0;
                if (tentativeG < gScore[ny][nx]) {
                    gScore[ny][nx] = tentativeG;
                    double f = tentativeG + heuristic(nx, ny, tx, ty);
                    Node next = new Node(nx, ny, tentativeG, f, cur);
                    open.add(next);
                }
            }
        }

        return empty;
    }

    private static double heuristic(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }
}
