package tvrsier.eu.mclaims;

import org.bukkit.plugin.java.JavaPlugin;
import tvrsier.eu.mclaims.manager.TeamManager;

public final class McClaims extends JavaPlugin {
    private static McClaims instance;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        try {
            TeamManager.initialize();
            getLogger().info("TeamManager inizialized successfully.");
        } catch (Exception e) {
            getLogger().severe("Failed to initialize TeamManager: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try {
            //TeamManager.saveAll();
        } catch(Exception e) {
            getLogger().severe("Failed to save teams: " + e.getMessage());
        }
    }

    public static McClaims getInstance() {
        return instance;
    }
}
