# JapsuGrid

![Overworld image of JapsuGrid world](/Screenshots/overworld_3_nodecor.png)

## Info

**Meet the ["RaysGrid"](https://www.youtube.com/watch?v=fdYABpmVwKg) experience**: A normal minecraft world, but with 99% of its blocks removed.

**A modified version of the oh-so-popular Sky-Grid, but unlike Sky-Grid, the blocks generated are the same as those in a typical Minecraft world.**

*Everything that's possible in a normal Minecraft world, is also possible here!*

## Features

- Structures & decorations can either be generated normally, or "gridified" like all other terrain.
- Bedrock generation can be turned off.
- Ability to skip the removal of specific blocks (end portal frames, chests, what ever you like)!
- No liquid spilling when a chunk is loaded: water/lava stays hovering.
- Very reasonable performance: we either intercept the chunk generation process, or do post-processing with BlockPopulators.

## Setup

1. Download and move the plugin to your "Plugins" -folder.
2. Assign JapsuGrid as your world generator in ```Bukkit.yml```:
```yaml
worlds:
  world:
    generator: JapsuGrid
  world_nether:
    generator: JapsuGrid
  world_the_end:
    generator: JapsuGrid
```

> [!NOTE]  
> If the lines above are missing from your ```Bukkit.yml```, just add them!

## Standard config

```yaml
# When to generate the grid.
# BEFORE_DECORATIONS: Faster, but leaves all decorations (structures, trees, etc) intact.
# AFTER_DECORATIONS: Slower, but decimates everything except blocks defined in "NonReplaceableBlocks" (below).
GenerationMode: AFTER_DECORATIONS

# Amount of empty space between each block.
# Valid range [1, 7].
BlockSpacing: 3

# Whether to skip all bedrock generation.
RemoveAllBedrock: true

# Whether to disable decay for naturally generated leaf blocks.
DisableNaturalLeafDecay: true

# Whether to disable popping off naturally generated item frames.
DisableNaturalItemFramePop: true

# Whether to TEMPORARILY disable water flow and block gravity in newly generated chunks.
# Reduces lag by not allowing liquids to flow when the chunk is loaded.
DisableEventsInNewChunks: true

# Blocks that never get removed by the generator.
NonReplaceableBlocks:
  - END_PORTAL_FRAME
# - SPAWNER
# - CHEST
```

## Gallery

<img src="/Screenshots/overworld_2.png" width="512">

<img src="/Screenshots/nether_0.png" width="512">

<img src="/Screenshots/ancient_city_1.png" width="512">

---

## Stronghold generation

With `BEFORE_DECORATIONS` config value.

<img src="/Screenshots/overworld_stronghold.png" width="512">

With `AFTER_DECORATIONS` config value.

<img src="/Screenshots/stronghold_nodecor.png" width="512">

---

## End island generation

With `BEFORE_DECORATIONS` config value.

<img src="/Screenshots/end_island.png" width="512"></br>

With `AFTER_DECORATIONS` config value.

<img src="/Screenshots/end_1_nodecor.png" width="512">

---

## Ancient city generation

With `BEFORE_DECORATIONS` config value.

<img src="/Screenshots/ancient_city_0.png" width="512">

With `AFTER_DECORATIONS` config value.

<img src="/Screenshots/ancient_city_1.png" width="512">

## Developer info

The main branch is reserved for the latest version of the plugin.
Previous versions are preserved in their own respective branches.

## Contributing

Feel free to submit any PRs :)
