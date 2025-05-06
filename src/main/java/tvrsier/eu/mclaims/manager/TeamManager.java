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

    public static void initialize(File dataDir) throws IOException {
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
        if (root == null) {
            return;
        }

        for (String key : root.getKeys(false)) {
            UUID teamId = UUID.fromString(key);
            ConfigurationSection sec = root.getConfigurationSection(key);

            assert sec != null;
            UUID ownerId = UUID.fromString(Objects.requireNonNull(sec.getString("owner")));
            String name = sec.getString("name");

            ConfigurationSection defs = sec.getConfigurationSection("defaults");
            assert defs != null;
            boolean isBuildingAllowed = defs.getBoolean(Permissions.BUILDING_ALLOWED.getValue());
            boolean isVisitorAllowed = defs.getBoolean(Permissions.VISITOR_ALLOWED.getValue());
            boolean canManageSubclaims = defs.getBoolean(Permissions.MANAGE_SUBCLAIMS.getValue());
            boolean canAcceptVisitors = defs.getBoolean(Permissions.ACCEPT_VISITORS.getValue());
            boolean canInvite = defs.getBoolean(Permissions.INVITE.getValue());
            boolean canChangeTeamName = defs.getBoolean(Permissions.CHANGE_TEAM_NAME.getValue());
            boolean canChangeTeamRole = defs.getBoolean(Permissions.CHANGE_TEAM_ROLE.getValue());
            boolean canBuild = defs.getBoolean(Permissions.BUILD.getValue());
            boolean canAllowBuilding = defs.getBoolean(Permissions.ALLOW_BUILDING.getValue());
            Team team = new Team(teamId, ownerId, name, isBuildingAllowed, isVisitorAllowed,
                    canManageSubclaims, canAcceptVisitors, canInvite, canChangeTeamName,
                    canChangeTeamRole, canBuild, canAllowBuilding);

            ConfigurationSection roleSec = sec.getConfigurationSection("roles");
            if (roleSec != null) {
                for (String memberKey : roleSec.getKeys(false)) {
                    UUID memberId = UUID.fromString(memberKey);
                    ConfigurationSection r = roleSec.getConfigurationSection(memberKey);
                    assert r != null;
                    String roleType = r.getString("type");
                    TeamRole role = null;
                    switch (Objects.requireNonNull(roleType).toUpperCase()) {
                        case "OWNER" -> role = new OwnerRole(teamId);
                        case "ADMIN" -> {
                            boolean allowSubClaim = r.getBoolean(Permissions.MANAGE_SUBCLAIMS.getValue());
                            boolean changeTeamName = r.getBoolean(Permissions.CHANGE_TEAM_NAME.getValue());
                            boolean aCanAllowBuilding = r.getBoolean(Permissions.ALLOW_BUILDING.getValue());
                            boolean canRemoveMember = r.getBoolean(Permissions.REMOVE_MEMBER.getValue());
                            boolean changeTeamRole = r.getBoolean(Permissions.CHANGE_TEAM_ROLE.getValue());

                            role = new AdminRole(teamId)
                                    .withAllowSubClaim(allowSubClaim)
                                    .withCanChangeTeamName(changeTeamName)
                                    .withAllowBuilding(aCanAllowBuilding)
                                    .withRemoveMember(canRemoveMember)
                                    .withChangeMemberRole(changeTeamRole);
                        }
                        case "MEMBER" -> {
                            boolean pCanBuild = r.getBoolean(Permissions.BUILD.getValue());
                            boolean pCanInvite = r.getBoolean(Permissions.INVITE.getValue());
                            boolean pCanAcceptVisitors = r.getBoolean(Permissions.ACCEPT_VISITORS.getValue());

                            role = new MemberRole(teamId)
                                    .withCanBuild(pCanBuild)
                                    .withCanInvite(pCanInvite)
                                    .withCanAcceptVisitors(pCanAcceptVisitors);
                        }
                        case "VISITOR" -> {
                            VisitorRole vr = new VisitorRole(teamId);
                            if (r.isSet("expire-at")) {
                                long expireAt = r.getLong("expire-at");
                                long now = System.currentTimeMillis() / 1000;
                                long remaining = expireAt - now;
                                if (remaining > 0) vr.withLifetime(remaining);
                            }
                            role = vr;
                        }
                        default -> {
                            if(McClaims.getInstance() != null) McClaims.getInstance().getLogger().warning("Unknown role type: " + roleType);
                            else System.out.println("Unknown role type: " + roleType);
                            continue;
                        }
                    }
                    if (role != null) team.addPlayer(memberId, role);
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

    public static Team createTeam(UUID ownerId, String name){
        UUID teamId = UUID.randomUUID();
        Team team = new Team(teamId, ownerId, name);
        teamsById.put(teamId, team);
        teamsByMember.put(ownerId, team);
        return team;
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
        yaml.set(base + "defaults." + Permissions.BUILDING_ALLOWED.getValue(), team.isBuildingAllowed());
        yaml.set(base + "defaults." + Permissions.VISITOR_ALLOWED.getValue(), team.isVisitorAllowed());
        yaml.set(base + "defaults." + Permissions.MANAGE_SUBCLAIMS.getValue(), team.isCanManageSubclaims());
        yaml.set(base + "defaults." + Permissions.ACCEPT_VISITORS.getValue(), team.isCanAcceptVisitors());
        yaml.set(base + "defaults." + Permissions.INVITE.getValue(), team.isCanInvite());
        yaml.set(base + "defaults." + Permissions.CHANGE_TEAM_NAME.getValue(), team.isCanChangeTeamName());
        yaml.set(base + "defaults." + Permissions.CHANGE_TEAM_ROLE.getValue(), team.isCanChangeTeamRole());
        yaml.set(base + "defaults." + Permissions.BUILD.getValue(), team.isCanBuild());

        yaml.set(base + "roles", null);
        ConfigurationSection roleSec = yaml.createSection(base + "roles");
        for (UUID memberId : team.getAllMembers()) {
            TeamRole role = team.getRole(memberId);
            String rbase = base + "roles." + memberId + ".";
            yaml.set(rbase + "type", role.getClass().getSimpleName().replace("Role", "").toUpperCase());
            if (role instanceof AdminRole ar) {
                yaml.set(rbase + Permissions.MANAGE_SUBCLAIMS.getValue(), ar.canManageSubclaims());
                yaml.set(rbase + Permissions.CHANGE_TEAM_NAME.getValue(), ar.canChangeTeamName());
                yaml.set(rbase + Permissions.ALLOW_BUILDING.getValue(), ar.canAllowBuilding());
                yaml.set(rbase + Permissions.REMOVE_MEMBER.getValue(), ar.canRemoveMember());
                yaml.set(rbase + Permissions.CHANGE_TEAM_ROLE.getValue(), ar.canChangeMemberRole());
            } else if (role instanceof MemberRole mr) {
                yaml.set(rbase + Permissions.BUILD.getValue(), mr.canBuild());
                yaml.set(rbase + Permissions.INVITE.getValue(), mr.canInvite());
                yaml.set(rbase + Permissions.ACCEPT_VISITORS.getValue(), mr.canAcceptVisitors());
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

    public static void addMember(UUID teamId, UUID playerId, TeamRole role) throws IOException {
        Team team = teamsById.get(teamId);
        if(team != null) {
            team.addPlayer(playerId, role);
            teamsByMember.put(playerId, team);
            saveTeam(team);
        }
    }

    // method that cleans current instance of TeamManager, save the current teams and then reset their in-memory values
    public static void reset() throws IOException {
        saveAll();
        teamsById.clear();
        teamsByMember.clear();
        yaml = null;
        teamsFile = null;
    }
}
