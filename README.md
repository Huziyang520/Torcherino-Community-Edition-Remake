# Torcherino Community Edition Remake

**Minecraft 1.20.1 · Forge / Fabric**

> [English](#english) ｜ [中文说明](#中文说明)

---

## English

**Torcherino Community Edition Remake** — a torch that makes everything around it run faster.

Place a Torcherino and the world inside its area speeds up: crops and saplings grow, furnaces smelt,
hoppers move, mob spawners spawn — anything that normally does something once per game tick simply
does it several times in the same tick.

### Features

- **Area acceleration** — accelerates crop growth, random ticks and machines in a whole area around it.
- **Adjustable area** — right-click to step through 16 area modes, from `Stopped` up to 15x15x15.
- **Adjustable speed** — hold the modifier key (**sneak** by default) while right-clicking to
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
- **Editor** — right-click opens a screen with four sliders (speed, X/Z/Y range) plus a redstone mode
  button; it can be turned off in favour of the classic quick interaction.
- **Translated** — the editor, the on-screen status text and every message follow your game language
  (13 languages).

### How to use

1. Place a Torcherino on the ground (placing it against a wall gives the wall variant).
2. **Right-click** it to open the **editor**: four sliders (speed, X range, Z range, Y range) plus a
   redstone mode button. Close it to apply; the values are stored in the block.
   **Sneak-right-click never opens the editor**, which keeps the vanilla interaction for placing
   blocks next to a Torcherino.
3. **Four redstone modes**: `Normal` (a signal stops it), `Inverted` (only runs with a signal),
   `Ignored` (always runs) and `Always off`.
4. Prefer the classic interaction? Set `gui.useGui = false` in `config/torcherino-client.toml` and you get
   **right-click to change the area** and **hold sneak + right-click to change the speed** again,
   with the action bar showing e.g. `Area: 3x3x3 | Speed: 200%`. Only one of the two modes is active.

> Slider feel is controlled by the client setting `gui.freeSpeedMultiplier`: `false` (default) pulls
> every handle onto the nearest step, `true` lets the handle be dragged continuously between steps
> while the reported value stays rounded.

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

The configuration is split into two files with clearly different owners. Both halves can also be
edited in game: the mod list entry opens a screen with a **Server settings** and a **Client settings**
tab, each with its own groups.

The screen is built with **Cloth Config**, which is an optional integration: the mod neither
bundles it nor requires it. With the API installed you get the two tabs (the server one holding a
`Recipes` and a `Performance` group); without it you simply edit the two files below. Cloth Config
is never written into the mod metadata, so nothing about your setup changes if you uninstall it.

| File | Owner | Contents |
|---|---|---|
| `config/torcherino-client.toml` | **client** | `gui.useGui` (open the editor on right click), `gui.freeSpeedMultiplier` (draggable slider handles), `gui.joinNotice` (chat notice on world join) |
| `config/torcherino-server.toml` | **server / server owner** | recipe switches and the blacklist |

Keys of `torcherino-server.toml`:

| Key | Default | Meaning |
|---|---|---|
| `general.overPoweredRecipe` | `true` | Use the cheap recipe instead of the nether star one |
| `general.compressedTorcherino` | `true` | Recipes of the Compressed Torcherino / Jack o'Lanterino |
| `general.doubleCompressedTorcherino` | `true` | Double compressed recipes (compressed must be enabled too) |
| `general.tripleCompressedTorcherino` | `true` | Triple compressed recipes (compressed and double must be enabled too) |
| `general.maxAcceleratedTicksEnabled` | `false` | **Switch** of the per Torcherino cap below. Off = uncapped |
| `general.maxAcceleratedTicksPerTick` | `16384` | Cap value, only read while the switch is on; `0` = unlimited |
| `general.maxAcceleratedTicksGlobalEnabled` | `false` | **Switch** of the server wide cap below |
| `general.maxAcceleratedTicksPerTickGlobal` | `65536` | Cap value for the **whole mod**, only read while the switch is on |
| `general.adaptiveThrottle` | `false` | Adaptive brake: slows down while the server tick is too slow |
| `general.adaptiveThrottleThresholdMs` | `50` | Tick time above which the brake starts scaling down |
| `general.adaptiveThrottleMaxDivisor` | `256` | Hardest the brake may scale the speed down |
| `general.skipSaturatedPositions` | `false` | **Lowers the rate.** Positions that stopped changing run once per tick |
| `general.probabilisticRandomTick` | `true` | **Different model, on by default since 1.4.1.** One roll instead of a burst |
| `general.ownerOnlyWhenOnline` | `false` | Only accelerate while the player who placed it is online |
| `general.statsLogIntervalSeconds` | `0` | Seconds between diagnostic log lines; `0` = silent |
| `blacklist.blacklistedBlocks` | `[]` | `modid:name` entries that must not be accelerated |
| `blacklist.blacklistedTiles` | `[]` | Fully qualified machine class names that must not be accelerated |

> ⚠️ Recipe switches are read while the **data pack loads**: restart or run `/reload` after a change,
> and in multiplayer edit the **server's** file — the client copy has no effect there.
> The three compression switches have been on by default since **1.4.0**; turn them off in the server
> file if you do not want the compressed tiers.
>
### Acceleration cap

A Torcherino is a deliberate load machine: one block asks the server to run a few thousand extra
growth and machine ticks for every block around it, and a large area multiplies that again. On a
weak server, or with a Triple Compressed Torcherino set to a high speed, that can pull the tick
time far above 50 ms. `general.maxAcceleratedTicksPerTick` is the safety valve for exactly that
case:

| Value | Meaning |
|---|---|
| `0` (default) | Unlimited — the behaviour of 1.4.0 and earlier |
| a positive number | Upper bound of acceleration calls one single Torcherino may spend per server tick |

When the budget is used up, that Torcherino simply stops for the current tick and carries on next
tick, starting from a rotating point, so every part of the area still gets accelerated and no block
is left out permanently. The budget is shared over many positions instead of being eaten by the
first one, so a capped Torcherino still accelerates a whole farm, just less often per block.

It applies **immediately** — unlike the recipe switches it needs no restart and no `/reload`.

Where to change it:

- in game: mod list → Torcherino → configuration → **Server settings** → `Performance`, the row
  `Acceleration cap` (a plain number field; `0` means unlimited);
- or directly in `config/torcherino-server.toml`.

If a large farm or a Triple Compressed Torcherino makes the server stutter, a value between `8192`
and `65536` is a good starting point.

### Global cap, and what the other performance switches do

Everything below is **off by default**: with the shipped configuration the mod plays your speed back
exactly, and these are the tools you reach for when one server cannot take it any more. All of them
apply immediately, and all of them are also in the in-game `Performance` group.

**`general.maxAcceleratedTicksPerTickGlobal`** — the per Torcherino cap above is useless when a
player simply builds many of them. This one bounds the whole mod: the allowance is divided over the
Torcherinos that scanned in the previous tick, so none of them can eat the pool and leave the rest of
the world starved.

**`general.adaptiveThrottleThresholdMs` / `general.adaptiveThrottleMaxDivisor`** — the knobs of the
adaptive brake. The threshold is where "slow" starts (raise it on a heavy modpack whose healthy tick
is already longer than 50 ms), the divisor is the hardest it may brake, so a stuck server cannot
scale the acceleration down to nothing.

**`general.skipSaturatedPositions`** — ⚠️ **this one lowers the configured rate.** A farm that is
already fully grown stops changing: every extra call is spent on a block that will not do anything
with it. With this switch on, a position whose block and the 26 blocks around it stayed identical for
two ticks is accelerated once per tick until something moves again. Positions on the border of a
chunk section keep running at full rate (their neighbourhood cannot be read cheaply). Turn it on when
you want big *saturated* farms to stop eating the tick, and accept that stable blocks no longer run
at your configured multiplier.

**`general.probabilisticRandomTick`** — ⚠️ **this one changes the model, not just the amount.**
Normally a position gets `multiplier` random ticks in a row. With this on, it gets a single random
tick with the probability `multiplier × randomTickSpeed / 4096`, which is the vanilla random tick
model scaled by your multiplier: the expected growth matches, but the tick cost becomes proportional
to the number of positions instead of positions × multiplier. This is the mode to use with a very
high multiplier over a very large area; growth is then statistical rather than call for call.

**`general.ownerOnlyWhenOnline`** — an abandoned Torcherino keeps costing a server after its owner
left. With this on, a Torcherino only accelerates while the player who placed it is online.
Torcherinos without a recorded owner (placed before 1.4.1, or placed by a machine) keep running.

**`general.statsLogIntervalSeconds`** — diagnostics. Every N seconds the server log gets one line
with how much work the acceleration loop actually asked for (positions, extra random ticks, extra
block entity ticks, skipped sections, brake divisor, remaining global budget). Useful to tell "this
mod is the problem" apart from "this machine is the problem". `0` (default) keeps it silent.

### Adaptive brake

`general.adaptiveThrottle` (default `false`) is the automatic counterpart of the cap above.
Instead of a fixed number it measures what the server is actually doing:

- as long as the tick stays at or below **50 ms** (TPS 20) everything runs at the configured
  speed, untouched;
- once a tick takes longer, every Torcherino divides its acceleration by the overshoot
  (a 150 ms tick → one third of the speed) and returns to full speed by itself as soon as the
  server recovers.

So it never limits a healthy server, and it never leaves a struggling one to die. The trade-off
is that the effective speed depends on the machine, which is exactly why it is off by default:
switched off, the speed you configured is the speed you get.

It is the recommended switch for servers that host big farms or where several players run high
tier Torcherinos at once. It applies immediately, and it can be toggled in game (mod list →
**Server settings** → `Adaptive brake`).
>
> On joining a world you get a short chat notice about exactly that. It can be silenced with
> `gui.joinNotice = false` in the client file.
>
> Upgrading: if the pre-split file `config/torcherino.toml` still exists, its values are copied into
> the two new files on first start.

### Owner, permissions and Jade

A Torcherino remembers **who placed it** (the UUID is stored in the block entity), and the editor
shows that owner on the line under the panel:

- **unclaimed** — everyone may edit, exactly like before;
- **claimed** — the owner can switch between `Others may edit` and `Owner only` with one click, and
  drop the claim entirely with `Unclaim`. Only the owner sees those two buttons; everyone else just
  reads the state.
- The claim never changes by itself: breaking the torch keeps the owner and the settings inside the
  dropped item, and placing it again keeps them, whoever places it. Only `Unclaim` clears the claim.
- A claim only restricts **editing**. It never changes how fast the torch runs - stopping an
  abandoned torch is what the optional `general.ownerOnlyWhenOnline` switch is for.

The black lists can be edited visually as well: **Server settings** → `Black list` holds both lists
with add and remove buttons (`modid:block` for blocks, fully qualified class names for block
entities), with an example in the tooltip. They apply immediately after saving.

With **Jade** installed, the tooltip of every Torcherino gains three lines, each with its own switch
in Jade's plugin configuration: the acceleration multiplier, the running state (with a note when
redstone is involved) and the owner. Jade stays optional - without it nothing changes.

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

