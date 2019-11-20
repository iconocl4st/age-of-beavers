package common.util.query;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

public class KeepSmallest<T> {

    private final TreeSet<Node> set = new TreeSet<>();
    private final int numToKeep;

    public KeepSmallest(int numToKeep) {
        this.numToKeep = numToKeep;
    }

    public void add(double val, T t) {
        set.add(new Node(val, t));
        while (set.size() > numToKeep) {
            set.pollLast();
        }
    }

    public double bound() {
        if (set.size() == numToKeep) return set.last().val;
        return Double.MAX_VALUE;
    }

    public List<T> toList() {
        LinkedList<T> ret = new LinkedList<>();
        for (Node node : set) {
            ret.add(node.t);
        }
        return ret;
    }


    private final class Node implements Comparable<Node> {
        private double val;
        private T t;

        public Node(double value, T t) {
            this.val = value;
            this.t =  t;
        }

        @Override
        public int compareTo(Node node) {
            return Double.compare(val, node.val);
        }
    }
}
