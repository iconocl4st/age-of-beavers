package common.util;

public interface Marked {

    int getWidth();
    int getHeight();
    boolean get(int x, int y);


    static Marked createMarked(final BitArray.BitArrayView view) {
        return new Marked() {
            @Override
            public int getWidth() {
                return view.getDimension(0);
            }

            @Override
            public int getHeight() {
                return view.getDimension(1);
            }

            @Override
            public boolean get(int x, int y) {
                return view.get(x, y);
            }
        };
    }
}
