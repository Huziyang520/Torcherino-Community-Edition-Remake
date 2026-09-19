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
2. **右键**它：打开**可视化编辑界面**。界面里有四条滑条 —— 速度、X 轴范围、Z 轴范围、Y 轴范围 ——
   外加一个红石模式按钮；调完关掉界面立刻生效，数值保存在方块里（拆掉再放回不丢）。
   **潜行时右键不会打开界面**（保持原版行为），方便你在它旁边正常放方块。
3. **红石模式共四档**：`正常`（有红石信号就停止）、`反向`（有红石信号才工作）、
   `忽略红石`（一直工作）、`始终关闭`。
4. 想换回老式的快捷操作：把 `config/torcherino-client.toml` 里的 `gui.useGui` 改成 `false`，
   之后**右键切范围**、**按住左 Shift 再右键切速度**，行动栏会显示如 `范围: 3x3x3 | 速度: 200%`。
   两种方式**只会生效一种**。

> 滑条手感由客户端配置 `gui.smoothSlider` 决定：默认 `false` = 手柄被吸附到最近的整档；
> `true` = 手柄可在档位之间**连续拖动**（数值仍按整档取），两者手感差异明显。
> 快捷操作模式下，左 Shift 是默认键，可在「选项 → 控制」里搜索「切换加速模式」改键。

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
| 三重压缩加速南瓜灯 | ×729 | |

倍率越高，同一时间能加速的次数越多，适合后期大规模农场与工业流水线。

### 配置（两个文件）

配置拆成两份，各归各的：

| 文件 | 归属 | 内容 |
|---|---|---|
| `config/torcherino-client.toml` | **客户端** | `gui.useGui`（右键是否打开编辑界面）、`gui.smoothSlider`（滑条手柄能否连续拖动） |
| `config/torcherino-server.toml` | **服务端 / 服主** | 配方开关与黑名单 |

`torcherino-server.toml` 的键：

| 配置项 | 默认值 | 含义 |
|---|---|---|
| `general.overPoweredRecipe` | `true` | 使用廉价配方（关闭则改用下界之星配方） |
| `general.compressedTorcherino` | `false` | 压缩火把 / 压缩南瓜灯的配方开关 |
| `general.doubleCompressedTorcherino` | `false` | 二重压缩配方开关（需同时开启压缩） |
| `general.tripleCompressedTorcherino` | `false` | 三重压缩配方开关（需同时开启压缩与二重压缩） |
| `blacklist.blacklistedBlocks` | `[]` | 禁止被加速的方块，格式 `modid:name` |
| `blacklist.blacklistedTiles` | `[]` | 禁止被加速的机器的类全限定名 |

> ⚠️ 配方开关是在**数据包加载时**读取的：改完要**重启**或执行 `/reload` 才会生效；
> 多人游戏里请改**服务端**那份文件，客户端改的不管用。
> 三个压缩开关默认关闭（原版行为），想要压缩系请在服务端文件里打开。
>
> 从旧版本升级：若还存在拆分前的单文件 `config/torcherino.toml`，首次启动会把其中的取值搬进上面两个新文件。

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
2. **Right-click** it to open the **editor**: four sliders (speed, X range, Z range, Y range) plus a
   redstone mode button. Close it to apply; the values are stored in the block.
   **Sneak-right-click never opens the editor**, which keeps the vanilla interaction for placing
   blocks next to a Torcherino.
3. **Four redstone modes**: `Normal` (a signal stops it), `Inverted` (only runs with a signal),
   `Ignored` (always runs) and `Always off`.
4. Prefer the classic interaction? Set `gui.useGui = false` in `config/torcherino-client.toml` and you get
   **right-click to change the area** and **hold Left Shift + right-click to change the speed** again,
   with the action bar showing e.g. `Area: 3x3x3 | Speed: 200%`. Only one of the two modes is active.

> Slider feel is controlled by the client setting `gui.smoothSlider`: `false` (default) pulls every
> handle onto the nearest step, `true` lets the handle be dragged continuously between steps while
> the reported value stays rounded.
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
| Triple Compressed Jack o'Lanterino | ×729 | |

Higher tiers perform more acceleration ticks at once, which is what you want for large farms and
late-game factories.

### Configuration (two files)

The configuration is split into two files with clearly different owners:

| File | Owner | Contents |
|---|---|---|
| `config/torcherino-client.toml` | **client** | `gui.useGui` (open the editor on right click) and `gui.smoothSlider` (draggable slider handles) |
| `config/torcherino-server.toml` | **server / server owner** | recipe switches and the blacklist |

Keys of `torcherino-server.toml`:

| Key | Default | Meaning |
|---|---|---|
| `general.overPoweredRecipe` | `true` | Use the cheap recipe instead of the nether star one |
| `general.compressedTorcherino` | `false` | Recipes of the Compressed Torcherino / Jack o'Lanterino |
| `general.doubleCompressedTorcherino` | `false` | Double compressed recipes (compressed must be enabled too) |
| `general.tripleCompressedTorcherino` | `false` | Triple compressed recipes (compressed and double must be enabled too) |
| `blacklist.blacklistedBlocks` | `[]` | `modid:name` entries that must not be accelerated |
| `blacklist.blacklistedTiles` | `[]` | Fully qualified machine class names that must not be accelerated |

> ⚠️ Recipe switches are read while the **data pack loads**: restart or run `/reload` after a change,
> and in multiplayer edit the **server's** file — the client copy has no effect there.
> The three compression switches default to off (upstream behaviour); enable them in the server file
> if you want the compressed tiers.
>
> Upgrading: if the pre-split file `config/torcherino.toml` still exists, its values are copied into
> the two new files on first start.

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
