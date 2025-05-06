package tvrsier.eu.mclaims.manager;

import org.junit.jupiter.api.*;
import tvrsier.eu.mclaims.manager.roles.AdminRole;
import tvrsier.eu.mclaims.manager.roles.MemberRole;
import tvrsier.eu.mclaims.manager.roles.Permissions;
import tvrsier.eu.mclaims.manager.roles.VisitorRole;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class TestTeamManager {

    private static File testDir;

    @BeforeAll
    static void setup() throws IOException {
        testDir = new File(System.getProperty("user.dir"), "src/test/resources");

        File teamsDir = new File(testDir, ".mcclaims.teams");
        deleteRecursively(teamsDir);
        TeamManager.initialize(teamsDir);
    }

    @Test
    @Order(1)
    void testCreateTeamAndLookup() {
        UUID ownerId = UUID.randomUUID();
        String name = "Testers";
        Team team = TeamManager.createTeam(ownerId, name);
        assertNotNull(team);

        Team loaded = TeamManager.getTeamFor(ownerId);
        assertNotNull(loaded);
        assertEquals(team.getId(), loaded.getId());
        assertEquals(team.getName(), loaded.getName());
    }

    @Test
    @Order(2)
    void testPersistenceAcrossReload() throws IOException {
        UUID ownerId = UUID.randomUUID();
        Team team = TeamManager.createTeam(ownerId, "ReloadTeam");
        UUID tid = team.getId();

        TeamManager.initialize(new File(testDir, ".mcclaims.teams"));
        TeamManager.saveAll();

        Team reloaded = TeamManager.getTeamFor(ownerId);
        assertNotNull(reloaded);
        assertEquals(tid, reloaded.getId());
        assertEquals("ReloadTeam", reloaded.getName());
    }

    @Test
    @Order(3)
    void testLoadTeamsFromYaml() throws IOException {
        TeamManager.reset();
        TeamManager.initialize(new File(testDir, ".mcclaims.teams"));
        // initialize loads the teams from the YML, so we can check if the teams are loaded directly
        Collection<Team> teams = TeamManager.getAllTeams();
        assertNotNull(teams);
        for(Team team: teams) {
            assertNotNull(team);
            assertNotNull(team.getId());
            assertNotNull(team.getOwnerId());
            assertNotNull(team.getName());
        }
    }

    @Test
    @Order(4)
    void testAddMember() throws IOException {
        UUID ownerId = UUID.randomUUID();
        Team team = TeamManager.createTeam(ownerId, "MemberTesters");
        UUID memberId = UUID.randomUUID();
        TeamManager.addMember(team.getId(), memberId, new MemberRole(team.getId()));

        assertTrue(team.getAllMembers().contains(memberId));
    }

    @Test
    @Order(5)
    void testMemberWithCustomPermissions() throws IOException {
        UUID ownerId = UUID.randomUUID();
        Team team = TeamManager.createTeam(ownerId, "CustomPermTesters");
        UUID memberId = UUID.randomUUID();
        MemberRole member = new MemberRole(team.getId())
                .withCanBuild(true);
        TeamManager.addMember(team.getId(), memberId, member);
        assertTrue(team.getAllMembers().contains(memberId));
        assertTrue(team.getRole(memberId).canBuild());
    }

    @Test
    @Order(6)
    void TestChangeDefaultPerm() throws Exception {
        UUID ownerId = UUID.randomUUID();
        Team team = TeamManager.createTeam(ownerId, "ChangeDefaultPermTesters");
        UUID memberId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        List<Permissions> to_change = new ArrayList<>();
        to_change.add(Permissions.BUILDING_ALLOWED);
        to_change.add(Permissions.ACCEPT_VISITORS);
        to_change.add(Permissions.ALLOW_BUILDING);
        for (Permissions p: to_change) {
            team.setDefaultPermission(p, true);
        }
        TeamManager.addMember(team.getId(), memberId, new MemberRole(team.getId()));
        TeamManager.addMember(team.getId(), adminId, new AdminRole(team.getId()));
        assertTrue(team.getAllMembers().contains(memberId));
        assertTrue(team.getAllMembers().contains(adminId));
        assertTrue(team.getRole(memberId).canBuild());
        assertTrue(team.getRole(memberId).canAcceptVisitors());
        assertTrue(team.getRole(adminId).canAllowBuilding());
        assertTrue(team.isBuildingAllowed());
        assertTrue(team.isCanAcceptVisitors());
        assertTrue(team.canAllowBuilding());
    }

    @AfterAll
    static void cleanup() {
        File teamsDir = new File(testDir, ".mcclaims.teams");
        deleteRecursively(teamsDir);
    }

    private static void deleteRecursively(File f) {
        if(f == null || !f.exists()) return;
        if(f.isDirectory()) for(File c: f.listFiles()) deleteRecursively(c);
        f.delete();
    }
}
