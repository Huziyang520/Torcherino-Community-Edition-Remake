# Torcherino Community Edition Remake

**Minecraft 1.20.1 · Forge / Fabric**

> [中文说明](#中文说明) ｜ [English](#english)

---

## 中文说明

**加速火把 社区版重制版** —— 一根能让周围一切都变快的火把。

插上一根加速火把，它覆盖范围内的世界就会加速运转：小麦和树苗疯长、熔炉飞快熔炼、漏斗飞速搬运、
刷怪笼加速刷怪……凡是「每游戏刻都会动一下」的方块，都会在同样的时间里多跑好几次。

### 功能

- **范围加速** —— 自动加速周围一整块区域里的作物生长、随机刻事件与机器运作。
- **范围可调** —— 右键即可切换 16 档范围，从「已停止」一直到 15x15x15，想只加速一格也行。
- **速度可调** —— 按住改装键（默认**左 Shift**）再右键，切换 8 档速度，最高 **800%**。
- **四个力量等级** —— 普通、**压缩 ×9**、**二重压缩 ×81**、**三重压缩 ×729**，越压缩越猛。
- **两种外形** —— 加速火把（可以插地上，也可以贴墙）和南瓜外形的**加速南瓜灯**。
- **红石可控** —— 给加速火把通上红石信号，它就立刻停止工作；断掉信号又恢复。
- **状态会记住** —— 范围、速度和红石状态都会保存，拆下来再放回去还是原来的设置，不会重置。
- **黑名单** —— 服主可以指定哪些方块 / 机器不许被加速。
- **多语言** —— 屏幕上的状态提示会跟随你的游戏语言显示。

### 怎么用

1. 手持加速火把，对着地面右键放下（对着墙放则会变成贴墙形态）。
2. **右键**它：范围变大，行动栏会显示当前范围与速度，例如 `范围: 3x3x3 | 速度: 200%`。
3. **按住左 Shift 再右键**：切换速度档位（0% → 800% → 0% 循环）。
4. 想关掉它：给方块通上红石信号即可；也可以在范围切到「已停止」时停用。

> 左 Shift 是默认键，可以在「选项 → 控制」里搜索「切换加速模式」改成任何你习惯的按键。

### 方块与倍率

| 方块 | 倍率 | 说明 |
|---|---|---|
| 加速火把 | ×1 | 可插地上，也可贴墙 |
| 压缩加速火把 | ×9 | |
| 二重压缩加速火把 | ×81 | |
| 三重压缩加速火把 | ×729 | |
| 加速南瓜灯 | ×1 | 南瓜外形，会发光 |
| 压缩加速南瓜灯 | ×9 | |
| 二重压缩加速南瓜灯 | ×81 | |

倍率越高，同一时间能加速的次数越多，适合后期大规模农场与工业流水线。

### 配置（服主）

配置文件位于 `config/torcherino.toml`：

| 配置项 | 默认值 | 含义 |
|---|---|---|
| `general.logPlacement` | `false` | 是否记录每一次放置加速火把 |
| `general.overPoweredRecipe` | `true` | 是否使用廉价的「OP」配方（否则改用下界之星配方） |
| `general.compressedTorcherino` | `false` | 压缩配方开关 |
| `general.doubleCompressedTorcherino` | `false` | 二重压缩配方开关 |
| `general.tripleCompressedTorcherino` | `false` | 三重压缩配方开关 |
| `blacklist.blacklistedBlocks` | `[]` | 禁止被加速的方块，格式 `modid:name` |
| `blacklist.blacklistedTiles` | `[]` | 禁止被加速的机器的类全限定名 |

> 注：目前只有 `overPoweredRecipe` 会实际影响配方；三个压缩开关虽然存在于配置里，但还没有配方引用它们，
> 属于已知的保留行为，不必当作故障反馈。

### 支持的语言

简体中文、繁體中文（台灣）、繁體中文（香港/澳門）、文言（華夏）、英语（美式/英式）、俄语、德语、
日语、法语、西班牙语、巴西葡萄牙语、韩语。

### 许可与署名

本项目以 **GNU Affero General Public License v3.0** 授权 —— 见 [LICENSE](LICENSE)。

- **Scitoshi Nakayobro** —— 加速火把原作者。
- **LukeGrahamLandry** —— 高版本加速火把维护者。
- **artiks4471** —— 加速火把社区版作者。
- **Huziyang520** —— 加速火把社区版重制版。

反馈与建议：<https://github.com/Huziyang520/Torcherino-Community-Edition-Remake/issues> |
<https://issue.mengcai.online/>

---

## English

**Torcherino Community Edition Remake** — a torch that makes everything around it run faster.

Place a Torcherino and the world inside its area speeds up: crops and saplings grow, furnaces smelt,
hoppers move, mob spawners spawn — anything that normally does something once per game tick simply
does it several times in the same tick.

### Features

- **Area acceleration** — accelerates crop growth, random ticks and machines in a whole area around it.
- **Adjustable area** — right-click to step through 16 area modes, from `Stopped` up to 15x15x15.
- **Adjustable speed** — hold the modifier key (**Left Shift** by default) while right-clicking to
  step through 8 speed levels, up to **800%**.
- **Four power tiers** — normal, **Compressed ×9**, **Double Compressed ×81**,
  **Triple Compressed ×729**.
- **Two shapes** — the torch itself (place it on the floor or against a wall) and the pumpkin shaped
  **Jack o'Lanterino**.
- **Redstone control** — a redstone signal stops a Torcherino completely, and removing the signal
  starts it again.
- **Settings are remembered** — area, speed and redstone state are saved, so breaking and replacing
  the block keeps your settings.
- **Blacklist** — server owners can decide which blocks or machines must never be accelerated.
- **Translated** — the on-screen status text follows your game language.

### How to use

1. Place a Torcherino on the ground (placing it against a wall gives the wall variant).
2. **Right-click** it to enlarge the area. The action bar shows the current area and speed, for
   example `Area: 3x3x3 | Speed: 200%`.
3. **Hold Left Shift and right-click** to change the speed level (0% → 800% → wraps around).
4. To switch it off, power the block with a redstone signal, or set the area to `Stopped`.

> Left Shift is only the default. You can rebind it in *Options → Controls* by searching for
> *Torcherino Modifier*.

### Blocks

| Block | Speed | Notes |
|---|---|---|
| Torcherino | ×1 | floor or wall |
| Compressed Torcherino | ×9 | |
| Double Compressed Torcherino | ×81 | |
| Triple Compressed Torcherino | ×729 | |
| Jack o'Lanterino | ×1 | pumpkin shaped, emits light |
| Compressed Jack o'Lanterino | ×9 | |
| Double Compressed Jack o'Lanterino | ×81 | |

Higher tiers perform more acceleration ticks at once, which is what you want for large farms and
late-game factories.

### Configuration (server owners)

The configuration file lives at `config/torcherino.toml`:

| Key | Default | Meaning |
|---|---|---|
| `general.logPlacement` | `false` | Log every Torcherino placement |
| `general.overPoweredRecipe` | `true` | Use the cheap "OP" recipe instead of the nether star recipe |
| `general.compressedTorcherino` | `false` | Compressed recipe toggle |
| `general.doubleCompressedTorcherino` | `false` | Double compressed recipe toggle |
| `general.tripleCompressedTorcherino` | `false` | Triple compressed recipe toggle |
| `blacklist.blacklistedBlocks` | `[]` | `modid:name` entries that must not be accelerated |
| `blacklist.blacklistedTiles` | `[]` | Fully qualified machine class names that must not be accelerated |

> Note: only `overPoweredRecipe` currently affects a recipe. The three compression toggles exist in
> the config file but no recipe reads them yet — that is intended behaviour, not a bug.

### Languages

English (US/GB), Simplified Chinese, Traditional Chinese (Taiwan and Hong Kong/Macau), Classical
Chinese, Russian, German, Japanese, French, Spanish, Brazilian Portuguese and Korean.

### License and credits

Licensed under the **GNU Affero General Public License v3.0** — see [LICENSE](LICENSE).

- **Scitoshi Nakayobro** — Torcherino author.
- **LukeGrahamLandry** — maintainer of Torcherino for newer Minecraft versions.
- **artiks4471** — Torcherino Community Edition author.
- **Huziyang520** — Torcherino Community Edition Remake.

Feedback: <https://github.com/Huziyang520/Torcherino-Community-Edition-Remake/issues> |
<https://issue.mengcai.online/>
