package tvrsier.eu.mclaims.manager.roles;

import java.util.UUID;

public class VisitorRole extends AbstractTeamRole {
    private long lifetimeSeconds = 1800;

    public VisitorRole(UUID teamId) {
        super(teamId);
    }

    public VisitorRole withLifetime(long seconds) {
        this.lifetimeSeconds = seconds;
        return this;
    }

    @Override public long getLifetimeSeconds()     { return lifetimeSeconds; }

    public void extendLifetime(long seconds) {
        if(seconds+lifetimeSeconds >= 604800)
            throw new ArithmeticException("Lifetime exceeds 1 week");
        lifetimeSeconds += seconds;
    }
}
