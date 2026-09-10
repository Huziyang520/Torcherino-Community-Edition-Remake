# Torcherino Community Edition Remake

A faithful re-implementation of **Torcherino 7.5** (Minecraft 1.12.2) for modern Minecraft,
built as a MultiLoader project so the same common code drives Forge and Fabric.

- **Minecraft 1.20.1** · Forge 47.x · Fabric 0.16.x · Java 17
- modId `torcherino` · package `com.sci.torcherino` — kept identical to the original so addons,
  data packs and existing world saves keep working.

## What it does

A Torcherino is a torch that accelerates everything around it. Each server tick it walks the
cube of radius `mode` centred on itself and:

- calls `randomTick` on randomly ticking blocks — `speed` times per block, and
- drives the block entity ticker of every block entity in range — `speed` times per entity.

Right-click a Torcherino to advance the area mode (1x1x1 → 15x15x15). Hold the modifier key
(**Left Shift** by default, rebindable as *Torcherino Modifier*) while right-clicking to
advance the speed level instead (up to 800%).

Compression tiers multiply the speed: **Compressed ×9**, **Double Compressed ×81**,
**Triple Compressed ×729**. Every tier also has a Jack o'Lanterino pumpkin variant.

A redstone signal disables a Torcherino completely.

## Blocks

| Block | Registry name | Speed | Notes |
|---|---|---|---|
| Torcherino | `torcherino:blocktorcherino` | ×1 | plus wall variant `wall_blocktorcherino` |
| Compressed Torcherino | `torcherino:blockcompressedtorcherino` | ×9 | |
| Double Compressed Torcherino | `torcherino:blockdoublecompressedtorcherino` | ×81 | |
| Triple Compressed Torcherino | `torcherino:blocktriplecompressedtorcherino` | ×729 | |
| Jack o'Lanterino | `torcherino:blocklanterino` | ×1 | |
| Compressed Jack o'Lanterino | `torcherino:blockcompressedlanterino` | ×9 | |
| Double Compressed Jack o'Lanterino | `torcherino:blockdoublecompressedlanterino` | ×81 | |

## Configuration

`config/torcherino.toml`

| Key | Default | Meaning |
|---|---|---|
| `general.logPlacement` | `false` | Log every Torcherino placement (server owners) |
| `general.overPoweredRecipe` | `true` | Use the cheap "OP" recipe instead of the nether star recipe |
| `general.compressedTorcherino` | `false` | Compressed recipe toggle (see note below) |
| `general.doubleCompressedTorcherino` | `false` | Double compressed recipe toggle |
| `general.tripleCompressedTorcherino` | `false` | Triple compressed recipe toggle |
| `blacklist.blacklistedBlocks` | `[]` | `modid:name` entries the Torcherino must not accelerate |
| `blacklist.blacklistedTiles` | `[]` | Fully qualified block entity class names to skip |

> Note: as in the original 7.5, only `overPoweredRecipe` actually gates a recipe condition.
> The three compression toggles exist but no recipe references them. This is reproduced
> on purpose — see `项目管理与决策记录.md` decision D-012.

## Building

```bash
cd 1.20.1
./gradlew build          # produces common / fabric / forge jars
```

Toolchains: Java 17 is required to build; the Minecraft artifact pipeline additionally needs
Java 21. This machine registers both in `~/.gradle/gradle.properties`
(`org.gradle.java.installations.paths`), because the foojay resolver cannot reach github.com
release assets from here.

## Credits

- **Artik4s** — original Torcherino, and the 7.5 baseline this port is derived from.
- **Huziyang520** — Community Edition Remake (multi-loader port).

Issues: <https://github.com/Huziyang520/Torcherino-Community-Edition-Remake/issues> |
<https://issue.mengcai.online/>
