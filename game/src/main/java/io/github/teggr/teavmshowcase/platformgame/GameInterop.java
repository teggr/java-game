package io.github.teggr.teavmshowcase.platformgame;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

final class GameInterop {
    @JSFunctor
    interface AnimationFrameCallback extends JSObject {
        void onAnimationFrame(double timestamp);
    }

    private GameInterop() {
    }

    @JSBody(params = "callback", script = "window.requestAnimationFrame(callback);")
    static native void requestAnimationFrame(AnimationFrameCallback callback);
}