---

## 中文说明

**加速火把 社区版重制版**

### 功能

- **范围加速** —— 自动加速周围一整块区域里的作物生长、随机刻事件与机器运作。
- **范围可调** —— 右键即可切换 16 档范围，从「已停止」一直到 15x15x15，想只加速一格也行。
- **速度可调** —— 按住改装键（默认**潜行**）再右键，切换 8 档速度，最高 **800%**。
- **四个等级** —— 普通、**压缩 ×9**、**二重压缩 ×81**、**三重压缩 ×729**，越压缩越猛。
- **两种类型** —— 加速火把（可以插地上，也可以贴墙）和南瓜外形的**加速南瓜灯**。
- **红石可控** —— 给加速火把通上红石信号，它就立刻停止工作；断掉信号又恢复。
- **状态保留** —— 范围、速度和红石状态都会保存，拆下来再放回去还是原来的设置，不会重置。
- **黑名单** —— 服主/房主可以指定哪些方块 / 机器不许被加速。
- **编辑界面** —— 右键方块打开界面，四条滑条（速度 / X / Z / Y 范围）加一个红石模式按钮；
  不想要界面也可以在配置里换回经典快捷操作。

### 怎么用

1. 手持加速火把，对着地面右键放下（对着墙放则会变成贴墙形态）。
2. **右键**它：打开**可视化编辑界面**。界面里有四条滑条 —— 速度、X 轴范围、Z 轴范围、Y 轴范围 ——
   外加一个红石模式按钮；调完关掉界面立刻生效，数值保存在方块里（拆掉再放回不丢）。
   **潜行时右键不会打开界面**（保持原版行为），方便你在它旁边正常放方块。
