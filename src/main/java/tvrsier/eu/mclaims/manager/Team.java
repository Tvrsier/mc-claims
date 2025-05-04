package tvrsier.eu.mclaims.manager;

import tvrsier.eu.mclaims.manager.roles.OwnerRole;
import tvrsier.eu.mclaims.manager.roles.TeamRole;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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

    private final Map<UUID, TeamRole> roles = new ConcurrentHashMap<>();

    public Team(UUID id, UUID ownerId, String name) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        roles.put(ownerId, new OwnerRole(id));
    }

    public Team(UUID id, UUID ownerId, String name, boolean isBuildingAllowed, boolean isVisitorAllowed,
                boolean canManageSubclaims, boolean canAcceptVisitors, boolean canInvite, boolean canChangeTeamName,
                boolean canChangeTeamRole, boolean canBuild) {
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
        if(playerId.equals(ownerId)) return;
        Set<UUID> members = getAllMembers();
        if(members.contains(playerId)) return;
        roles.put(playerId, role);
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

    public void setDefaultPermission(String pname, boolean b) {
        switch (pname.toLowerCase()) {
            case "isbuildingallowed" -> isBuildingAllowed = b;
            case "isvisitorallowed" -> isVisitorAllowed = b;
            case "canmanagesubclaims" -> canManageSubclaims = b;
            case "canacceptvisitors" -> canAcceptVisitors = b;
            case "caninvite" -> canInvite = b;
            case "canchangeteamname" -> canChangeTeamName = b;
            case "canchangeteamrole" -> canChangeTeamRole = b;
            case "canbuild" -> canBuild = b;
        }
        // TeamManager.saveTeam(this);
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
}
