package org.japsu.japsugrid;

import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.japsu.japsugrid.gridgenerators.AfterStructuresGridGenerator;
import org.japsu.japsugrid.gridgenerators.BeforeStructuresGridGenerator;
import org.japsu.japsugrid.gridgenerators.GridGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public final class JapsuGrid extends JavaPlugin {

    private static final int DEFAULT_BLOCK_SPACING = 3;

    public enum ExecuteMode {
        BEFORE_STRUCTURES,
        AFTER_STRUCTURES
    }

    public static JapsuGrid Singleton;
    public static Logger PluginLogger;

    private PluginManager pluginManager;
    private GridGenerator generator;

    public void ScheduleSyncRepeatingTask(Runnable task, long delay, long period) {
        getServer().getScheduler().scheduleSyncRepeatingTask(this, task, delay, period);
    }

    public void RegisterEventListener(Listener listener){
        pluginManager.registerEvents(listener, this);
    }

    @Override
    public void onEnable() {

        Singleton = this;

        startMetrics();

        PluginLogger = getLogger();
        pluginManager = getServer().getPluginManager();

        FileConfiguration config = this.getConfig();

        addConfigDefaults(config);

        executeVersionChecks(config);

        int blockSpacingInterval = getBlockSpacingInterval(config);
        boolean removeAllBedrock = config.getBoolean("RemoveAllBedrock");
        boolean disableEventsInNewChunks = config.getBoolean("DisableEventsInNewChunks");
        ArrayList<Material> nonReplaceableMaterials = getNonReplaceableMaterials(config);
        ExecuteMode executeMode = getExecuteMode(config);

        // Determine the generator.
        switch (executeMode) {
            case BEFORE_STRUCTURES:
                generator = new BeforeStructuresGridGenerator(blockSpacingInterval, removeAllBedrock, nonReplaceableMaterials, disableEventsInNewChunks);
                break;
            case AFTER_STRUCTURES:
                generator = new AfterStructuresGridGenerator(blockSpacingInterval, removeAllBedrock, nonReplaceableMaterials, disableEventsInNewChunks);
                break;
        }

        config.options().copyDefaults(true);
        saveConfig();

        PluginLogger.info("JapsuGrid enabled!");

        generator.onEnable();
    }

    private ExecuteMode getExecuteMode(FileConfiguration config) {

        try {
            return ExecuteMode.valueOf(config.getString("ExecuteMode"));
        } catch (IllegalArgumentException ex){
            PluginLogger.severe("Could not get ExecutionMode from config! Defaulting to BEFORE_STRUCTURES!");
            return ExecuteMode.BEFORE_STRUCTURES;
        }
    }

    private int getBlockSpacingInterval(FileConfiguration config) {

        int blockSpacingInterval = config.getInt("BlockSpacing");
        if(blockSpacingInterval < 1 || blockSpacingInterval > 7){
            PluginLogger.warning(String.format("Invalid BlockSpacing config value (%s)! Valid range is [1,7] Defaulting to %s...", blockSpacingInterval, DEFAULT_BLOCK_SPACING));
            blockSpacingInterval = DEFAULT_BLOCK_SPACING;
        }

        // Increment BlockSpacing by one, because logically it means "amount of space between each block",
        // but internally it means the interval a block is placed.
        blockSpacingInterval++;
        return blockSpacingInterval;
    }

    private ArrayList<Material> getNonReplaceableMaterials(FileConfiguration config) {

        List<String> nonReplaceableBlocks = config.getStringList("NonReplaceableBlocks");
        ArrayList<Material> nonReplaceableMaterials = new ArrayList<>();

        // Convert string input to Materials.
        for (String nonReplaceableBlock : nonReplaceableBlocks) {
            Material material = Material.getMaterial(nonReplaceableBlock);
            if (material == null) {
                PluginLogger.warning(String.format("Invalid entry in NonReplaceableBlocks (%s)! Skipping...", nonReplaceableBlock));
                continue;
            }
            nonReplaceableMaterials.add(material);
        }

        return nonReplaceableMaterials;
    }

    private void addConfigDefaults(FileConfiguration config) {
        config.addDefault("Version", "1.0.0");
        config.addDefault("ExecuteMode", ExecuteMode.BEFORE_STRUCTURES.toString());
        config.addDefault("BlockSpacing", DEFAULT_BLOCK_SPACING);
        config.addDefault("DisableEventsInNewChunks", true);
        config.addDefault("RemoveAllBedrock", true);
        config.addDefault("NonReplaceableBlocks", new ArrayList<String>());
    }

    private void executeVersionChecks(FileConfiguration config) {

        // Try to get the current project version.
        String currentVersion = config.getString("Version");
        try {
            Properties properties = new Properties();
            properties.load(this.getClassLoader().getResourceAsStream("project.properties"));
            currentVersion = properties.getProperty("version").replaceAll("'", "");
        } catch (IOException e) {
            // Disable backwards compatibility, since we don't want to destroy the user's config.
            PluginLogger.severe("project.properties doesn't exist, cannot get current version number!");
            PluginLogger.severe("Disabled backwards compat!");
        }

        String previousVersion = config.getString("Version");
        if(previousVersion == null){
            previousVersion = "1.0.0";
        }

        boolean hasVersionChanged = !previousVersion.equals(currentVersion);

        if(!hasVersionChanged) return;

        // Version upgrade logic here.

        config.set("Version", currentVersion);
    }

    private void startMetrics(){
        new Metrics(this, 18036);
    }

    @Override
    public void onDisable() {

        generator.onDisable();
        PluginLogger.info("JapsuGrid disabled!");
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {

        return generator.getChunkGeneratorDelegate();
    }
}
