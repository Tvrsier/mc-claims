package tvrsier.eu.mclaims.manager.roles;

import java.util.UUID;

public class AdminRole extends AbstractTeamRole{
    private boolean allowSubclaimManagement;
    private boolean allowVisitorAcceptance = true;
    private boolean canChangeTeamName;
    private boolean canAllowBuilding;
    private boolean canRemoveMember;

    public AdminRole(UUID teamId) {
        super(teamId);
    }

    public AdminRole withAllowSubClaim(boolean b) {
        allowSubclaimManagement = b;
        return this;
    }

    public AdminRole withCanChangeTeamName(boolean b) {
        canChangeTeamName = b;
        return this;
    }

    public AdminRole withAllowBuilding(boolean b) {
        canAllowBuilding = b;
        return this;
    }

    public AdminRole withRemoveMember(boolean b) {
        canRemoveMember = b;
        return this;
    }

    @Override public boolean canInvite()           { return true; }
    @Override public boolean canRemoveMember()     { return canRemoveMember; }
    @Override public boolean canDeleteTeam()       { return false; }
    @Override public boolean canBuild()            { return true; }
    @Override public boolean canAcceptVisitors()   { return allowVisitorAcceptance; }
    @Override public boolean canManageSubclaims()  { return allowSubclaimManagement; }
    @Override public boolean canChangeTeamName()   { return canChangeTeamName; }
    @Override public boolean canAllowBuilding()    { return canAllowBuilding ;}

    public void setClaimManagement(boolean b) { this.allowSubclaimManagement = b; }

    public void setVisitorAcceptance(boolean b) { this.allowVisitorAcceptance = b; }

    public void setChangeTeamName(boolean b) { this.canChangeTeamName = b; }

    public void setAllowBuilding(boolean b) { this.canAllowBuilding = b; }
}
