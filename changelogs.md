## 1.7.1

### Changed

- The configuration screen no longer needs Cloth Config: it is now NeoForge's own configuration screen, brought to this version by Neo Configuration Screen Port, on Forge and Fabric alike. Forge Config API Port (Fabric) and Neo Configuration Screen Port (both loaders) are required.
  配置界面不再依赖 Cloth Config：改用 NeoForge 的配置界面，由 Neo Configuration Screen Port 移植到本版本，Forge 与 Fabric 表现一致；Forge 端需要 Neo Configuration Screen Port，Fabric 端还需要 Forge Config API Port。

- The chat notice after joining a world now names the mod, invites feedback on the mod, and says where the notice can be turned off.
  进入世界的提示改为：本模组有任何问题或建议都可以提交给作者，并说明该提示可在配置界面或配置文件中关闭。

## 1.7.0

### Changed

- The jukebox is part of the default blacklist, so a Torcherino never speeds it up. Existing configurations gain the entry once.
  唱片机已加入默认黑名单，加速火把不会加速它；已有配置会在首次启动时补上这一条。

## 1.6.0

### Added

- Owner system master switch (`general.ownerSystem`, off by default).
  所有者系统总开关（`general.ownerSystem`，默认关闭）。

- The editor can be closed with **E**, the same key that closes the inventory.
  编辑界面可以用 **E** 关闭，与关闭物品栏是同一个键。

### Changed

- The slider highlight is a texture, so the whole editor is textured.
  滑条高亮改用贴图，整个编辑界面都是贴图 UI。

- The chat notice on world join now also says where the configuration screen comes from.
  进入世界时的聊天提示现在也会说明配置界面来自哪里。

- The chat notice is translated into English, Simplified Chinese and Traditional Chinese only; every other language uses English.
  聊天提示只有英、简中、繁中三种翻译，其余语言使用英文。

### Fixed

- Fixed the slider turning solid white when hovered.
  修复了鼠标悬停时滑条变成纯白块的问题。

- Fixed the slider staying highlighted after it was dragged.
  修复了拖动之后滑条仍然保持高亮的问题。

- Fixed the redstone button repeating its own text in its tooltip.
  修复了红石按钮悬停提示重复按钮文案的问题。
