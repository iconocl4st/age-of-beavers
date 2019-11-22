package driver;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrintThreads {

    private static final Pattern name = Pattern.compile("^\"([^\"]*)\".*prio=.*");
    private static final Pattern state = Pattern.compile("  java.lang.Thread.State: ([A-Z]*)");
    private static final Pattern waitingFor = Pattern.compile(".*waiting for (.*) to release lock on <(0x[^>]*)> \\(a ([^)]*)\\).*");
    private static final Pattern blocking = Pattern.compile("\t blocks (.*)");
    private static final Pattern owns = Pattern.compile(".*- locked <(0x[^>]*)> \\(a ([^)]*)\\).*");

    enum ThreadState {
        BLOCKED,
        RUNNABLE,
        WAITING,
    }
    private static final class WaitingFor {
        String threadName;
        String lockName;
        String whatItIs;

        public String toString() {
            return "[" + threadName + "][" + lockName + "][" + whatItIs + "]";
        }
    }
    private static final class Owns {
        String lockName;
        String whatItIs;

        public String toString() {
            return "[" + lockName + "][" + whatItIs + "]";
        }
    }
    private static final class ThreadInfo {
        String name;
        ThreadState state;
        WaitingFor waitingFor;
        LinkedList<Owns> owns = new LinkedList<>();
        HashSet<String> blocks = new HashSet<>();
        LinkedList<String> stackTrace = new LinkedList<>();

        public String toString() {
            return name + " (" + state + ") (" + waitingFor + ") (" + owns + ")";
        }

        public Owns getOwns(String lockName) {
            for (Owns owned : owns)
                if (owned.lockName.equals(lockName))
                    return owned;
            return null;
        }
    }

    private static final class ThreadNode {
        ThreadInfo info;
        ThreadNode parent;
        LinkedList<ThreadNode> children = new LinkedList<>();

        public ThreadNode(ThreadInfo info) {
            this.info = info;
        }
    }

    private static HashMap<String, ThreadInfo> parseThreads(String filePath) throws IOException {
        HashMap<String, ThreadInfo> threads = new HashMap<>();
        ThreadInfo current = new ThreadInfo();
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(filePath));) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {

                Matcher matcher;
                if ((matcher = name.matcher(line)).matches()) {
                    current.name = matcher.group(1);
                } else if ((matcher = state.matcher(line)).matches()) {
                    current.state = ThreadState.valueOf(matcher.group(1));
                } else if ((matcher = waitingFor.matcher(line)).matches()) {
                    WaitingFor waitingFor = new WaitingFor();
                    waitingFor.threadName = matcher.group(1);
                    waitingFor.lockName = matcher.group(2);
                    waitingFor.whatItIs = matcher.group(3);
                    current.waitingFor = waitingFor;
                } else if ((matcher = blocking.matcher(line)).matches()) {
                    current.blocks.add(matcher.group(1));
                } else if (line.length() == 0) {
                    threads.put(current.name, current);
                    current = new ThreadInfo();
                } else {
                    if ((matcher = owns.matcher(line)).matches()) {
                        Owns owns = new Owns();
                        owns.lockName = matcher.group(1);
                        owns.whatItIs = matcher.group(2);
                        current.owns.add(owns);
                    }
                    current.stackTrace.add(line);
                }
            }
        }
        return threads;
    }

    private static void validate(HashMap<String, ThreadInfo> threads) {
        for (ThreadInfo node : threads.values()) {
            if (node.state.equals(ThreadState.WAITING) || node.state.equals(node.state.RUNNABLE)) {
                if (node.waitingFor != null) throw new IllegalStateException();
            }
            if (node.state.equals(ThreadState.BLOCKED)) {
                if (node.waitingFor == null) throw new IllegalStateException();

                ThreadInfo waitingOn = threads.get(node.waitingFor.threadName);
                if (waitingOn == null) throw new IllegalStateException(node.waitingFor.threadName);

                if (!waitingOn.blocks.contains(node.name)) {
                    throw new IllegalStateException();
                }

                Owns owns = waitingOn.getOwns(node.waitingFor.lockName);
                if (owns == null)
                    throw new IllegalStateException();

                if (!owns.whatItIs.equals(node.waitingFor.whatItIs))
                    throw new IllegalStateException();
            }
        }
    }

    private static boolean printsWaits(
            HashMap<String, ThreadInfo> threads,
            ThreadInfo startingThread,
            ThreadInfo currentThread,
            boolean start
    ) {
        if (!start && startingThread.name.equals(currentThread.name)) {
            return true;
        }
        if (currentThread.waitingFor == null) {
            return false;
        }
        if (printsWaits(threads, startingThread, threads.get(currentThread.waitingFor.threadName), false)) {
            System.out.print(" is blocked by " + currentThread.name);
            return true;
        }
        return false;
    }

    private static String indent(int depth) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            stringBuilder.append('\t');
        }
        return stringBuilder.toString();
    }

    private static void printNodeTree(HashSet<String> printedNames, ThreadNode current, int depth) {
        System.out.println(indent(depth) + current.info.name + " " + current.info.state);
        if (printedNames.contains(current.info.name)) {
            System.out.println(indent(depth + 1) + "XXXXXXXXXXXXXXXX");
            return;
        }
        printedNames.add(current.info.name);
        for (ThreadNode child : current.children) {
            printNodeTree(printedNames, child, depth + 1);
        }
    }

    public static void main(String[] args) throws IOException {
        HashMap<String, ThreadInfo> threads = parseThreads("/home/thallock/Documents/Idea/age-of-beavers/CircleAndShapes/threads_report.txt");
        validate(threads);
        for (ThreadInfo node : threads.values()) {
            System.out.println(node);
        }
        System.out.println("===========================");
        printCycles(threads);
        printsWaits(threads);
        printTree(threads);
    }

    private static void printCycles(HashMap<String, ThreadInfo> threads) {
        for (ThreadInfo thread : threads.values()) {
            printsWaits(threads, thread, thread, true);
        }
        System.out.println("===========================");
    }

    private static void printsWaits(HashMap<String, ThreadInfo> threads) {
        for (ThreadInfo thread : threads.values()) {
            if (!thread.state.equals(ThreadState.BLOCKED))
                continue;
            System.out.println(thread.name + " waits on " + threads.get(thread.waitingFor.threadName).name);
        }
        System.out.println("===========================");
    }

    private static void printTree(HashMap<String, ThreadInfo> threads) {
        HashMap<String, ThreadNode> currentRoots = new HashMap<>();
        HashMap<String, ThreadNode> allNodes = new HashMap<>();
        for (ThreadInfo thread : threads.values()) {
            ThreadNode node = new ThreadNode(thread);
            currentRoots.put(thread.name, node);
            allNodes.put(thread.name, node);
        }
        for (ThreadInfo thread : threads.values()) {
            if (thread.waitingFor == null)
               continue;
            ThreadNode parent = allNodes.get(thread.waitingFor.threadName);
            ThreadNode remove = currentRoots.remove(thread.name);
            if (remove == null)
                throw new IllegalStateException("cycle detected");
            remove.parent = parent;
            parent.children.add(remove);
        }
        for (ThreadNode node : currentRoots.values()) {
            if (node.children.isEmpty())
                continue;
            printNodeTree(new HashSet<>(), node, 0);
        }
        System.out.println("===========================");
    }
}

