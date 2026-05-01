// Enemy.java
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.Clip;

public class Enemy {
    private double x, y;
    private double speed = 1.2;
    private BufferedImage texture;
    private long lastStepTime = 0;
    private Clip loopClip;
    private Clip[] stepHolder = new Clip[1];

    // Pathfinding
    private java.util.List<Point> path = null;
    private int pathIndex = 0;
    private long lastPathCalcTime = 0;
    private static final long PATH_RECALC_MS = 500;
    private int lastPlayerCellX = -1, lastPlayerCellY = -1;

    // Step volume parameters
    private static final double ALPHA_STEP = 1.8;
    private static final double STEP_MAX_VOLUME = 0.20;
    private static final double STEP_PLAY_THRESHOLD = 0.02;

    public Enemy(Map map, Player player) {
        Random rand = new Random();
        int ex, ey;
        do {
            ex = rand.nextInt(map.getWidth());
            ey = rand.nextInt(map.getHeight());
        } while (map.isWall(ex, ey) || (Math.abs(ex - (int)player.x) < 2 && Math.abs(ey - (int)player.y) < 2));
        this.x = ex + 0.5;
        this.y = ey + 0.5;
        try {
            texture = ImageIO.read(getClass().getResourceAsStream("/images/enemy.png"));
        } catch (Exception e) {
            texture = null;
        }
        try {
            loopClip = SoundPlayer.loadClipFromResource("/sounds/enemy_loop.wav");
            SoundPlayer.setClipVolumeLinear(loopClip, 0.0);
            loopClip.loop(Clip.LOOP_CONTINUOUSLY);
            loopClip.start();
        } catch (Exception e) {
            loopClip = null;
            e.printStackTrace();
        }
    }

    public void update(Player player, Map map) {
        double dx = player.x - x;
        double dy = player.y - y;
        double distance = Math.sqrt(dx*dx + dy*dy);

        int playerCellX = (int)player.x;
        int playerCellY = (int)player.y;
        long now = System.currentTimeMillis();
        if (now - lastPathCalcTime > PATH_RECALC_MS || playerCellX != lastPlayerCellX || playerCellY != lastPlayerCellY) {
            recalcPath(map, playerCellX, playerCellY);
            lastPathCalcTime = now;
            lastPlayerCellX = playerCellX;
            lastPlayerCellY = playerCellY;
        }

        boolean moved = false;
        if (path != null && pathIndex < path.size()) {
            Point targetCell = path.get(pathIndex);
            double targetX = targetCell.x + 0.5;
            double targetY = targetCell.y + 0.5;
            double vx = targetX - x;
            double vy = targetY - y;
            double distToWaypoint = Math.sqrt(vx*vx + vy*vy);
            if (distToWaypoint < 0.12) {
                pathIndex++;
            } else {
                double nx = vx / distToWaypoint;
                double ny = vy / distToWaypoint;
                double step = speed * 0.05;
                double newX = x + nx * step;
                double newY = y + ny * step;
                if (!map.isWall((int)newX, (int)y)) x = newX;
                if (!map.isWall((int)x, (int)newY)) y = newY;
                moved = true;
            }
        }

        if (!moved) {
            if (distance > 0.1) {
                double nx = dx / distance;
                double ny = dy / distance;
                double newX = x + nx * speed * 0.05;
                double newY = y + ny * speed * 0.05;
                if (!map.isWall((int)newX, (int)y)) x = newX;
                if (!map.isWall((int)x, (int)newY)) y = newY;
            }
        }

        if (loopClip != null) {
            double linearGain = Math.exp(-0.35 * distance);
            if (linearGain < 0.0005) linearGain = 0.0;
            SoundPlayer.setClipVolumeLinear(loopClip, linearGain);
        }

        if (moved) playStepSound(distance);
    }

    private void recalcPath(Map map, int playerCellX, int playerCellY) {
        int sx = (int) x;
        int sy = (int) y;
        if (map.isWall(sx, sy) || map.isWall(playerCellX, playerCellY)) {
            path = null;
            pathIndex = 0;
            return;
        }
        List<Point> newPath = PathFinder.findPath(map, sx, sy, playerCellX, playerCellY);
        if (newPath == null || newPath.isEmpty()) {
            path = null;
            pathIndex = 0;
        } else {
            path = newPath;
            pathIndex = 0;
        }
    }

    private void playStepSound(double distance) {
        long now = System.currentTimeMillis();
        if (now - lastStepTime < 600) return;
        lastStepTime = now;

        double linearGain = Math.exp(-ALPHA_STEP * distance);
        linearGain = Math.pow(linearGain, 1.0);
        linearGain *= STEP_MAX_VOLUME;

        if (linearGain < STEP_PLAY_THRESHOLD) return;

        SoundPlayer.playOneShotNoOverlapLinear("/sounds/enemy_step.wav", linearGain, stepHolder);
    }

    public boolean checkCollision(Player player) {
        double dx = player.x - x;
        double dy = player.y - y;
        double distance = Math.sqrt(dx*dx + dy*dy);
        return distance < 0.5;
    }

    public void draw(Graphics g, Player player, Map map, int screenWidth, int screenHeight) {
        if (texture == null) return;
        double dx = x - player.x;
        double dy = y - player.y;
        double angleToEnemy = Math.atan2(dy, dx) - player.angle;
        if (angleToEnemy < -Math.PI) angleToEnemy += 2*Math.PI;
        if (angleToEnemy > Math.PI) angleToEnemy -= 2*Math.PI;
        if (angleToEnemy > -Math.PI/6 && angleToEnemy < Math.PI/6) {
            double distToEnemy = Math.sqrt(dx*dx + dy*dy);
            double distToWall  = map.castRay(player.x, player.y, player.angle + angleToEnemy);
            if (distToEnemy < distToWall) {
                int screenX = (int)((screenWidth/2) + Math.tan(angleToEnemy) * screenWidth/2);
                int spriteHeight = (int)(screenHeight / distToEnemy);
                g.drawImage(texture, screenX - spriteHeight/2, screenHeight/2 - spriteHeight/2, spriteHeight, spriteHeight, null);
            }
        }
    }

    public void stopSounds() {
        if (loopClip != null) {
            if (loopClip.isRunning()) loopClip.stop();
            loopClip.close();
            loopClip = null;
        }
        SoundPlayer.stopAndClose(stepHolder);
    }
}
