// GamePanel.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;
import java.io.IOException;

public class GamePanel extends JPanel implements KeyListener {
    private Player player;
    private Map map;
    private Enemy enemy;
    private boolean gameOver = false;
    private BufferedImage wallTexture;
    private BufferedImage floorTexture;
    private BufferedImage ceilingTexture;
    private boolean upPressed, downPressed, leftPressed, rightPressed;
    private boolean rotateLeft, rotateRight;
    private boolean lookUp, lookDown;
    private double cameraPitch = 0;
    private long lastStepTime = 0;
    private Timer timer;
    private Clip ambienceClip;
    private Clip[] playerStepHolder = new Clip[1];

    public GamePanel() {
        map = new Map();
        player = new Player(3.5, 3.5, 0);
        enemy = new Enemy(map, player);
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        try {
            wallTexture    = ImageIO.read(getClass().getResourceAsStream("/images/wall_texture.png"));
            floorTexture   = ImageIO.read(getClass().getResourceAsStream("/images/floor_texture.png"));
            ceilingTexture = ImageIO.read(getClass().getResourceAsStream("/images/ceiling_texture.png"));
        } catch (Exception e) {
            wallTexture = floorTexture = ceilingTexture = null;
        }
        ambienceClip = SoundPlayer.loopClip("/sounds/lights.wav", -20f);
        timer = new Timer(16, e -> updateGame());
        timer.start();
    }

    private void updateGame() {
        if (gameOver) return;
        boolean moved = false;
        if (upPressed)    { player.moveForward(0.2, map); moved = true; }
        if (downPressed)  { player.moveForward(-0.2, map); moved = true; }
        if (leftPressed)  { player.strafe(-0.2, map); moved = true; }
        if (rightPressed) { player.strafe(0.2, map); moved = true; }
        if (rotateLeft)   player.rotate(-0.15);
        if (rotateRight)  player.rotate(0.15);
        if (lookUp)       cameraPitch = Math.min(cameraPitch + 0.02, 1.0);
        if (lookDown)     cameraPitch = Math.max(cameraPitch - 0.02, -1.0);
        if (moved) playPlayerStepSound();
        else SoundPlayer.stopAndClose(playerStepHolder);
        enemy.update(player, map);
        if (enemy.checkCollision(player)) {
            gameOver = true;
            SoundPlayer.playOneShotNoOverlap("/sounds/death.wav", -6.0f, new Clip[1]);
        }
        repaint();
    }

