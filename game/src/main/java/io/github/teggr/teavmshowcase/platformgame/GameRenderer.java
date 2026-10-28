package io.github.teggr.teavmshowcase.platformgame;

import org.teavm.jso.canvas.CanvasRenderingContext2D;

final class GameRenderer {
    private final CanvasRenderingContext2D context;

    GameRenderer(CanvasRenderingContext2D context) {
        this.context = context;
    }

    void render(PlatformGame game, double timeSeconds) {
        drawSky(timeSeconds);
        drawPlatforms();
        drawCoins(game, timeSeconds);
        drawPlayer(game, timeSeconds);
        if (game.isComplete()) {
            drawVictory();
        }
    }

    private void drawSky(double time) {
        context.setFillStyle("#b9e8ff");
        context.fillRect(0, 0, PlatformGame.SCENE_WIDTH, PlatformGame.SCENE_HEIGHT);
        context.setFillStyle("#dcf4ff");
        for (int i = 0; i < 4; i++) {
            double x = (i * 285 + 75 + time * (8 + i * 2)) % 1120 - 100;
            double y = 65 + i % 2 * 58;
            context.beginPath();
            context.arc(x, y, 24, 0, Math.PI * 2);
            context.arc(x + 25, y - 10, 30, 0, Math.PI * 2);
            context.arc(x + 54, y, 22, 0, Math.PI * 2);
            context.fill();
        }
        context.setFillStyle("#9bd6cb");
        context.beginPath();
        context.moveTo(0, 420);
        context.lineTo(180, 260);
        context.lineTo(340, 420);
        context.lineTo(520, 280);
        context.lineTo(730, 420);
        context.lineTo(900, 295);
        context.lineTo(960, 350);
        context.lineTo(960, 540);
        context.lineTo(0, 540);
        context.fill();
        context.setFillStyle("#74c7a5");
        context.fillRect(0, 488, PlatformGame.SCENE_WIDTH, 52);
    }

    private void drawPlatforms() {
        for (Platform platform : PlatformGame.platforms()) {
            if (platform.y == 488) {
                continue;
            }
            context.setFillStyle("#bd7748");
            context.fillRect(platform.x, platform.y, platform.width, 18);
            context.setFillStyle("#f4ba67");
            context.fillRect(platform.x, platform.y, platform.width, 7);
            context.setFillStyle("#d99051");
            for (double brickX = platform.x + 8; brickX < platform.x + platform.width; brickX += 34) {
                context.fillRect(brickX, platform.y + 10, 2, 8);
            }
        }
        context.setFillStyle("#4aa875");
        context.fillRect(0, 488, PlatformGame.SCENE_WIDTH, 8);
    }

    private void drawCoins(PlatformGame game, double time) {
        double[][] coins = PlatformGame.coins();
        for (int i = 0; i < coins.length; i++) {
            if (game.isCoinCollected(i)) {
                continue;
            }
            double bob = Math.sin(time * 4 + i) * 3;
            context.setFillStyle("#f6a623");
            context.beginPath();
            context.arc(coins[i][0], coins[i][1] + bob, 12, 0, Math.PI * 2);
            context.fill();
            context.setStrokeStyle("#fff1a6");
            context.setLineWidth(3);
            context.beginPath();
            context.arc(coins[i][0], coins[i][1] + bob, 6, 0, Math.PI * 2);
            context.stroke();
        }
    }

    private void drawPlayer(PlatformGame game, double time) {
        double x = game.getPlayerX();
        double y = game.getPlayerY();
        double bounce = game.isGrounded() ? Math.sin(time * 12) * 1.5 : 0;
        context.setFillStyle("#e85b48");
        context.fillRect(x + 4, y + 8 + bounce, 26, 29);
        context.setFillStyle("#bd3f39");
        context.fillRect(x + 1, y + 3 + bounce, 32, 9);
        context.fillRect(x + 6, y + 36 + bounce, 9, 8);
        context.fillRect(x + 22, y + 36 + bounce, 9, 8);
        context.setFillStyle("#fff8e8");
        context.fillRect(x + 21, y + 15 + bounce, 6, 8);
        context.setFillStyle("#25354a");
        context.fillRect(x + 24, y + 18 + bounce, 3, 4);
    }

    private void drawVictory() {
        context.setFillStyle("rgba(25, 43, 67, 0.72)");
        context.fillRect(0, 0, PlatformGame.SCENE_WIDTH, PlatformGame.SCENE_HEIGHT);
        context.setTextAlign("center");
        context.setFillStyle("#fff8e8");
        context.setFont("700 48px system-ui, sans-serif");
        context.fillText("All stars collected!", PlatformGame.SCENE_WIDTH / 2, 240);
        context.setFont("500 22px system-ui, sans-serif");
        context.fillText("Nice jumping. Press Play again to run it back.", PlatformGame.SCENE_WIDTH / 2, 283);
    }
}
