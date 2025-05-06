package tvrsier.eu.mclaims.manager.roles;

import java.util.UUID;

public class OwnerRole extends AbstractTeamRole{
    public OwnerRole(UUID teamId) {
        super(teamId);
    }

    @Override public boolean canInvite()           { return true; }
    @Override public boolean canRemoveMember()     { return true; }
    @Override public boolean canDeleteTeam()       { return true; }
    @Override public boolean canBuild()            { return true; }
    @Override public boolean canAcceptVisitors()   { return true; }
    @Override public boolean canManageSubclaims()  { return true; }
    @Override public boolean canChangeTeamName()   { return true; }
    @Override public boolean canAllowBuilding()    { return true; }
    @Override public boolean canChangeMemberRole()   { return true; }

}
