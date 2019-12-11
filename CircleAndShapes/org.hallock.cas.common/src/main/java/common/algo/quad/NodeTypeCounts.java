package common.algo.quad;

public class NodeTypeCounts {
    int numBranches;
    int numDontExist;
    int[] byType = new int[QuadNodeType.values().length];

    NodeTypeCounts() {}

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Total: ").append(total()).append(": ");
        for (QuadNodeType nt : QuadNodeType.values())
            builder.append(nt.name()).append(":").append(byType[nt.ordinal()]).append(", ");
        builder.append("Branches: ").append(numBranches).append(",");
        builder.append("Empty: ").append(numDontExist);
        return builder.toString();
    }

    private int total() {
        int s = numBranches + numDontExist;
        for (int c : byType) s += c;
        return s;
    }
}
