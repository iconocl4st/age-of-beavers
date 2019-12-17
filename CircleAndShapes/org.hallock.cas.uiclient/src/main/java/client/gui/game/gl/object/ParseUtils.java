package client.gui.game.gl.object;

class ParseUtils {
    static final double[][] DOUBLE_2_DUMMY = new double[0][0];
    static final int[][][] INT_3_DUMMY = new int[0][0][0];

    static StringBuilder toString(StringBuilder builder, String initial, double[][] arr) {
        for (double[] anArr : arr) {
            toString(builder, initial, anArr);
        }
        return builder;
    }

    static StringBuilder toString(StringBuilder builder, String initial, double[] arr) {
        builder.append(initial);
        for (int i = 0; i < arr.length; i++) {
            builder.append(arr[i]);
            if (i != arr.length - 1) {
                builder.append(' ');
            }
        }
        builder.append('\n');
        return builder;
    }

    static double[] parseDoubleArray(String[] components, int bIdx, int eIdx) {
        double[] ret = new double[eIdx - bIdx];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = Double.valueOf(components[bIdx + i]);
        }
        return ret;
    }

    static int[][] parseFace(String[] components, int bIdx, int eIdx) {
        int[][] ret = new int[eIdx - bIdx][3];
        for (int i = 0; i < ret.length; i++) {
            String[] args = components[bIdx + i].split("/");
            for (int j = 0;j<args.length;j++)
               ret[i][j] = Integer.valueOf(args[j]);
        }
        return ret;
    }
}
