package common.algo;

import java.util.HashSet;

public class OneDUnionFind {
    private int[] roots;
    private int size;

    public OneDUnionFind(int len) {
        roots = new int[len];
        this.size = len;
        clear();
    }

    public void clear() {
        for (int i = 0; i < roots.length; i++)
            roots[i] = i;
    }

    public void add(int numberToAdd) {

    }

    public void remove(int index) {
        for (int i = 0; i < size; i++) {
            if (roots[i] == index) {
                roots[i] = i;
            }
        }
    }

    public int getRoot(int i) {
        int or = roots[i];
        if (or == i) return i;
        int nr = getRoot(or);
        if (or != nr) roots[i] = nr;
        return nr;
    }

    public boolean areConnected(int i, int j) {
        return getRoot(i) == getRoot(j);
    }

    public void connect(int i, int j) {
        roots[getRoot(i)] = getRoot(j);
    }

    public int numSets() {
        HashSet<Integer> ret = new HashSet<>();
        for (int i = 0; i < roots.length;i++) {
            ret.add(getRoot(i));
        }
        return ret.size();
    }
}
