package client.gui.game.gl;

public interface GlListeners {
    interface GameMousePressListener {
        void mousePressed(double x, double y, PressInfo info);
    }

    interface RectangleHandler {
        void run(double xBegin, double yBegin, double xEnd, double yEnd);
    }

    class PressInfo {
        public final boolean isLeftPress;
        public final boolean isMiddlePress;
        public final boolean isRightPress;

        // is control...
        // is shift...

        PressInfo(boolean isLeftPress, boolean isMiddlePress, boolean isRightPress) {
            this.isLeftPress = isLeftPress;
            this.isMiddlePress = isMiddlePress;
            this.isRightPress = isRightPress;
        }
    }
}
