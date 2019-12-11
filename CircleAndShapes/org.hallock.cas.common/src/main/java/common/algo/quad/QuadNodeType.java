package common.algo.quad;

public enum QuadNodeType {
//    UnitOccupied,  I wish...
    Occupied,

    // cosntruction zones are only visible if they have resources
    // otherwise you can build on them, and walk  on them
    ConstructionOccupied,
    Empty,
}
