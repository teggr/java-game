package io.github.teggr.teavmshowcase.platformgame;

import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

public final class PlatformGameApp {
    private final PlatformGame game = new PlatformGame();
    private final GameRenderer renderer;
    private final HTMLElement score;
    private double previousFrame;

    private PlatformGameApp() {
        HTMLDocument document = HTMLDocument.current();
        HTMLElement canvasElement = requireElement(document, "game-canvas");
        if (!(canvasElement instanceof HTMLCanvasElement)) {
            throw new IllegalStateException("Page element must be a canvas: game-canvas");
        }
        HTMLCanvasElement canvas = (HTMLCanvasElement) canvasElement;
        CanvasRenderingContext2D context = (CanvasRenderingContext2D) canvas.getContext("2d");
        if (context == null) {
            throw new IllegalStateException("The browser could not create a 2D canvas context.");
        }
        renderer = new GameRenderer(context);
        score = requireElement(document, "score");
        registerKeyboard(document);
        registerControls(document);
        requireElement(document, "restart").addEventListener("click", event -> restart());
        GameInterop.requestAnimationFrame(this::animate);
    }

    public static void main(String[] args) {
        new PlatformGameApp();
    }

    private static HTMLElement requireElement(HTMLDocument document, String id) {
        HTMLElement element = document.getElementById(id);
        if (element == null) {
            throw new IllegalStateException("Required page element is missing: " + id);
        }
        return element;
    }

    private void registerKeyboard(HTMLDocument document) {
        document.addEventListener("keydown", event -> {
            String key = ((KeyboardEvent) event).getKey();
            if (isGameKey(key)) {
                event.preventDefault();
                setKey(key, true);
            }
        });
        document.addEventListener("keyup", event -> setKey(((KeyboardEvent) event).getKey(), false));
        document.getBody().addEventListener("blur", event -> releaseControls());
    }

    private void registerControls(HTMLDocument document) {
        registerHoldButton(document, "move-left", -1);
        registerHoldButton(document, "move-right", 1);
        HTMLElement jump = requireElement(document, "jump");
        jump.addEventListener("pointerdown", event -> {
            event.preventDefault();
            game.jump();
        });
        jump.addEventListener("click", event -> game.jump());
    }

    private void registerHoldButton(HTMLDocument document, String id, int direction) {
        HTMLElement button = requireElement(document, id);
        button.addEventListener("pointerdown", event -> {
            event.preventDefault();
            setDirection(direction, true);
        });
        button.addEventListener("pointerup", event -> setDirection(direction, false));
        button.addEventListener("pointerleave", event -> setDirection(direction, false));
        button.addEventListener("pointercancel", event -> setDirection(direction, false));
    }

    private void setKey(String key, boolean pressed) {
        if ("ArrowLeft".equals(key) || "a".equalsIgnoreCase(key)) {
            game.setLeftPressed(pressed);
        } else if ("ArrowRight".equals(key) || "d".equalsIgnoreCase(key)) {
            game.setRightPressed(pressed);
        } else if (pressed && ("ArrowUp".equals(key) || "w".equalsIgnoreCase(key) || " ".equals(key))) {
            game.jump();
        } else if (pressed && "r".equalsIgnoreCase(key)) {
            restart();
        }
    }

    private void setDirection(int direction, boolean pressed) {
        if (direction < 0) {
            game.setLeftPressed(pressed);
        } else {
            game.setRightPressed(pressed);
        }
    }

    private boolean isGameKey(String key) {
        return "ArrowLeft".equals(key) || "ArrowRight".equals(key) || "ArrowUp".equals(key)
                || " ".equals(key) || "a".equalsIgnoreCase(key) || "d".equalsIgnoreCase(key)
                || "w".equalsIgnoreCase(key) || "r".equalsIgnoreCase(key);
    }

    private void releaseControls() {
        game.setLeftPressed(false);
        game.setRightPressed(false);
    }

    private void restart() {
        game.reset();
        previousFrame = 0;
        updateScore();
    }

    private void animate(double timestamp) {
        double elapsed = previousFrame == 0 ? 0 : (timestamp - previousFrame) / 1000;
        previousFrame = timestamp;
        game.update(elapsed);
        renderer.render(game, timestamp / 1000);
        updateScore();
        GameInterop.requestAnimationFrame(this::animate);
    }

    private void updateScore() {
        score.setTextContent(game.getCoinCount() + " / " + game.getCoinTotal() + " stars");
    }
}
