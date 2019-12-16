package common.algo.quad;

public class NodeTypeCounts<T extends Enum> {
    int numBranches;
    int numDontExist;
    final int[] byType;

    NodeTypeCounts(int numValues) {
        byType = new int[numValues];
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Total: ").append(total()).append(": ");
        for (QuadTreeOccupancyState nt : QuadTreeOccupancyState.values())
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
