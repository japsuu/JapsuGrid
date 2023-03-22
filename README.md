# JapsuGrid

![Overworld image of JapsuGrid world](/Screenshots/overworld_3.png)

## Info

This spigot plugin implements the ["RaysGrid"](https://www.youtube.com/watch?v=fdYABpmVwKg) world generation:
A normal minecraft world, but with 99% of its blocks removed.

**A re-iteration of the infamous Sky-Grid**, but instead of generating random blocks, these are the actual blocks that would generate if this were a 'normal' world.

*Everything that's possible in a normal world, is also possible here!*

## Features

- Structures are generated normally.
- Bedrock generation can be turned off.
- Ability to skip the removal of specific blocks.
- No liquid spilling when a chunk is loaded: water/lava stays hovering.
- Very good performance: we intercept the chunk generation process.

## Standard config

```yaml
# "Did you try to turn it on and off again?"
Enabled: true

# Amount of empty space between each block.
BlockSpacing: 3

# Whether to skip all bedrock generation.
RemoveAllBedrock: true

# Whether to temporarily disable water flow and block gravity in newly generated chunks.
# Reduces lag by not allowing liquids to start flowing instantly when the chunk is loaded.
DisableEventsInNewChunks: true

# Blocks that never get removed by the generator.
NonReplaceableBlocks:
# - SPAWNER
# - CHEST
```

## Usage statistics

[![Usage statistics](https://bstats.org/signatures/bukkit/JapsuGrid.svg)](https://bstats.org/plugin/bukkit/JapsuGrid/18037)

## Gallery

![Overworld in JapsuGrid world](/Screenshots/overworld_2.png)
![Nether in JapsuGrid world](/Screenshots/nether_0.png)
![Stronghold in JapsuGrid world](/Screenshots/overworld_stronghold.png)
![End island in JapsuGrid world](/Screenshots/end_island.png)

## Contributing

Feel free to submit any PRs :)