package tvrsier.eu.mclaims.manager.roles;

import java.util.UUID;

public class MemberRole extends AbstractTeamRole {
    private boolean canBuild;
    private boolean canInvite = false;
    private boolean canAcceptVisitors = false;

    public MemberRole(UUID teamId) {
        super(teamId);
    }

    public MemberRole withCanBuild(boolean b) {
        canBuild = b;
        return this;
    }

    public MemberRole withCanInvite(boolean b) {
        this.canInvite = b;
        return this;
    }

    public MemberRole withCanAcceptVisitors(boolean b) {
        this.canAcceptVisitors = b;
        return this;
    }

    @Override public boolean canInvite()            { return canInvite; }
    @Override public boolean canBuild()             { return canBuild; }
    @Override public boolean canAcceptVisitors()    { return canAcceptVisitors; }

    public void setCanBuild(boolean b) { canBuild = b; }
    public void setCanInvite(boolean b) { canInvite = b; }
    public void setCanAcceptVisitors(boolean b) { canAcceptVisitors = b; }
}
