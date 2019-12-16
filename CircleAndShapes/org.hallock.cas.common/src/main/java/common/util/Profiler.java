package common.util;

import common.state.edit.P;

import java.util.*;


public class Profiler {
    private final Object sync = new Object();
    private final HashMap<String, CallTracker> allEvents = new HashMap<>();
    private final CallTracker root = new CallTracker();
    private final String name;
    private CallTracker currentNode = root;

    public Profiler(String name) {
        this.name = name;
    }


    public void reset() {
        synchronized (sync) {
            root.reset();
        }
    }


    public P time(String name) {
        synchronized (sync) {
            CallTracker prev = currentNode;
            CallTracker current = currentNode.getChild(name);
            currentNode = current;
            long currentTime = System.nanoTime();
            return () -> {
                synchronized (sync) {
                    long time = System.nanoTime() - currentTime;
                    current.maxTimeTaken = Math.max(current.maxTimeTaken, time);
                    current.totalTimeTaken += time;
                    current.count++;
                    currentNode = prev;
                }
            };
        }
    }

    private StringBuilder report(CallTracker node, String name, double parentsTime, int depth, StringBuilder builder) {
        double time = node.avgTime();
        for (int i = 0; i < depth; i++)
            builder.append('\t');
        builder.append(name);
        if (!Double.isNaN(time)) {
            builder.append(": ").append(String.format("%.4f", time));
            if (parentsTime > 1e-5)
                builder.append(' ').append(String.format("%.2f", 100 * time / parentsTime)).append("%");
            builder.append(" max: ").append(String.format("%.2f", node.maxTimeTaken * 1e-6));
        }
        builder.append('\n');

        if (node.children.isEmpty())
            return builder;

        List<Map.Entry<String, CallTracker>> entries = new ArrayList<>(node.children.entrySet());
        entries.sort(ENTRY_CMP.reversed());

        for (Map.Entry<String, CallTracker> childEntry : entries)
            report(childEntry.getValue(), childEntry.getKey(), time, depth + 1, builder);
        return builder;
    }

    public String report() {
        synchronized (sync) {
            return report(root, name, -1, 0, new StringBuilder()).toString();
        }
    }

    private static class CallTracker {
        private HashMap<String, CallTracker> children = new HashMap<>();

        private double totalTimeTaken;
        private int count;
        private double maxTimeTaken;

        private CallTracker getChild(String name) {
            return children.computeIfAbsent(name, e -> new CallTracker());
        }

        double avgTime() {
            return totalTimeTaken / count * 1e-6;
        }

        void reset() {
            count = 0;
            totalTimeTaken = 0;
            maxTimeTaken = 0 ;
            for (CallTracker child : children.values())
                child.reset();
        }
    }

    private static final P NOTHING = () -> {};

    private static final Comparator<Map.Entry<String, CallTracker>> ENTRY_CMP = Comparator.comparing(e->e.getValue().avgTime());
}
