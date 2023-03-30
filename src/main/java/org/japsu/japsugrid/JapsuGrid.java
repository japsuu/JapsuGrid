package org.japsu.japsugrid;

import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class JapsuGrid extends JavaPlugin {

    public enum GenerationMode {
        BEFORE_DECORATIONS,
        AFTER_DECORATIONS
    }

    public static JapsuGrid Singleton;
    public static Logger PluginLogger;

    private static org.bukkit.generator.ChunkGenerator chunkGeneratorOverride;

    private GenerationMode generationMode = GenerationMode.AFTER_DECORATIONS;
    private int blockSpacingInterval = 3;
    private boolean disableEventsInNewChunks = true;
    private boolean removeAllBedrock = true;
    private List<Material> nonReplaceableMaterials = new ArrayList<>();

    @Override
    public void onEnable() {

        Singleton = this;
        PluginLogger = getLogger();

        // Copy our config file if needed.
        saveDefaultConfig();

        // Start metrics.
        try {
            new Metrics(this, 18036);
        } catch (Exception ignored) { }

        // Init config.
        processConfig(this.getConfig());

        // Register freezer.
        if (disableEventsInNewChunks)
            getServer().getPluginManager().registerEvents(new ChunkFreezer(), this);

        // Override generator.
        chunkGeneratorOverride = new ChunkGenerator(generationMode, blockSpacingInterval, removeAllBedrock, nonReplaceableMaterials);

        PluginLogger.info("JapsuGrid enabled!");
    }

    @Override
    public void onDisable() {

        PluginLogger.info("JapsuGrid disabled!");
    }

    private void processConfig(FileConfiguration config){

        // Add the default non-replaceable materials.
        nonReplaceableMaterials.add(Material.END_PORTAL_FRAME);

        // NonReplaceableMaterials requires special processing, since config cannot save classes:
        List<String> nonReplaceableBlocks = new ArrayList<>();
        for (Material nonReplaceableMaterial : nonReplaceableMaterials) {
            nonReplaceableBlocks.add(nonReplaceableMaterial.toString());
        }

        // Add config default values.
        config.addDefault("GenerationMode", generationMode.toString());
        config.addDefault("BlockSpacing", blockSpacingInterval);
        config.addDefault("DisableEventsInNewChunks", disableEventsInNewChunks);
        config.addDefault("RemoveAllBedrock", removeAllBedrock);
        config.addDefault("NonReplaceableBlocks", nonReplaceableBlocks);

        // Read config values.
        String generationModeInput =    config.getString("GenerationMode");
        blockSpacingInterval =          config.getInt("BlockSpacing");
        disableEventsInNewChunks =      config.getBoolean("DisableEventsInNewChunks");
        removeAllBedrock =              config.getBoolean("RemoveAllBedrock");
        nonReplaceableBlocks =          config.getStringList("NonReplaceableBlocks");

        // Configure gen mode.
        generationMode = GenerationMode.valueOf(generationModeInput);

        // Configure block spacing.
        if (blockSpacingInterval < 1 || blockSpacingInterval > 7) {
            PluginLogger.warning(String.format("Invalid BlockSpacing config value (%s)! Valid range is [1,7] Defaulting to 3...", blockSpacingInterval));
            blockSpacingInterval = 3;
        }
        // Increment BlockSpacing by one, because logically the config value means "amount of space between each block",
        // but internally it means the interval a block is placed.
        blockSpacingInterval++;

        // Convert strings back to Spigot Materials.
        nonReplaceableMaterials = new ArrayList<>();
        for (String nonReplaceableBlock : nonReplaceableBlocks) {
            Material material = Material.getMaterial(nonReplaceableBlock);
            if (material == null) {
                PluginLogger.warning(String.format("Invalid entry in NonReplaceableBlocks (%s)! Skipping...", nonReplaceableBlock));
                continue;
            }
            nonReplaceableMaterials.add(material);
        }

        // Write & save the config file.
        config.options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public org.bukkit.generator.ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {

        return chunkGeneratorOverride;
    }
}
