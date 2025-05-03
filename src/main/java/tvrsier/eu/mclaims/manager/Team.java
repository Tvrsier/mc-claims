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

    private final Map<UUID, TeamRole> roles = new ConcurrentHashMap<>();

    public Team(UUID id, UUID ownerId, String name) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
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
}
