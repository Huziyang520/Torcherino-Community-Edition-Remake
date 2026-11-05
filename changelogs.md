## 1.7.0

### Changed

- The jukebox is part of the default blacklist, so a Torcherino never speeds it up. Existing configurations gain the entry once.
  唱片机已加入默认黑名单，加速火把不会加速它；已有配置会在首次启动时补上这一条。

### Fixed

- Fixed the crafting recipes of the Torcherino and the Lanterino not showing up.
  修复了加速火把与加速南瓜灯的合成配方不显示的问题。

- Fixed the Torcherino dropping nothing when broken in survival.
  修复了生存模式破坏加速火把后不掉落物品的问题。

- Fixed the blocks not being mineable with the correct tool.
  修复了方块无法用正确工具采集的问题。

## 1.6.0

### Added

- Owner system master switch (`general.ownerSystem`, off by default).
  所有者系统总开关（`general.ownerSystem`，默认关闭）。

- The editor can be closed with **E**, the same key that closes the inventory.
  编辑界面可以用 **E** 关闭，与关闭物品栏是同一个键。

- On NeoForge the configuration screen comes from the loader; on Fabric it is still Cloth Config.
  NeoForge 端的配置界面由加载器提供；Fabric 端仍然使用 Cloth Config。

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

- Fixed the editor panel looking blurred while the buttons stayed sharp.
  修复了编辑界面面板发虚、按钮却清晰的问题。

- Fixed the configuration screen showing raw translation keys.
  修复了配置界面显示裸翻译键名的问题。

- Fixed the configuration screen tooltips not being translated.
  修复了配置界面悬停提示没有翻译的问题。

- Fixed the two blacklists in the configuration screen not being editable.
  修复了配置界面里两个黑名单无法编辑的问题。
