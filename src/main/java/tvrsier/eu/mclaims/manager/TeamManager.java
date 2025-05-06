package tvrsier.eu.mclaims.manager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import tvrsier.eu.mclaims.McClaims;
import tvrsier.eu.mclaims.manager.roles.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TeamManager {
    private static final Map<UUID, Team> teamsById = new ConcurrentHashMap<>();
    private static final Map<UUID, Team> teamsByMember = new ConcurrentHashMap<>();

    private static File teamsFile;
    private static YamlConfiguration yaml;

    public static void initialize() throws IOException {
        File dataDir = new File(McClaims.getInstance().getDataFolder(), ".mcclaims.teams");
        if(!dataDir.exists()) {
            if (!dataDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + dataDir.getAbsolutePath());
            }
        }
        teamsFile = new File(dataDir, "teams.yml");
        if(!teamsFile.exists()) {
            if (!teamsFile.createNewFile()) {
                throw new IOException("Failed to create file: " + teamsFile.getAbsolutePath());
            }
        }
        yaml = YamlConfiguration.loadConfiguration(teamsFile);
        loadTeamsFromYaml();
        buildReverseLookup();
    }

    private static void loadTeamsFromYaml() {
        ConfigurationSection root = yaml.getConfigurationSection("teams");
        if(root == null) {
            return;
        }

        for(String key: root.getKeys(false)) {
            UUID teamId = UUID.fromString(key);
            ConfigurationSection sec = root.getConfigurationSection(key);

            assert sec != null;
            UUID ownerId = UUID.fromString(Objects.requireNonNull(sec.getString("owner")));
            String name = sec.getString("name");

            ConfigurationSection defs = sec.getConfigurationSection("defaults");
            assert defs != null;
            boolean isBuildingAllowed = defs.getBoolean("building-allowed");
            boolean isVisitorAllowed = defs.getBoolean("visitor-allowed");
            boolean canManageSubclaims = defs.getBoolean("can-manage-subclaims");
            boolean canAcceptVisitors = defs.getBoolean("can-accept-visitors");
            boolean canInvite = defs.getBoolean("can-invite");
            boolean canChangeTeamName = defs.getBoolean("can-change-team-name");
            boolean canChangeTeamRole = defs.getBoolean("can-change-team-role");
            boolean canBuild = defs.getBoolean("can-build");
            Team team = new Team(teamId, ownerId, name, isBuildingAllowed, isVisitorAllowed,
                    canManageSubclaims, canAcceptVisitors, canInvite, canChangeTeamName,
                    canChangeTeamRole, canBuild);

            ConfigurationSection roleSec = sec.getConfigurationSection("roles");
            if(roleSec != null) {
                for(String memberKey: roleSec.getKeys(false)) {
                    UUID memberId = UUID.fromString(memberKey);
                    ConfigurationSection r = roleSec.getConfigurationSection(memberKey);
                    assert r != null;
                    String roleType = r.getString("type");
                    TeamRole role = null;
                    switch(Objects.requireNonNull(roleType).toUpperCase()) {
                        case "OWNER" -> role = new OwnerRole(teamId);
                        case "ADMIN" -> {
                            boolean allowSubClaim = r.getBoolean("allow-subclaim-management");
                            boolean changeTeamName = r.getBoolean("can-change-team-name");
                            boolean canAllowBuilding = r.getBoolean("can-allow-building");
                            boolean canRemoveMember = r.getBoolean("can-remove-member");
                            boolean changeTeamRole = r.getBoolean("can-change-team-role");

                            role = new AdminRole(teamId)
                                    .withAllowSubClaim(allowSubClaim)
                                    .withCanChangeTeamName(changeTeamName)
                                    .withAllowBuilding(canAllowBuilding)
                                    .withRemoveMember(canRemoveMember)
                                    .withChangeMemberRole(changeTeamRole);
                        }
                        case "MEMBER" -> {
                            boolean pCanBuild = r.getBoolean("can-build");
                            boolean pCanInvite = r.getBoolean("can-invite");
                            boolean pCanAcceptVisitors = r.getBoolean("can-accept-visitors");

                            role = new MemberRole(teamId)
                                    .withCanBuild(pCanBuild)
                                    .withCanInvite(pCanInvite)
                                    .withCanAcceptVisitors(pCanAcceptVisitors);
                        }
                        case "VISITOR" -> {
                            VisitorRole vr = new VisitorRole(teamId);
                            if(r.isSet("expire-at")) {
                                long expireAt = r.getLong("expire-at");
                                long now = System.currentTimeMillis() / 1000;
                                long remaining = expireAt - now;
                                if(remaining > 0) vr.withLifetime(remaining);
                            }
                            role = vr;
                        }
                        default -> {
                            McClaims.getInstance().getLogger().warning("Unknown role type: " + roleType);
                            continue;
                        }
                    }
                    if(role != null) team.addPlayer(memberId, role);
                }
            }
            teamsById.put(teamId, team);
        }
    }

    private static void buildReverseLookup() {
        teamsByMember.clear();
        for(Team team: teamsById.values()) {
            for(UUID member: team.getAllMembers()) teamsByMember.put(member, team);
        }
    }

    public static Team getTeamFor(UUID playerId) {
        return teamsByMember.get(playerId);
    }

    public static Team getTeam(UUID ownerId, String name) throws IOException {
        UUID teamId = UUID.randomUUID();
        Team team = new Team(teamId, ownerId, name);
        teamsByMember.put(ownerId, team);
        saveTeam(team);
        return team;
    }

    public static void saveTeam(Team team) throws IOException {
        String base = "teams." + team.getId() + ".";
        yaml.set(base + "owner", team.getOwnerId().toString());
        yaml.set(base + "name", team.getName());
        yaml.set(base + "defaults.building-allowed", team.isBuildingAllowed());
        yaml.set(base + "defaults.visitor-allowed", team.isVisitorAllowed());
        yaml.set(base + "defaults.can-manage-subclaims", team.isCanManageSubclaims());
        yaml.set(base + "defaults.can-accept-visitors", team.isCanAcceptVisitors());
        yaml.set(base + "defaults.can-invite", team.isCanInvite());
        yaml.set(base + "defaults.can-change-team-name", team.isCanChangeTeamName());
        yaml.set(base + "defaults.can-change-team-role", team.isCanChangeTeamRole());
        yaml.set(base + "defaults.can-build", team.isCanBuild());

        yaml.set(base + "roles", null);
        ConfigurationSection roleSec = yaml.createSection(base + "roles");
        for(UUID memberId: team.getAllMembers()) {
            TeamRole role = team.getRole(memberId);
            String rbase = base + "roles." + memberId + ".";
            yaml.set(rbase + "type", role.getClass().getSimpleName().toUpperCase());
            if(role instanceof AdminRole ar) {
                yaml.set(rbase + "allow-subclaim-management", ar.canManageSubclaims());
                yaml.set(rbase + "can-change-team-name", ar.canChangeTeamName());
                yaml.set(rbase + "can-allow-building", ar.canAllowBuilding());
                yaml.set(rbase + "can-remove-member", ar.canRemoveMember());
                yaml.set(rbase + "can-change-team-role", ar.canChangeMemberRole());
            } else if (role instanceof MemberRole mr) {
                yaml.set(rbase + "can-build", mr.canBuild());
                yaml.set(rbase + "can-invite", mr.canInvite());
                yaml.set(rbase + "can-accept-visitors", mr.canAcceptVisitors());
            } else if (role instanceof VisitorRole vr) {
                long expireAt = System.currentTimeMillis() / 1000 + vr.getLifetimeSeconds();
                yaml.set(rbase + "expire-at", expireAt);
            }
        }
        yaml.save(teamsFile);
    }

    public static void saveAll() throws IOException {
        for(Team team: teamsById.values()) {
            saveTeam(team);
        }
    }

    public static void deleteTeam(UUID teamId) throws IOException {
        Team team = teamsById.remove(teamId);
        if(team != null) {
            for(UUID member: team.getAllMembers()) {
                teamsByMember.remove(member);
            }
            yaml.set("teams." + teamId, null);
            yaml.save(teamsFile);
        }
    }

    public static Collection<Team> getAllTeams() {
        return Collections.unmodifiableCollection(teamsById.values());
    }
}