3. **红石模式共四档**：`正常`（有红石信号就停止）、`反向`（有红石信号才工作）、
   `忽略红石`（一直工作）、`始终关闭`。
4. 想换回老式的快捷操作：把 `config/torcherino-client.toml` 里的 `gui.useGui` 改成 `false`，
   之后**右键切范围**、**按住潜行再右键切速度**，行动栏会显示如 `范围: 3x3x3 | 速度: 200%`。
   两种方式**只会生效一种**。

> 滑条手感由客户端配置 `gui.freeSpeedMultiplier` 决定：默认 `false` = 手柄被吸附到最近的整档；
> `true` = 手柄可在档位之间**连续拖动**（数值仍按整档取），两者手感差异明显。

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

配置拆成两份，各归各的。两份都可以在游戏里改：模组列表里点「配置」会打开一个界面，
分成**服务端配置**与**客户端配置**两个标签页，服务端页里再分「配方」与「性能」两个分组。

配置界面由 **Cloth Config** 构建

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

> ⚠️ 配方开关是在**数据包加载时**读取的：改完要**重启**或执行 `/reload` 才会生效；
> 多人游戏里请改**服务端**那份文件，客户端改的不管用。
> 三个压缩开关自 **1.4.0** 起**默认开启**；不想要压缩系就在服务端文件里关掉。
>
### 加速上限（性能保险阀）

