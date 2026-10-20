# Torcherino Community Edition Remake

> [English](#english) ｜ [中文说明](#中文说明)

---

## English

**Torcherino Community Edition Remake**

Torcherino Community Edition Remake restores the content of the classic version and adds new features. A torch that makes everything around it run faster.

Place a Torcherino and the world inside its area speeds up: crops and saplings grow, furnaces smelt, hoppers move, mob spawners spawn — anything that normally does something once per game tick simply does it several times in the same tick.

Release history and past changes: see [changelogs.md](changelogs.md).

### Features

- **Area acceleration** — accelerates crop growth, random ticks and machine operations throughout a configurable area.
- **Adjustable area** — choose from 16 area modes, from `Stopped` to 15×15×15.
- **Adjustable speed** — hold the modifier key (**sneak** by default) while right-clicking to cycle through 8 speed levels, up to **800%**.
- **Four power tiers** — normal, **Compressed ×9**, **Double Compressed ×81** and **Triple Compressed ×729**.
- **Three shapes** — the Torcherino torch, which can be placed on the ground or against a wall; the pumpkin-shaped **Jack o'Lanterino**; and the hanging **Lanterino** lantern.
- **Redstone control** — redstone behaviour is configurable, with four modes: `Normal`, `Inverted`, `Ignored` and `Always off`.
- **Settings are remembered** — area, speed, redstone mode and ownership information are preserved when the block is broken and placed again.
- **Blacklist** — server owners can specify which blocks or machines must never be accelerated.
- **Owner system (off by default)** — when enabled, the player who places a Torcherino becomes its owner. The owner can control whether others may edit its settings or remove the claim entirely.
- **Editor** — right-click opens an editor with four sliders (speed, X range, Z range and Y range), plus a redstone mode button. The editor can be disabled in favour of the classic quick interaction.
- **Translated** — the editor, on-screen status text and messages follow your game language. Supported languages include English, Chinese, Russian, German, Japanese, French, Spanish, Brazilian Portuguese and Korean.

### How to use

1. Place a Torcherino on the ground. Placing it against a wall automatically selects the wall variant, just like a vanilla torch.
2. **Right-click** the Torcherino to open the **editor**. It contains four sliders — speed, X range, Z range and Y range — plus a redstone mode button. Close the editor to apply your settings; the values are stored in the block.

   **Sneak-right-click never opens the editor**, preserving the vanilla interaction for placing blocks next to a Torcherino.
3. Choose one of the **four redstone modes**:
   - `Normal` — stops when receiving a redstone signal.
   - `Inverted` — runs only while receiving a redstone signal.
   - `Ignored` — always runs, regardless of redstone signals.
   - `Always off` — never runs.
4. Prefer the classic interaction? Set `gui.useGui = false` in `config/torcherino-client.toml`. You can then **right-click to change the area** and **hold sneak + right-click to change the speed**. The action bar displays the current settings, for example, `Area: 3x3x3 | Speed: 200%`. Only one interaction mode is active at a time.

The slider behaviour is controlled by the client setting `gui.freeSpeedMultiplier`:
- `false` (default) snaps each slider handle to the nearest available step.
- `true` allows the handle to be dragged continuously between steps, while the displayed value remains rounded to a valid step.

### Blocks and multipliers

| Block | Multiplier | Notes |
|---|---:|---|
| Torcherino | ×1 | Ground or wall |
| Compressed Torcherino | ×9 | |
| Double Compressed Torcherino | ×81 | |
| Triple Compressed Torcherino | ×729 | |
| Jack o'Lanterino | ×1 | Pumpkin-shaped, emits light |
| Compressed Jack o'Lanterino | ×9 | |
| Double Compressed Jack o'Lanterino | ×81 | |
| Triple Compressed Jack o'Lanterino | ×729 | |
| Lanterino | ×1 | Lantern-shaped, can hang from a ceiling, emits light |
| Compressed Lanterino | ×9 | |
| Double Compressed Lanterino | ×81 | |
| Triple Compressed Lanterino | ×729 | |

Higher tiers perform more acceleration operations at once, making them suitable for large farms and late-game production lines.

### Three shapes: Torcherino, Jack o'Lanterino and Lanterino

Every tier is available in three shapes. They share the same settings (area, speed, redstone mode and owner) and acceleration logic, so choose whichever best fits your build.

- **Torcherino** — the torch itself. Place it on the ground or against a wall; the wall variant is selected automatically, just like a vanilla torch.
- **Jack o'Lanterino** — the pumpkin-shaped variant, crafted from a vanilla carved pumpkin and a Torcherino. It emits light.
- **Lanterino** — the **lantern-shaped variant**. Place it on the ground or hang it from a ceiling, just like a vanilla lantern. It also emits light.

Crafting a **Lanterino** is shapeless: combine a vanilla **lantern** with a Torcherino of the corresponding tier.

```text
Lantern + Torcherino                    -> Lanterino
Lantern + Compressed Torcherino         -> Compressed Lanterino
Lantern + Double Compressed Torcherino  -> Double Compressed Lanterino
Lantern + Triple Compressed Torcherino  -> Triple Compressed Lanterino
```

A Lanterino can also be converted back into its corresponding Torcherino on its own, just like a Jack o'Lanterino. Compressed Lanterinos use the same server-side recipe switches as the compressed torches: `general.compressedTorcherino`, `general.doubleCompressedTorcherino` and `general.tripleCompressedTorcherino`.

### Configuration (two files)

Configuration is split into two files, each with a clearly defined owner. Both can also be edited in game: selecting the mod's configuration screen from the mod list opens two tabs, **Server settings** and **Client settings**. The server tab contains separate `Recipes` and `Performance` groups.

The configuration screen is provided by **Cloth Config**, an optional integration. The mod neither bundles nor requires Cloth Config. When the API is installed, the two tabs are available; without it, edit the two files below directly. Cloth Config is not declared in the mod metadata, so installing or removing it does not change the mod's dependency requirements.

| File | Owner | Contents |
|---|---|---|
| `config/torcherino-client.toml` | **Client** | `gui.useGui` (whether right-click opens the editor), `gui.freeSpeedMultiplier` (whether slider handles can be dragged continuously), `gui.joinNotice` (whether to show a chat notice when joining a world) |
| `config/torcherino-server.toml` | **Server / server owner** | Recipe switches and blacklists |

The configuration screen on Fabric and Forge for Minecraft 1.20–1.20.1 is provided by **Cloth Config**.

Keys in `torcherino-server.toml`:

| Key | Default | Meaning |
|---|---|---|
| `general.overPoweredRecipe` | `true` | Use the cheaper recipe instead of the Nether Star recipe |
| `general.compressedTorcherino` | `true` | Enable recipes for Compressed Torcherinos and compressed Jack o'Lanterinos |
| `general.doubleCompressedTorcherino` | `true` | Enable double-compressed recipes; the compressed tier must also be enabled |
| `general.tripleCompressedTorcherino` | `true` | Enable triple-compressed recipes; both lower compression tiers must also be enabled |
| `general.maxAcceleratedTicksEnabled` | `false` | **Master switch** for the per-Torcherino acceleration cap below. Off = unlimited |
| `general.maxAcceleratedTicksPerTick` | `16384` | Per-Torcherino cap; only used when the switch is enabled. `0` = unlimited |
| `general.maxAcceleratedTicksGlobalEnabled` | `false` | **Master switch** for the global acceleration cap below |
| `general.maxAcceleratedTicksPerTickGlobal` | `65536` | Acceleration cap for the **entire mod** per server tick; only used when the switch is enabled |
| `general.adaptiveThrottle` | `false` | Adaptive brake: automatically reduces acceleration when server ticks become too slow |
| `general.adaptiveThrottleThresholdMs` | `50` | Tick duration above which the brake starts reducing acceleration |
| `general.adaptiveThrottleMaxDivisor` | `256` | Maximum divisor used by the adaptive brake |
| `general.skipSaturatedPositions` | `false` | **Reduces the configured rate.** Stable positions are accelerated only once per tick |
| `general.probabilisticRandomTick` | `true` | **Changes the acceleration model. Enabled by default since 1.4.1.** Uses a single probabilistic random tick instead of a burst of random ticks |
| `general.ownerOnlyWhenOnline` | `false` | Accelerate only while the placing player is online |
| `general.statsLogIntervalSeconds` | `0` | Interval between diagnostic log entries, in seconds. `0` = silent |
| `blacklist.blacklistedBlocks` | `[]` | Blocks that must not be accelerated, specified as `modid:name` |
| `blacklist.blacklistedTiles` | `[]` | Fully qualified class names of block entities that must not be accelerated |

> ⚠️ Recipe switches are read when the **data pack loads**. Restart the game or run `/reload` after changing them. In multiplayer, edit the **server's** configuration file; the client's copy has no effect on the server.
>
> The three compression switches have been enabled by default since **1.4.0**. Disable them in the server configuration if you do not want the compressed tiers.

### Acceleration cap

A Torcherino is a deliberate source of server load: it performs extra growth and machine operations for blocks in its surrounding area, and a larger area multiplies the workload further. On a weak server, or with a Triple Compressed Torcherino running at a high speed, tick duration can easily exceed 50 ms.

`general.maxAcceleratedTicksPerTick` is the safety valve for this situation:

| Value | Meaning |
|---|---|
| `0` | Unlimited — the behaviour of version 1.4.0 and earlier |
| Any positive number | Maximum number of acceleration operations one Torcherino can spend during a single server tick |

Once its budget is exhausted, the Torcherino stops accelerating for the remainder of that tick and resumes from a rotating position on the next tick. This ensures that no part of the area is permanently excluded, although each position may be accelerated less frequently. The budget is distributed across multiple positions rather than being consumed entirely by the first one, so a capped Torcherino can still accelerate an entire farm.

**Changes take effect immediately**. Unlike recipe switches, this setting requires neither a restart nor `/reload`.

Where to change it:

- In game: mod list → Torcherino → configuration → **Server settings** → `Performance` → `Acceleration cap`. Enter a number; `0` means unlimited.
- Or edit `config/torcherino-server.toml` directly.

If a large farm or a Triple Compressed Torcherino causes server lag, try a value between `8192` and `65536` as a starting point.

### Global cap and other performance settings

All the performance options described below are **disabled by default**. With the default configuration, Torcherinos run at the configured speed without these additional limits. These options are intended for servers that cannot handle the full workload. All take effect immediately and are available in the in-game `Performance` group.

**`general.maxAcceleratedTicksPerTickGlobal` (global cap)** — The per-Torcherino cap is not enough when players build many Torcherinos. This setting limits the entire mod. The available budget is distributed among Torcherinos that participated in scanning during the previous tick, preventing one Torcherino from consuming the whole budget and starving the rest of the world.

**`general.adaptiveThrottleThresholdMs` / `general.adaptiveThrottleMaxDivisor` (brake threshold / maximum brake divisor)** — These control the adaptive brake. The threshold determines when a tick is considered too slow; increase it if a heavily modded server normally takes longer than 50 ms per tick. The maximum divisor determines how far acceleration can be reduced, preventing the brake from reducing it all the way to zero.

**`general.skipSaturatedPositions` (skip saturated positions)** — ⚠️ **This option reduces the configured acceleration rate.** Once a farm is fully grown, repeated acceleration calls may be wasted on blocks that can no longer change. When enabled, a position whose block and surrounding 26 blocks remain unchanged for two consecutive ticks receives only one acceleration operation per tick until something nearby changes. Positions at chunk-section boundaries continue running at full speed because their neighbourhood cannot be read cheaply. Enable this option to reduce the tick cost of large, saturated farms, at the expense of the configured multiplier no longer being applied to stable positions.

**`general.probabilisticRandomTick` (probabilistic random ticks)** — ⚠️ **This changes the model, not just the amount of work.** Normally, a position receives `multiplier` random ticks in succession. When this option is enabled, it receives one random tick with probability `multiplier × randomTickSpeed / 4096`, scaling the vanilla random-tick model by the configured multiplier. The expected amount of growth remains equivalent, but the tick cost becomes proportional to the number of positions rather than the number of positions multiplied by the multiplier. This is useful for very high multipliers over very large areas, but growth becomes statistical rather than a direct sequence of extra ticks.

**`general.ownerOnlyWhenOnline` (accelerate only while the owner is online)** — An abandoned Torcherino can continue consuming server resources after its owner leaves. When enabled, it accelerates only while the player who placed it is online. Torcherinos without a recorded owner — including those placed before 1.4.1 or placed by machines — continue running normally.

**`general.statsLogIntervalSeconds` (diagnostic log interval)** — Every N seconds, the server log receives a diagnostic line reporting the work requested by the acceleration loop: scanned positions, extra random ticks, extra block entity ticks, skipped sections, the current brake divisor and the remaining global budget. This helps distinguish problems caused by the mod from those caused by a particular machine. The default value, `0`, disables these log entries.

### Adaptive brake

`general.adaptiveThrottle` (default: `false`) is the automatic counterpart to the fixed acceleration cap. Instead of enforcing a fixed budget, it measures the server's actual tick duration:

- While a tick takes **50 ms or less** (20 TPS), acceleration runs at the configured speed without interference.
- Once a tick exceeds 50 ms, acceleration is reduced in proportion to the tick duration. For example, a 150 ms tick reduces acceleration to one third of its configured rate. Full speed is restored automatically when the server recovers.

This means a healthy server is left alone, while a struggling server receives automatic relief. The trade-off is that effective acceleration depends on the server's performance, which is why this option is disabled by default. When disabled, the configured speed is not automatically reduced by this brake.

This option is recommended for servers with large farms or several players running high-tier Torcherinos simultaneously. **Changes take effect immediately**. It can also be toggled in game: mod list → **Server settings** → `Adaptive brake`.

When joining a world, the mod displays a short chat notice about this setting. Disable the notice with `gui.joinNotice = false` in the client configuration file.

**Upgrading from an older version:** if the original, unsplit file `config/torcherino.toml` still exists, its values are copied into the two new configuration files on the first start.

### Owner system, permissions and Jade

The owner system has been **disabled by default since 1.6.0**. When disabled, placing a Torcherino does not claim it, the editor does not display an owner row, everyone can edit its settings, and Jade does not show an owner line.

To enable the features described below, set `general.ownerSystem` to `true` in the server configuration. Disabling the system again does not delete existing claims: claims stored in the world are retained but ignored, and become active again if the system is re-enabled.

When enabled, a Torcherino records **who placed it**, storing the player's UUID in the block entity. The editor displays the owner information beneath the main panel.

- **Unclaimed** — everyone may edit the Torcherino, as before.
- **Claimed** — the owner can switch between `Others may edit` and `Owner only`, or remove the claim entirely using `Unclaim`. Only the owner can see these buttons; other players can see the current ownership state.
- Ownership does not change automatically. **Breaking a Torcherino preserves its owner and all settings in the dropped item**. Placing the item again restores them, regardless of who places it. Only the original owner can clear the claim using `Unclaim`; after that, the next player to place it can claim it.
- Ownership controls **editing permissions only**. It does not affect acceleration. To stop an abandoned Torcherino from running, use the optional `general.ownerOnlyWhenOnline` setting.

The blacklists can also be edited visually. Open **Server settings** → `Black list` to access two lists with add and remove buttons. Enter `modid:block` for blocks and fully qualified class names for block entities. Tooltips include examples. Changes take effect immediately after saving.

With **Jade** installed, each Torcherino's tooltip can display three additional lines: **acceleration multiplier**, **running state** (including a note when redstone affects it) and **owner**. Each line has its own switch in Jade's plugin configuration, so they can be disabled individually. Jade remains optional.

### Languages

English (US/GB), Simplified Chinese, Traditional Chinese (Taiwan and Hong Kong/Macau), Classical Chinese, Russian, German, Japanese, French, Spanish, Brazilian Portuguese and Korean.

### License and credits

Licensed under the **GNU Affero General Public License v3.0** — see [LICENSE](LICENSE).

- **Scitoshi Nakayobro** — original Torcherino author.
- **LukeGrahamLandry** — maintainer of Torcherino for newer Minecraft versions.
- **artiks4471** — Torcherino Community Edition author.
- **Huziyang520** — Torcherino Community Edition Remake.

Feedback and suggestions: <https://github.com/Huziyang520/Torcherino-Community-Edition-Remake/issues> | <https://issue.mengcai.online/>

---

## 中文说明

**加速火把 社区版重制版**

往期更新的改动见 [changelogs.md](changelogs.md)。

### 功能

- **范围加速** —— 自动加速周围一整块区域里的作物生长、随机刻事件与机器运作。
- **范围可调** —— 右键即可切换 16 档范围，从「已停止」一直到 15x15x15，想只加速一格也行。
- **速度可调** —— 按住改装键（默&#x8BA4;**潜行**）再右键，切换 8 档速度，最高 **800%**。
- **四个等级** —— 普通、**压缩 ×9**、**二重压缩 ×81**、**三重压缩 ×729**，越压缩越猛。
- **三种类型** —— 加速火把（可以插地上，也可以贴墙）、南瓜外形的**加速南瓜灯**，以及可挂在天花板下的**加速灯笼**。
- **红石可控** —— 给加速火把通上红石信号，它就立刻停止工作；断掉信号又恢复。
- **状态保留** —— 范围、速度和红石状态都会保存，拆下来再放回去还是原来的设置，不会重置。
- **黑名单** —— 服主/房主可以指定哪些方块 / 机器不许被加速。
- **所有者系统（默认关闭）** —— 打开后，放置者成为所有者，可切换「他人可改」或取消认领。
- **编辑界面** —— 右键方块打开界面，四条滑条（速度 / X / Z / Y 范围）加一个红石模式按钮；不想要界面也可以在配置里换回经典快捷操作。

### 怎么用

1. 手持加速火把，对着地面右键放下（对着墙放则会变成贴墙形态）。
2. **右键**它：打开**可视化编辑界面**。界面里有四条滑条 —— 速度、X 轴范围、Z 轴范围、Y 轴范围 —— 外加一个红石模式按钮；调完关掉界面立刻生效，数值保存在方块里（拆掉再放回不丢）。

   **潜行时右键不会打开界面**（保持原版行为），方便你在它旁边正常放方块。
3. **红石模式共四档**：`正常`（有红石信号就停止）、`反向`（有红石信号才工作）、`忽略红石`（一直工作）、`始终关闭`。
4. 想换回老式的快捷操作：把 `config/torcherino-client.toml` 里的 `gui.useGui` 改成 `false`，之后**右键切范围**、**按住潜行再右键切速度**，行动栏会显示如 `范围: 3x3x3 | 速度: 200%`。两种方式**只会生效一种**。

> 滑条手感由客户端配置 `gui.freeSpeedMultiplier` 决定：默认 `false` = 手柄被吸附到最近的整档；`true` = 手柄可在档位之间**连续拖动**（数值仍按整档取），两者手感差异明显。

### 方块与倍率

| 方块 | 倍率 | 说明 |
|---|---:|---|
| 加速火把 | ×1 | 可插地上，也可贴墙 |
| 压缩加速火把 | ×9 | |
| 二重压缩加速火把 | ×81 | |
| 三重压缩加速火把 | ×729 | |
| 加速南瓜灯 | ×1 | 南瓜外形，会发光 |
| 压缩加速南瓜灯 | ×9 | |
| 二重压缩加速南瓜灯 | ×81 | |
| 三重压缩加速南瓜灯 | ×729 | |
| 加速灯笼 | ×1 | 灯笼外形，可挂天花板，会发光 |
| 压缩加速灯笼 | ×9 | |
| 二重压缩加速灯笼 | ×81 | |
| 三重压缩加速灯笼 | ×729 | |

倍率越高，同一时间能加速的次数越多，适合后期大规模农场与工业流水线。

### 三种形态：加速火把 / 加速南瓜灯 / 加速灯笼

每个档位都有三种形态，它们的设置（范围、速度、红石模式、所有者）与加速逻辑完全一致，按建筑风格任选其一：

- **加速火把** —— 火把本体。可插地上，也可贴墙（贴墙形态自动选择，与原版火把一致）。
- **加速南瓜灯** —— 南瓜外形的加速火把，由南瓜（雕刻南瓜）+ 加速火把合成，会发光。
- **加速灯笼** —— **灯笼**外形。可放地上，也可挂在天花板下（与原版灯笼一致），同样会发光。

**加速灯笼**的合成是**无序配方**：把原版**灯笼**与对应档位的加速火把放一起即可。

```text
灯笼 + 加速火把            → 加速灯笼
灯笼 + 压缩加速火把        → 压缩加速灯笼
灯笼 + 二重压缩加速火把    → 二重压缩加速灯笼
灯笼 + 三重压缩加速火把    → 三重压缩加速灯笼
```

加速灯笼也能单独变回对应档位的加速火把（与南瓜灯一致）。压缩系灯笼与压缩火把共用同一组服务端配方开关（`general.compressedTorcherino` / `doubleCompressedTorcherino` / `tripleCompressedTorcherino`）。

### 配置（两个文件）

配置拆成两份，各归各的。两份都可以在游戏里改：模组列表里点「配置」会打开一个界面，分成**服务端配置**与**客户端配置**两个标签页，服务端页里再分「配方」与「性能」两个分组。

Fabric端和1.20-1.20.1Forge的配置界面由 **Cloth Config** 提供

| 文件 | 归属 | 内容 |
|---|---|---|
| `config/torcherino-client.toml` | **客户端** | `gui.useGui`（右键是否打开编辑界面）、`gui.freeSpeedMultiplier`（滑条手柄能否连续拖动）、`gui.joinNotice`（进入世界时是否提示） |
| `config/torcherino-server.toml` | **服务端 / 服主** | 配方开关与黑名单 |

`torcherino-server.toml` 的键：

| 配置项 | 默认值 | 含义 |
|---|---|---|
| `general.overPoweredRecipe` | `true` | 使用廉价配方（关闭则改用下界之星配方） |
| `general.compressedTorcherino` | `true` | 压缩火把 / 压缩南瓜灯的配方开关 |
| `general.doubleCompressedTorcherino` | `true` | 二重压缩配方开关（需同时开启压缩） |
| `general.tripleCompressedTorcherino` | `true` | 三重压缩配方开关（需同时开启压缩与二重压缩） |
| `general.maxAcceleratedTicksEnabled` | `false` | 单火把上限的**总开关**；关着就是不限制 |
| `general.maxAcceleratedTicksPerTick` | `16384` | 上限值（推荐默认），仅开关打开时生效；`0` = 仍然不限制 |
| `general.maxAcceleratedTicksGlobalEnabled` | `false` | 全局上限的**总开关** |
| `general.maxAcceleratedTicksPerTickGlobal` | `65536` | **整个模组**每 tick 合计上限值（推荐默认），仅开关打开时生效 |
| `general.adaptiveThrottle` | `false` | 自适应刹车：服务器单 tick 过慢时自动降速 |
| `general.adaptiveThrottleThresholdMs` | `50` | 超过这个单 tick 耗时，刹车才开始降速 |
| `general.adaptiveThrottleMaxDivisor` | `256` | 刹车最多把速度降到几分之一 |
| `general.skipSaturatedPositions` | `false` | **降速项。** 长期无变化的位置改为每 tick 一次 |
| `general.probabilisticRandomTick` | `true` | **不同模型，自 1.4.1 起默认开启。** 用一次原版式随机刻取代整串补刻 |
| `general.ownerOnlyWhenOnline` | `false` | 只有放置者在线时才加速 |
| `general.statsLogIntervalSeconds` | `0` | 诊断日志间隔（秒）；`0` = 不输出 |
| `blacklist.blacklistedBlocks` | `[]` | 禁止被加速的方块，格式 `modid:name` |
| `blacklist.blacklistedTiles` | `[]` | 禁止被加速的机器的类全限定名 |

> ⚠️ 配方开关是在**数据包加载时**读取的：改完要**重启**或执行 `/reload` 才会生效；多人游戏里请改**服务端**那份文件，客户端改的不管用。
>
> 三个压缩开关自 **1.4.0** 起**默认开启**；不想要压缩系就在服务端文件里关掉。

### 加速上限（性能保险阀）

加速火把本身就是一个"主动制造负载"的方块：它会让周围每一格都多跑几千次生长 / 机器逻辑，范围一大还要再乘一次。服务器偏弱、或者用三重压缩火把开到高倍率时，单 tick 耗时很容易冲过 50ms。`general.maxAcceleratedTicksPerTick` 就是为这种情况准备的：

| 取值 | 含义 |
|---|---|
| `0`（默认） | 不限制 —— 与 1.4.0 及更早版本行为完全一致 |
| 任意正数 | 单个加速火把在一个服务端 tick 内最多消耗的加速次数 |

额度用完后，这个火把在本 tick 直接收手，下一 tick 从**轮换过的位置**接着扫 —— 所以区域里没有哪一块会被永久漏掉，只是每格被加速的次数变少。额度还会尽量平摊到多个方块上，而不是被第一格一口吃光，所以开了上限之后它依然在带动整片农场。

**改完立即生效**，不像配方开关那样需要重启或 `/reload`。

在哪里改：

- 游戏内：模组列表 → Torcherino → 配置 → **服务端配置** → `性能` 分组，`加速上限` 这一行是个数字输入框（`0` 表示不限制）；
- 或者直接改 `config/torcherino-server.toml`。

如果大片农场或三重压缩火把已经让服务器卡顿，建议先填 `8192` ~ `65536` 之间的值试试。

### 全局上限，以及其余性能开关都干了什么

下面这些**全部默认关闭**：原样运行时火把就按你设定的速度跑，它们只是服务器实在撑不住时的工具。全部**改完立即生效**，也都能在游戏内的 `性能` 分组里改。

**`general.maxAcceleratedTicksPerTickGlobal`（全局上限）** —— 上面那个"单火把上限"在玩家一口气堆很多把火把时就失效了。这个管的是整个模组：额度会在上一 tick 参与扫描的火把之间分摊，所以没有哪一把能把池子吃光、把世界其它地方饿着。

**`general.adaptiveThrottleThresholdMs` / `general.adaptiveThrottleMaxDivisor`（刹车阈值 / 刹车倍率上限）** —— 自适应刹车的两个旋钮。阈值是"从多慢开始算慢"（重整合包的常态 tick 本来就超过 50ms 时往上调），倍率上限是"最多降速到几分之一"，避免服务器彻底卡死时把加速直接降成零。

**`general.skipSaturatedPositions`（跳过已饱和位置）** —— ⚠️ **这一项会降低你设定的速率。** 已经长满的农场不再变化，之后每一次补刻都是花在"给了也不会有反应"的方块上。开启后，某个位置及其周围 26 格方块连续两 tick 完全没变，就只保留每 tick 一次补刻，直到附近再次变动为止。位于区块段边界上的位置仍按全速运行（它们的邻域无法廉价读取）。想让**大型饱和农场**不再吃满 tick 就打开它，代价是稳定的方块不再按你设定的倍率运行。

**`general.probabilisticRandomTick`（概率式随机刻）** —— ⚠️ **这一项换的是模型，不只是数量。** 平时一个位置会被连续补 `倍率` 次随机刻；开启后改成掷一次骰子：以 `倍率 × 随机刻速率 / 4096` 的概率补一次随机刻 —— 也就是把原版随机刻速率按你的倍率放大。期望增长量一致，但单 tick 成本从"位置数 × 倍率"变成"位置数"。极高倍率 + 极大范围的场景就该用它；代价是生长变成统计意义上的，而不是逐一补刻。

**`general.ownerOnlyWhenOnline`（仅所有者在线时加速）** —— 主人走了、火把还在白烧服务器。开启后只有放置该火把的玩家在线时它才会加速。没有记录所有者的火把（1.4.1 之前放置的、或由机器放置的）照常运行。

**`general.statsLogIntervalSeconds`（统计日志间隔）** —— 诊断用。每 N 秒向服务器日志输出一行：加速循环实际要求了多少工作（扫描位置数、实际补刻次数、方块实体补刻次数、整段跳过的区块段数、当前刹车倍率、剩余全局额度）。用来区分"是这个模组的问题"还是"是这台机器的问题"。`0`（默认）不输出。

### 自适应刹车

`general.adaptiveThrottle`（默认 `false`）是上面那个上限的"自动版"，不给固定数字，而是看服务器实际在干什么：

- 单 tick ≤ **50ms**（TPS 20）时，一切都按你设定的速度跑，不做任何干预；
- 一旦某 tick 超过 50ms，所有加速火把按超出倍数等比降速（150ms 的 tick → 降到三分之一），服务器一恢复就自动回到全速。

也就是说：服务器健康时它完全不插手，服务器撑不住时它不会袖手旁观。代价是"实际速度取决于机器"，这也正是它默认关闭的原因 —— 关掉时，你设多少就是多少。

推荐给开了大农场、或者同时有多人挂高压缩火把的服务器使用。**改完立即生效**，也可以在游戏内切换（模组列表 → **服务端配置** → `自适应刹车`）。

进入世界时聊天栏会有一条关于这一点的提示，可用客户端文件里的 `gui.joinNotice = false` 关掉。

从旧版本升级：若还存在拆分前的单文件 `config/torcherino.toml`，首次启动会把其中的取值搬进上面两个新文件。

### 额外功能

所有者系统**默认关闭**（自 1.6.0 起）：放置加速火把不会认领，界面不显示所有者行，所有人都能改，Jade 也不显示所有者。想要下文描述的功能，请在服务端配置里把 `general.ownerSystem` 打开；再关掉时不会删除任何东西 —— 存档里已记录的认领关系会保留但被忽略，重新打开即恢复。

开启后，加速火把会记住**是谁放的**（UUID 存在方块实体里），编辑界面在面板下方显示这一行：

- **无所有者** —— 和以前一样，谁都能改；
- **已认领** —— 所有者可以一键在 `他人可改配置` / `他人不可改` 之间切换，也可以点 `取消认领` 变回无所有者。这两个按钮**只有所有者看得到**，其他玩家只能看到当前状态。
- 归属不会自己变：**破坏火把时所有者与全部设置会随掉落物保留**，重新放下时（无论谁放）都会还原；只有原所有者点「取消认领」才会清空，之后谁放下来就归谁。
- 归属只限制**能否改配置**，不影响加速效果 —— 想停掉被遗弃的火把，请用可选的 `general.ownerOnlyWhenOnline`。

黑名单也能可视化编辑：**服务端配置** → `黑名单` 分组里是两个带增删按钮的列表（方块填 `modid:方块名`，方块实体填完整类名），tooltip 里有示例；保存后**立即生效**。

装了 **Jade** 时，会显示：**加速倍率**、**启用状态**（受红石影响时会注明）、**所有者**（含「无所有者」）。这三行各自是 Jade 插件配置里的一个开关，可以在 `config/jade/` 里逐项关闭。

### 许可与署名

本项目以 **GNU Affero General Public License v3.0** 授权 —— 见 [LICENSE](LICENSE)。

- **Scitoshi Nakayobro** —— 加速火把原作者。
- **LukeGrahamLandry** —— 高版本加速火把维护者。
- **artiks4471** —— 加速火把社区版作者。
- **Huziyang520** —— 加速火把社区版重制版。

反馈与建议：<https://github.com/Huziyang520/Torcherino-Community-Edition-Remake/issues> | <https://issue.mengcai.online/>