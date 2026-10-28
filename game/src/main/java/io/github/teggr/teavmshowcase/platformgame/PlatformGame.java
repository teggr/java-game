package io.github.teggr.teavmshowcase.platformgame;

public final class PlatformGame {
    public static final double SCENE_WIDTH = 960;
    public static final double SCENE_HEIGHT = 540;
    public static final double PLAYER_WIDTH = 34;
    public static final double PLAYER_HEIGHT = 44;
    private static final double FLOOR_Y = 488;
    private static final double PLAYER_SPEED = 285;
    private static final double JUMP_SPEED = 700;
    private static final double GRAVITY = 1850;
    private static final double COIN_RADIUS = 12;
    private static final Platform[] PLATFORMS = {
            new Platform(0, FLOOR_Y, SCENE_WIDTH),
            new Platform(105, 395, 190),
            new Platform(385, 315, 175),
            new Platform(650, 235, 190),
            new Platform(755, 385, 140),
            new Platform(345, 205, 115)
    };
    private static final double[][] COINS = {
            { 190, 365 }, { 445, 285 }, { 700, 205 }, { 815, 355 }, { 390, 175 }
    };

    private double playerX;
    private double playerY;
    private double velocityY;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean grounded;
    private boolean complete;
    private final boolean[] collectedCoins = new boolean[COINS.length];
    private int coinCount;

    public PlatformGame() {
        reset();
    }

    public void reset() {
        playerX = 45;
        playerY = FLOOR_Y - PLAYER_HEIGHT;
        velocityY = 0;
        leftPressed = false;
        rightPressed = false;
        grounded = true;
        complete = false;
        coinCount = 0;
        for (int i = 0; i < collectedCoins.length; i++) {
            collectedCoins[i] = false;
        }
    }

    public void setLeftPressed(boolean pressed) {
        leftPressed = pressed;
    }

    public void setRightPressed(boolean pressed) {
        rightPressed = pressed;
    }

    public void jump() {
        if (grounded && !complete) {
            velocityY = -JUMP_SPEED;
            grounded = false;
        }
    }

    public void update(double deltaSeconds) {
        if (complete || deltaSeconds <= 0) {
            return;
        }
        double elapsed = Math.min(deltaSeconds, 0.05);
        double direction = (rightPressed ? 1 : 0) - (leftPressed ? 1 : 0);
        playerX = Math.max(0, Math.min(SCENE_WIDTH - PLAYER_WIDTH, playerX + direction * PLAYER_SPEED * elapsed));

        double previousBottom = playerY + PLAYER_HEIGHT;
        velocityY += GRAVITY * elapsed;
        playerY += velocityY * elapsed;
        grounded = false;
        if (velocityY >= 0) {
            double currentBottom = playerY + PLAYER_HEIGHT;
            for (Platform platform : PLATFORMS) {
                if (previousBottom <= platform.y && currentBottom >= platform.y
                        && playerX + PLAYER_WIDTH > platform.x
                        && playerX < platform.x + platform.width) {
                    playerY = platform.y - PLAYER_HEIGHT;
                    velocityY = 0;
                    grounded = true;
                    break;
                }
            }
        }

        collectCoins();
    }

    private void collectCoins() {
        double closestX;
        double closestY;
        for (int i = 0; i < COINS.length; i++) {
            if (collectedCoins[i]) {
                continue;
            }
            closestX = Math.max(playerX, Math.min(COINS[i][0], playerX + PLAYER_WIDTH));
            closestY = Math.max(playerY, Math.min(COINS[i][1], playerY + PLAYER_HEIGHT));
            double dx = COINS[i][0] - closestX;
            double dy = COINS[i][1] - closestY;
            if (dx * dx + dy * dy <= COIN_RADIUS * COIN_RADIUS) {
                collectedCoins[i] = true;
                coinCount++;
                if (coinCount == COINS.length) {
                    complete = true;
                    leftPressed = false;
                    rightPressed = false;
                }
            }
        }
    }

    public double getPlayerX() {
        return playerX;
    }

    public double getPlayerY() {
        return playerY;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public boolean isGrounded() {
        return grounded;
    }

    public boolean isComplete() {
        return complete;
    }

    public int getCoinCount() {
        return coinCount;
    }

    public int getCoinTotal() {
        return COINS.length;
    }

    static Platform[] platforms() {
        return PLATFORMS;
    }

    static double[][] coins() {
        return COINS;
    }

    boolean isCoinCollected(int index) {
        return collectedCoins[index];
    }
}
