package tvrsier.eu.mclaims.manager;

import tvrsier.eu.mclaims.manager.roles.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Team {
    private final UUID id;
    private final UUID ownerId;
    private String name;

    // Team default settings, saved and readed from file
    private boolean isBuildingAllowed;
    private boolean isVisitorAllowed;
    private boolean canManageSubclaims;
    private boolean canAcceptVisitors;
    private boolean canInvite;
    private boolean canChangeTeamName;
    private boolean canChangeTeamRole;
    private boolean canBuild;
    private boolean canAllowBuilding;

    private final Map<UUID, TeamRole> roles = new ConcurrentHashMap<>();

    private static final Map<Permissions, BiConsumer<Team, Boolean>> DEFAULT_PERMISSIONS = Map.of(
            Permissions.BUILDING_ALLOWED, (team, val) -> team.isBuildingAllowed = val,
            Permissions.ACCEPT_VISITORS, (team, val) -> team.canAcceptVisitors = val,
            Permissions.CHANGE_TEAM_NAME, (team, val) -> team.canChangeTeamName = val,
            Permissions.CHANGE_TEAM_ROLE, (team, val) -> team.canChangeTeamRole = val,
            Permissions.INVITE, (team, val) -> team.canInvite = val,
            Permissions.MANAGE_SUBCLAIMS, (team, val) -> team.canManageSubclaims = val,
            Permissions.VISITOR_ALLOWED, (team, val) -> team.isVisitorAllowed = val,
            Permissions.BUILD, (team, val) -> team.canBuild = val,
            Permissions.ALLOW_BUILDING, (team, val) -> team.canAllowBuilding = val
    );

    public Team(UUID id, UUID ownerId, String name) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        roles.put(ownerId, new OwnerRole(id));
    }

    public Team(UUID id, UUID ownerId, String name, boolean isBuildingAllowed, boolean isVisitorAllowed,
                boolean canManageSubclaims, boolean canAcceptVisitors, boolean canInvite, boolean canChangeTeamName,
                boolean canChangeTeamRole, boolean canBuild, boolean canAllowBuilding) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.isBuildingAllowed = isBuildingAllowed;
        this.isVisitorAllowed = isVisitorAllowed;
        this.canManageSubclaims = canManageSubclaims;
        this.canAcceptVisitors = canAcceptVisitors;
        this.canInvite = canInvite;
        this.canChangeTeamName = canChangeTeamName;
        this.canChangeTeamRole = canChangeTeamRole;
        this.canBuild = canBuild;
        this.canAllowBuilding = canAllowBuilding;

        roles.put(ownerId, new OwnerRole(id));
    }

    public UUID getId() { return id; }

    public UUID getOwnerId() { return ownerId; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public TeamRole getRole(UUID playerId) {
        Set<UUID> members = getAllMembers();
        if(!members.contains(playerId)) return null;
        return roles.get(playerId);
    }

    public void addPlayer(UUID playerId, TeamRole role) {
        if (playerId.equals(ownerId)) return;
        Set<UUID> members = getAllMembers();
        if (members.contains(playerId)) return;

        if (role instanceof MemberRole mr) {
            if (!mr.canBuild()) mr.withCanBuild(isBuildingAllowed);
            if (!mr.canAcceptVisitors()) mr.withCanAcceptVisitors(canAcceptVisitors);
            if (!mr.canInvite()) mr.withCanInvite(canInvite);
            roles.put(playerId, mr);
        } else if (role instanceof AdminRole ar) {
            if (!ar.canAllowBuilding()) ar.withAllowBuilding(canAllowBuilding);
            if (!ar.canManageSubclaims()) ar.withAllowSubClaim(canManageSubclaims);
            if (!ar.canChangeTeamName()) ar.withCanChangeTeamName(canChangeTeamName);
            if (!ar.canRemoveMember()) ar.withRemoveMember(canChangeTeamRole);
            if (!ar.canChangeMemberRole()) ar.withChangeMemberRole(canChangeTeamRole);
            roles.put(playerId, ar);
        }
}

    public void removeMember(UUID playerId) {
        if(playerId.equals(ownerId)) return;
        Set<UUID> members = getAllMembers();
        if(!members.contains(playerId)) return;
        roles.remove(playerId);
    }

    public Set<UUID> getMembersByRole(Class<? extends TeamRole> roleClass) {
        return roles.entrySet().stream()
                .filter(e -> roleClass.isInstance(e.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    public Set<UUID> getAllMembers() {
        return Collections.unmodifiableSet(roles.keySet());
    }

    public TeamRole getMember(UUID member) {
        Set<UUID> members = getAllMembers();
        if(!members.contains(member)) return null;
        return roles.get(member);
    }

    public void setDefaultPermission(Permissions pname, boolean b) throws Exception {
        BiConsumer<Team, Boolean> setter = DEFAULT_PERMISSIONS.get(pname);
        if(setter == null) {
            throw new IllegalArgumentException("Permission " + pname + " not found");
        }
        setter.accept(this, b);
        TeamManager.saveTeam(this);
    }

    public boolean isBuildingAllowed() {
        return isBuildingAllowed;
    }

    public boolean isVisitorAllowed() {
        return isVisitorAllowed;
    }

    public boolean isCanManageSubclaims() {
        return canManageSubclaims;
    }

    public boolean isCanAcceptVisitors() {
        return canAcceptVisitors;
    }

    public boolean isCanInvite() {
        return canInvite;
    }

    public boolean isCanChangeTeamName() {
        return canChangeTeamName;
    }

    public boolean isCanChangeTeamRole() {
        return canChangeTeamRole;
    }

    public boolean isCanBuild() {
        return canBuild;
    }

    public boolean canAllowBuilding() { return canAllowBuilding; }
}
