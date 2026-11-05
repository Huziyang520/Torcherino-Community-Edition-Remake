## 1.7.0

### Changed

- The jukebox is part of the default blacklist, so a Torcherino never speeds it up. Existing configurations gain the entry once.
  唱片机已加入默认黑名单，加速火把不会加速它；已有配置会在首次启动时补上这一条。

- The 26.1-26.2 and 26.3 lines are built from one source tree; install the file that matches your game version.
  26.1-26.2 与 26.3 两条线由同一份源码构建，请安装与游戏版本对应的文件。

### Fixed

- Fixed the editor dimming layer flickering when it opens.
  修复了打开编辑界面时调暗层闪烁的问题。

- Fixed the game crashing when the editor is opened on 26.2.
  修复了在 26.2 上打开编辑界面时崩溃的问题。

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

- Fixed the editor panel and sliders being out of place.
  修复了编辑界面面板与滑块错位的问题。

- Fixed the configuration screen showing raw translation keys.
  修复了配置界面显示裸翻译键名的问题。

- Fixed the configuration screen tooltips not being translated.
  修复了配置界面悬停提示没有翻译的问题。

- Fixed the two blacklists in the configuration screen not being editable.
  修复了配置界面里两个黑名单无法编辑的问题。

- Fixed items showing as missing-texture blocks in the inventory and in the hand.
  修复了物品在物品栏与手中显示为紫黑方块的问题。

- Fixed crafting recipes being lost.
  修复了合成配方丢失的问题。

- Fixed the mod description showing garbled characters.
  修复了模组简介乱码的问题。

- Fixed a crash when joining a world.
  修复了进入世界时崩溃的问题。