加速火把本身就是一个"主动制造负载"的方块：它会让周围每一格都多跑几千次生长 / 机器逻辑，
范围一大还要再乘一次。服务器偏弱、或者用三重压缩火把开到高倍率时，单 tick 耗时很容易冲过 50ms。
`general.maxAcceleratedTicksPerTick` 就是为这种情况准备的：

| 取值 | 含义 |
|---|---|
| `0`（默认） | 不限制 —— 与 1.4.0 及更早版本行为完全一致 |
| 任意正数 | 单个加速火把在一个服务端 tick 内最多消耗的加速次数 |

额度用完后，这个火把在本 tick 直接收手，下一 tick 从**轮换过的位置**接着扫 ——
所以区域里没有哪一块会被永久漏掉，只是每格被加速的次数变少。额度还会尽量平摊到多个方块上，
而不是被第一格一口吃光，所以开了上限之后它依然在带动整片农场。

**改完立即生效**，不像配方开关那样需要重启或 `/reload`。

在哪里改：

- 游戏内：模组列表 → Torcherino → 配置 → **服务端配置** → `性能` 分组，`加速上限` 这一行是个
  数字输入框（`0` 表示不限制）；
- 或者直接改 `config/torcherino-server.toml`。

如果大片农场或三重压缩火把已经让服务器卡顿，建议先填 `8192` ~ `65536` 之间的值试试。

### 全局上限，以及其余性能开关都干了什么

下面这些**全部默认关闭**：原样运行时火把就按你设定的速度跑，它们只是服务器实在撑不住时的工具。
全部**改完立即生效**，也都能在游戏内的 `性能` 分组里改。

**`general.maxAcceleratedTicksPerTickGlobal`（全局上限）** —— 上面那个"单火把上限"在玩家一口气
堆很多把火把时就失效了。这个管的是整个模组：额度会在上一 tick 参与扫描的火把之间分摊，
所以没有哪一把能把池子吃光、把世界其它地方饿着。

**`general.adaptiveThrottleThresholdMs` / `general.adaptiveThrottleMaxDivisor`（刹车阈值 / 刹车倍率上限）**
—— 自适应刹车的两个旋钮。阈值是"从多慢开始算慢"（重整合包的常态 tick 本来就超过 50ms 时往上调），
倍率上限是"最多降速到几分之一"，避免服务器彻底卡死时把加速直接降成零。