    private void playPlayerStepSound() {
        long now = System.currentTimeMillis();
        if (now - lastStepTime > 400) {
            SoundPlayer.playOneShotNoOverlap("/sounds/footstep.wav", -6.0f, playerStepHolder);
            lastStepTime = now;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width  = getWidth();
        int height = getHeight();
        int horizon = (int)(height / 2 + cameraPitch * 100);
        double rayDirX0 = Math.cos(player.angle - Math.PI/6);
        double rayDirY0 = Math.sin(player.angle - Math.PI/6);
        double rayDirX1 = Math.cos(player.angle + Math.PI/6);
        double rayDirY1 = Math.sin(player.angle + Math.PI/6);
        for (int y = 0; y < height; y++) {
            boolean isFloor = y > horizon;
            int p = isFloor ? (y - horizon) : (horizon - y);
            if (p == 0) continue;
            double posZ = 0.5 * height;
            double rowDistance = posZ / p;
            double floorStepX = rowDistance * (rayDirX1 - rayDirX0) / width;
            double floorStepY = rowDistance * (rayDirY1 - rayDirY0) / width;
            double floorX = player.x + rowDistance * rayDirX0;
            double floorY = player.y + rowDistance * rayDirY0;
            for (int x = 0; x < width; ++x) {
                if (isFloor && floorTexture != null) {
                    int tx = (int)(floorTexture.getWidth()  * (floorX - Math.floor(floorX))) & (floorTexture.getWidth()-1);
                    int ty = (int)(floorTexture.getHeight() * (floorY - Math.floor(floorY))) & (floorTexture.getHeight()-1);
                    g.drawImage(floorTexture, x, y, x+1, y+1, tx, ty, tx+1, ty+1, this);
                } else if (!isFloor && ceilingTexture != null) {
                    int tx = (int)(ceilingTexture.getWidth()  * (floorX - Math.floor(floorX))) & (ceilingTexture.getWidth()-1);
                    int ty = (int)(ceilingTexture.getHeight() * (floorY - Math.floor(floorY))) & (ceilingTexture.getHeight()-1);
                    g.drawImage(ceilingTexture, x, y, x+1, y+1, tx, ty, tx+1, ty+1, this);
                }
                floorX += floorStepX;
                floorY += floorStepY;
            }
        }
        if (wallTexture != null) {
            for (int x = 0; x < width; x++) {
                double rayAngle = (player.angle - Math.PI / 6) + (x * (Math.PI / 3) / width);
                double dist = map.castRay(player.x, player.y, rayAngle);
                if (dist <= 0) continue;
                int wallHeight = (int) (height / dist);
                int yStart = Math.max(0, (horizon) - (wallHeight / 2));
                int yEnd   = Math.min(height, (horizon) + (wallHeight / 2));
                double hitX = player.x + dist * Math.cos(rayAngle);
                double hitY = player.y + dist * Math.sin(rayAngle);
                boolean hitVertical = Math.abs(hitX - Math.round(hitX)) < Math.abs(hitY - Math.round(hitY));
                int textureX;
                if (hitVertical) {
                    double offset = hitY - Math.floor(hitY);
                    textureX = (int)(offset * wallTexture.getWidth());
                } else {
                    double offset = hitX - Math.floor(hitX);
                    textureX = (int)(offset * wallTexture.getWidth());
                }
                for (int y = yStart; y < yEnd; y++) {
                    int textureY = (int)(((y - (horizon - wallHeight / 2)) / (double) wallHeight) * wallTexture.getHeight());
                    g.drawImage(wallTexture, x, y, x+1, y+1, textureX, textureY, textureX+1, textureY+1, this);
                }
            }
        }
        enemy.draw(g, player, map, width, height);
        if (gameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("GAME OVER", width/2 - 100, height/2);
            g.setFont(new Font("Arial", Font.PLAIN, 18));
            g.drawString("Drücke R zum Neustarten", width/2 - 120, height/2 + 30);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: upPressed = true; break;
            case KeyEvent.VK_S: downPressed = true; break;
            case KeyEvent.VK_A: leftPressed = true; break;
            case KeyEvent.VK_D: rightPressed = true; break;
            case KeyEvent.VK_LEFT:  rotateLeft = true; break;
            case KeyEvent.VK_RIGHT: rotateRight = true; break;
            case KeyEvent.VK_UP:    lookUp = true; break;
            case KeyEvent.VK_DOWN:  lookDown = true; break;
            case KeyEvent.VK_R:     if (gameOver) resetGame(); break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W: upPressed = false; break;
            case KeyEvent.VK_S: downPressed = false; break;
            case KeyEvent.VK_A: leftPressed = false; break;
            case KeyEvent.VK_D: rightPressed = false; break;
            case KeyEvent.VK_LEFT:  rotateLeft = false; break;
            case KeyEvent.VK_RIGHT: rotateRight = false; break;
            case KeyEvent.VK_UP:    lookUp = false; break;
            case KeyEvent.VK_DOWN:  lookDown = false; break;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}

    private void resetGame() {
        player = new Player(3.5, 3.5, 0);
        enemy.stopSounds();
        enemy = new Enemy(map, player);
        gameOver = false;
        lastStepTime = 0;
        SoundPlayer.stopAndClose(playerStepHolder);
    }

    public void stop() {
        if (timer != null) timer.stop();
        if (ambienceClip != null) {
            if (ambienceClip.isRunning()) ambienceClip.stop();
            ambienceClip.close();
            ambienceClip = null;
        }
        enemy.stopSounds();
        SoundPlayer.stopAndClose(playerStepHolder);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Backrooms Spiel");
            GamePanel gamePanel = new GamePanel();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.add(gamePanel);
            frame.setVisible(true);
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    gamePanel.stop();
                }
            });
        });
    }
}
