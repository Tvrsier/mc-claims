package tvrsier.eu.mclaims.manager.roles;

import java.util.UUID;

public abstract class AbstractTeamRole implements TeamRole{
    protected final UUID teamId;

    protected AbstractTeamRole(UUID teamId) { this.teamId = teamId; }

    @Override public UUID getTeamId() { return teamId; }

    @Override public boolean canInteract() { return true; }

    @Override public boolean canAcceptVisitors() { return false; }

    @Override public boolean canManageSubclaims() { return false; }

    @Override public boolean canChangeTeamName() { return false; }

    @Override public boolean canAllowBuilding() { return false; }

    @Override public boolean canChangeMemberRole() { return false; }

    @Override public boolean canRemoveMember() { return false; }

    @Override public boolean canInvite() { return false; }

    @Override public boolean canDeleteTeam() { return false; }

    @Override public boolean canBuild() { return false; }

    @Override public long getLifetimeSeconds() { return -1; } // Default value, should be overridden in subclasses
}