**`general.skipSaturatedPositions`（跳过已饱和位置）** —— ⚠️ **这一项会降低你设定的速率。**
已经长满的农场不再变化，之后每一次补刻都是花在"给了也不会有反应"的方块上。开启后，
某个位置及其周围 26 格方块连续两 tick 完全没变，就只保留每 tick 一次补刻，直到附近再次变动为止。
位于区块段边界上的位置仍按全速运行（它们的邻域无法廉价读取）。想让**大型饱和农场**不再吃满 tick 就打开它，
代价是稳定的方块不再按你设定的倍率运行。

**`general.probabilisticRandomTick`（概率式随机刻）** —— ⚠️ **这一项换的是模型，不只是数量。**
平时一个位置会被连续补 `倍率` 次随机刻；开启后改成掷一次骰子：以 `倍率 × 随机刻速率 / 4096` 的概率补一次随机刻
—— 也就是把原版随机刻速率按你的倍率放大。期望增长量一致，但单 tick 成本从"位置数 × 倍率"变成"位置数"。
极高倍率 + 极大范围的场景就该用它；代价是生长变成统计意义上的，而不是逐一补刻。

**`general.ownerOnlyWhenOnline`（仅所有者在线时加速）** —— 主人走了、火把还在白烧服务器。
开启后只有放置该火把的玩家在线时它才会加速。没有记录所有者的火把（1.4.1 之前放置的、或由机器放置的）照常运行。

**`general.statsLogIntervalSeconds`（统计日志间隔）** —— 诊断用。每 N 秒向服务器日志输出一行：
加速循环实际要求了多少工作（扫描位置数、实际补刻次数、方块实体补刻次数、整段跳过的区块段数、
当前刹车倍率、剩余全局额度）。用来区分"是这个模组的问题"还是"是这台机器的问题"。`0`（默认）不输出。

### 自适应刹车

`general.adaptiveThrottle`（默认 `false`）是上面那个上限的"自动版"，不给固定数字，而是看服务器实际在干什么：

- 单 tick ≤ **50ms**（TPS 20）时，一切都按你设定的速度跑，不做任何干预；
- 一旦某 tick 超过 50ms，所有加速火把按超出倍数等比降速（150ms 的 tick → 降到三分之一），
  服务器一恢复就自动回到全速。

也就是说：服务器健康时它完全不插手，服务器撑不住时它不会袖手旁观。代价是"实际速度取决于机器"，
这也正是它默认关闭的原因 —— 关掉时，你设多少就是多少。

推荐给开了大农场、或者同时有多人挂高压缩火把的服务器使用。**改完立即生效**，
也可以在游戏内切换（模组列表 → **服务端配置** → `自适应刹车`）。
>
> 进入世界时聊天栏会有一条关于这一点的提示，可用客户端文件里的 `gui.joinNotice = false` 关掉。
>
> 从旧版本升级：若还存在拆分前的单文件 `config/torcherino.toml`，首次启动会把其中的取值搬进上面两个新文件。

### 所有者、权限与 Jade

加速火把会记住**是谁放的**（UUID 存在方块实体里），编辑界面在面板下方显示这一行：

- **无所有者** —— 和以前一样，谁都能改；
- **已认领** —— 所有者可以一键在 `他人可改配置` / `他人不可改` 之间切换，也可以点 `取消认领` 变回无所有者。
  这两个按钮**只有所有者看得到**，其他玩家只能看到当前状态。
- 归属不会自己变：**破坏火把时所有者与全部设置会随掉落物保留**，重新放下时（无论谁放）都会还原；
  只有原所有者点「取消认领」才会清空，之后谁放下来就归谁。
- 归属只限制**能否改配置**，不影响加速效果 —— 想停掉被遗弃的火把，请用可选的 `general.ownerOnlyWhenOnline`。

黑名单也能可视化编辑：**服务端配置** → `黑名单` 分组里是两个带增删按钮的列表
（方块填 `modid:方块名`，方块实体填完整类名），tooltip 里有示例；保存后**立即生效**。

装了 **Jade** 时，对准任一加速火把会多出三行：**加速倍率**、**启用状态**（受红石影响时会注明）、
**所有者**（含「无所有者」）。这三行各自是 Jade 插件配置里的一个开关，可以在 `config/jade/` 里逐项关闭；
没装 Jade 时一切照旧。

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
