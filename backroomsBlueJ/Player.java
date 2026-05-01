// Player.java
public class Player {
    public double x, y;
    public double angle;

    public Player(double x, double y, double angle) {
        this.x = x;
        this.y = y;
        this.angle = angle;
    }

    public void moveForward(double step, Map map) {
        double newX = x + Math.cos(angle) * step;
        double newY = y + Math.sin(angle) * step;
        if (!map.isWall((int)newX, (int)newY)) {
            x = newX;
            y = newY;
        }
    }

    public void strafe(double step, Map map) {
        double newX = x + Math.cos(angle + Math.PI/2) * step;
        double newY = y + Math.sin(angle + Math.PI/2) * step;
        if (!map.isWall((int)newX, (int)newY)) {
            x = newX;
            y = newY;
        }
    }

    public void rotate(double deltaAngle) {
        angle += deltaAngle;
    }
}
