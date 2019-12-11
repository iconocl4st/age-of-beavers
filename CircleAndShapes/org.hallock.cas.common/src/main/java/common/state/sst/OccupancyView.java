package common.state.sst;

public interface OccupancyView {
    boolean isOccupied(int x, int y);

    static OccupancyView combine(OccupancyView v1, OccupancyView v2) {
        return (x, y) -> v1.isOccupied(x, y) || v2.isOccupied(x, y);
    }
}
