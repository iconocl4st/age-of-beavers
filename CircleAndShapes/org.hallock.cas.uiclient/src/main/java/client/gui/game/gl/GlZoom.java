package client.gui.game.gl;

class GlZoom {
    final Object sync = new Object();

    // Assuming it is a square for now...
    double screenLowerY;
    double screenUpperY;
    double screenLowerX;
    double screenUpperX;

    int screenWidth;
    int screenHeight;

    double aspect;

    double locationX;
    double locationY;
    double locationZ = GlConstants.INITIAL_Z;

    public String toString() {
        return (
            "Location: " + locationX + ", " + locationY + ", " + locationZ + '\n' +
            "Screen size: " + screenWidth + ", " + screenHeight + '\n' +
            "Game Screen Location: " + "[" + screenLowerX + "," + screenLowerY + ":" + screenUpperX + "," + screenUpperY + "]" + '\n'
//            "Game Screen Center: " + screenCenterX + ", " + screenCenterY + '\n'
        );
    }
}
