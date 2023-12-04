import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

public class SpaceShooter extends JPanel implements ActionListener, KeyListener {
    private int playerX, playerY;
    private boolean[] keys;
    private Timer timer;
    private List<Bullet> bullets;
    private List<SpaceObject> spaceObjects;
    private int score;
    private int playerLives;
    private int bulletDelay;
    private int maxBulletDelay;

    private BufferedImage spaceshipImage; // Added for the spaceship image
private BufferedImage bulletImage;
private BufferedImage spaceObjectImage;
    public SpaceShooter() {
        keys = new boolean[256];
        playerX = 150;
        playerY = 150;
        bullets = new ArrayList<>();
        spaceObjects = new ArrayList<>();
        score = 0;
        playerLives = 3;
        bulletDelay = 0;
        maxBulletDelay = 30;

        try {
            spaceshipImage = ImageIO.read(new File("C:\\Users\\PC\\Documents\\NetBeansProjects\\mavenproject2\\src\\main\\java\\space2.png"));
            bulletImage = ImageIO.read(new File("C:\\Users\\PC\\Documents\\NetBeansProjects\\mavenproject2\\src\\main\\java\\space.jpg")); // Provide the correct path
            spaceObjectImage = ImageIO.read(new File("C:\\Users\\PC\\Documents\\NetBeansProjects\\mavenproject2\\src\\main\\java\\space.jpg")); // Provide the correct path
    
        } catch (IOException e) {
            e.printStackTrace();
        }

        timer = new Timer(10, this);
        timer.start();

        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        spawnSpaceObjects();
    }

    private void spawnSpaceObjects() {
        int numObjects = 5 + score / 100;
        for (int i = 0; i < numObjects; i++) {
            int objX = (int) (Math.random() * (getWidth() - 20));
            int objY = -20 - (int) (Math.random() * 200);
            spaceObjects.add(new SpaceObject(objX, objY));
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.black);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw the spaceship image
        g.drawImage(spaceshipImage, playerX, playerY, 60, 60, this);

        g.setColor(Color.red);
        for (SpaceObject obj : spaceObjects) {
            g.fillRect(obj.getX(), obj.getY(), 20, 20);
        }

        g.setColor(Color.red);
        for (Bullet bullet : bullets) {
            g.drawImage(bulletImage, bullet.getX(), bullet.getY(), 10, 20, this); // Adjust the width and height as needed
        }

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Lives: " + playerLives, getWidth() - 70, 20);

        if (playerLives <= 0) {
            g.setColor(Color.red);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Over", getWidth() / 2 - 80, getHeight() / 2);
            g.drawString("Press R to Restart", getWidth() / 2 - 110, getHeight() / 2 + 30);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (playerLives > 0) {
            int speed = 5;

            if (keys[KeyEvent.VK_LEFT] && playerX > 0) {
                playerX -= speed;
            }
            if (keys[KeyEvent.VK_RIGHT] && playerX < getWidth() - 20) {
                playerX += speed;
            }
            if (keys[KeyEvent.VK_UP] && playerY > 0) {
                playerY -= speed;
            }
            if (keys[KeyEvent.VK_DOWN] && playerY < getHeight() - 60) {
                playerY += speed;
            }

            if (keys[KeyEvent.VK_SPACE] && bulletDelay <= 0) {
                int bulletX = playerX + (60 )/2;
                int bulletY = playerY;
                bullets.add(new Bullet(bulletX, bulletY));
                bulletDelay = maxBulletDelay;
            }

            if (bulletDelay > 0) {
                bulletDelay--;
            }

            for (SpaceObject obj : spaceObjects) {
                obj.move();
            }

            for (Bullet bullet : bullets) {
                bullet.move();
            }

            bullets.removeIf(bullet -> bullet.getY() < 0);

            Iterator<SpaceObject> objectIterator = spaceObjects.iterator();
            while (objectIterator.hasNext()) {
                SpaceObject obj = objectIterator.next();
                if (obj.getY() >= getHeight()) {
                    objectIterator.remove();
                }
            }

            Iterator<Bullet> bulletIterator = bullets.iterator();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                Iterator<SpaceObject> objectIterator2 = spaceObjects.iterator();
                while (objectIterator2.hasNext()) {
                    SpaceObject obj = objectIterator2.next();
                    if (obj.isCollided(bullet)) {
                        bulletIterator.remove();
                        objectIterator2.remove();
                        score += 10;
                        break;
                    }
                }
            }

            if (spaceObjects.isEmpty()) {
                spawnSpaceObjects();
                if (maxBulletDelay > 10) {
                    maxBulletDelay -= 5;
                }
            }

            Iterator<SpaceObject> objectIterator3 = spaceObjects.iterator();
            while (objectIterator3.hasNext()) {
                SpaceObject obj = objectIterator3.next();
                if (obj.isCollided(playerX, playerY, 20, 20)) {
                    playerLives--;
                    objectIterator3.remove();
                    if (playerLives <= 0) {
                        gameOver();
                    }
                    break;
                }
            }

            repaint();
        }
    }

    private class Bullet {
        private int x, y;
        private static final int BULLET_SPEED = 10;

        public Bullet(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void move() {
            y -= BULLET_SPEED;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    private class SpaceObject {
        private int x, y;
        private int moveX, moveY;

        public SpaceObject(int x, int y) {
            this.x = x;
            this.y = y;
            this.moveX = (int) (Math.random() * 3) - 1;
            this.moveY = (int) (Math.random() * 3) + 1;
        }

        public void move() {
            x += moveX;
            y += moveY;

            x = Math.max(0, Math.min(getWidth() - 20, x));
            y = Math.max(-20, Math.min(getHeight(), y));

            if (y == getHeight()) {
                y = -20;
                x = (int) (Math.random() * (getWidth() - 20));
            }
        }

        public boolean isCollided(Bullet bullet) {
            return x < bullet.getX() + 4 &&
                    x + 20 > bullet.getX() &&
                    y < bullet.getY() + 10 &&
                    y + 20 > bullet.getY();
        }

        public boolean isCollided(int rx, int ry, int rWidth, int rHeight) {
            return x < rx + rWidth &&
                    x + 20 > rx &&
                    y < ry + rHeight &&
                    y + 20 > ry;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    private void restartGame() {
        playerX = 150;
        playerY = 150;
        bullets.clear();
        spaceObjects.clear();
        score = 0;
        playerLives = 3;
        bulletDelay = 0;
        maxBulletDelay = 30;
        spawnSpaceObjects();
        timer.start();
    }

    private void gameOver() {
        timer.stop();
    }

    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;

        if (playerLives <= 0 && e.getKeyCode() == KeyEvent.VK_R) {
            restartGame();
        }
    }

    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
    }

    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Space Shooter");
        SpaceShooter spaceShooter = new SpaceShooter();
        frame.add(spaceShooter);
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
