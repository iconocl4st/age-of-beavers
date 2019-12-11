package app;

public class Goals {
    public boolean wantsMoreTransportWagons;
    public boolean wantsMoreDropoffWagons;

    public boolean wantsMoreWagons() {
        return wantsMoreDropoffWagons || wantsMoreTransportWagons;
    }
}
