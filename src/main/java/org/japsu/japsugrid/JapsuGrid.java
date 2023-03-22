package org.japsu.japsugrid;

import org.bstats.bukkit.Metrics;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class JapsuGrid extends JavaPlugin {

    private static Logger pluginLogger;
    private static ChunkGenerator chunkGeneratorOverride;

    @Override
    public void onEnable() {

        int DEFAULT_BLOCK_SPACING = 3;

        new Metrics(this, 18036);
        pluginLogger = getLogger();
        FileConfiguration config = this.getConfig();

        // Add config default values.
        config.addDefault("Enabled", true);
        config.addDefault("BlockSpacing", DEFAULT_BLOCK_SPACING);
        config.addDefault("DisableEventsInNewChunks", true);
        config.addDefault("RemoveAllBedrock", true);
        config.addDefault("NonReplaceableBlocks", new ArrayList<String>());

        // Return early if plugin is disabled.
        boolean enabled = config.getBoolean("Enabled");
        if(!enabled) return;

        // Register listener for disabling events on chunk load.
        if (config.getBoolean("DisableEventsInNewChunks")){
            getServer().getPluginManager().registerEvents(new JapsuGridChunkFreezer(this), this);
        }

        // Configure the blocks we don't want to replace.
        List<String> nonReplaceableBlocks = config.getStringList("NonReplaceableBlocks");
        ArrayList<Material> nonReplaceableMaterials = new ArrayList<>();
        // Convert string input to Materials.
        for (String nonReplaceableBlock : nonReplaceableBlocks) {
            Material material = Material.getMaterial(nonReplaceableBlock);
            if (material == null) {
                pluginLogger.warning(String.format("Invalid entry in NonReplaceableBlocks (%s)! Skipping...", nonReplaceableBlock));
                continue;
            }
            nonReplaceableMaterials.add(material);
        }

        // Configure block spacing.
        int blockSpacing = config.getInt("BlockSpacing");
        if(blockSpacing < 1 || blockSpacing > 7){
            pluginLogger.warning(String.format("Invalid BlockSpacing config value (%s)! Valid range is [1,7] Defaulting to %s...", blockSpacing, DEFAULT_BLOCK_SPACING));
            blockSpacing = DEFAULT_BLOCK_SPACING;
        }

        // Increment BlockSpacing by one, because logically it means "amount of space between each block",
        // but internally it means the interval a block is placed.
        blockSpacing++;

        // Write & save the config file.
        config.options().copyDefaults(true);
        saveConfig();


        boolean removeAllBedrock = config.getBoolean("RemoveAllBedrock");

        chunkGeneratorOverride = new JapsuGridChunkGenerator(blockSpacing, removeAllBedrock, nonReplaceableMaterials);

        pluginLogger.info("JapsuGrid enabled!");
    }

    @Override
    public void onDisable() {

        pluginLogger.info("JapsuGrid disabled!");
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id) {

        // Do not intercept generation if the plugin is disabled.
        if(chunkGeneratorOverride != null)
            return chunkGeneratorOverride;

        return super.getDefaultWorldGenerator(worldName, id);
    }
}
