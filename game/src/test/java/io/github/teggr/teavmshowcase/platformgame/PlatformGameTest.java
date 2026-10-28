package io.github.teggr.teavmshowcase.platformgame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlatformGameTest {
    @Test
    void movesHorizontallyAndKeepsPlayerInsideTheScreen() {
        PlatformGame game = new PlatformGame();
        game.setRightPressed(true);
        game.update(0.1);
        assertTrue(game.getPlayerX() > 45);

        game.setLeftPressed(true);
        game.setRightPressed(false);
        for (int i = 0; i < 100; i++) {
            game.update(0.05);
        }
        assertEquals(0, game.getPlayerX());
    }

    @Test
    void jumpsAndLandsOnTheFirstPlatform() {
        PlatformGame game = new PlatformGame();
        game.setRightPressed(true);
        for (int i = 0; i < 6; i++) {
            game.update(0.05);
        }
        game.setRightPressed(false);
        assertTrue(game.isGrounded());
        assertEquals(45 + 285 * 0.05 * 6, game.getPlayerX(), 0.001);

        game.jump();
        assertFalse(game.isGrounded());
        assertTrue(game.getVelocityY() < 0);
        for (int i = 0; i < 80; i++) {
            game.update(0.025);
        }
        assertTrue(game.isGrounded());
        assertEquals(395 - PlatformGame.PLAYER_HEIGHT, game.getPlayerY(), 0.001);
    }

    @Test
    void doesNotJumpAgainUntilGroundedAndResetsAllProgress() {
        PlatformGame game = new PlatformGame();
        game.jump();
        double initialVelocity = game.getVelocityY();
        game.jump();
        assertEquals(initialVelocity, game.getVelocityY());
        game.reset();

        assertTrue(game.isGrounded());
        assertEquals(0, game.getCoinCount());
        assertFalse(game.isComplete());
        assertEquals(45, game.getPlayerX());
    }
}
