package common.util;

import common.state.spec.ResourceType;

import java.util.*;

public class Util {
    private static String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUIVWXYZ0123456789";
    public static String createRandomString(Random random, int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(chars.charAt(random.nextInt(chars.length())));
        }
        return builder.toString();
    }


    public static class IndexIterator implements Iterator<int[]> {
        private int[] maxs;
        private int[] current;
        private boolean rev;

        public IndexIterator(int[] upper) {
            this(upper, false);
        }
        public IndexIterator(int[] upper, boolean rev) {
            this.rev = rev;
            this.maxs = Arrays.copyOf(upper, upper.length);
            this.current = new int[upper.length];
            this.current[current.length - 1] = -1;
        }


        @Override
        public boolean hasNext() {
            for (int i = 0; i < maxs.length; i++) {
                if (current[i] < maxs[i] - 1) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int[] next() {
            int idx = rev ? 0 : current.length - 1;
            int inc = rev ? 1 : -1;
            while (idx >= 0 && idx < current.length && current[idx] == maxs[idx] - 1) {
                current[idx] = 0;
                idx += inc;
            }
            if (idx < 0 || idx >= current.length) {
                throw new IllegalStateException("Called next while hasNext() is false");
            }
            current[idx]++;
            return Arrays.copyOf(current, current.length);
        }

        @Override
        public void remove() {
            throw new RuntimeException("Since when was this a method I had to implement?");
        }
    }
}
