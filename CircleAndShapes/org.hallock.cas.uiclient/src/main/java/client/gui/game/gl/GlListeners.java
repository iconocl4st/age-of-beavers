package client.gui.game.gl;

public interface GlListeners {
    interface GameMousePressListener {
        void mousePressed(double x, double y, PressInfo info);
        void mouseReleased(double x, double y, PressInfo info);
    }

    interface RectangleHandler {
        void run(double xBegin, double yBegin, double xEnd, double yEnd);
    }

    class PressInfo {
        public final boolean isLeftButton;
        public final boolean isMiddleButton;
        public final boolean isRightButton;
        public final boolean isControl;
        public final boolean isShift;
        public final int clickCount;

        PressInfo(boolean isLeftPress, boolean isMiddlePress, boolean isRightPress, boolean isControl, boolean isShift, int clickCount) {
            this.isLeftButton = isLeftPress;
            this.isMiddleButton = isMiddlePress;
            this.isRightButton = isRightPress;
            this.isControl = isControl;
            this.isShift = isShift;
            this.clickCount = clickCount;
        }
    }
}
