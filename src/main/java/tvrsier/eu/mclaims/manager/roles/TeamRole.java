package tvrsier.eu.mclaims.manager.roles;

import java.util.UUID;

public interface TeamRole {
    UUID getTeamId();

    boolean canInvite();
    boolean canRemoveMember();
    boolean canDeleteTeam();
    boolean canBuild();
    boolean canInteract();
    boolean canAcceptVisitors();
    boolean canManageSubclaims();
    boolean canChangeTeamName();
    boolean canAllowBuilding();

    default long getLifetimeSeconds() {
        return -1;
    }
}
