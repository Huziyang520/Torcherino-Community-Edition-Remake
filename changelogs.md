## 1.6.0

### Added

- **Owner system master switch** (`general.ownerSystem`, off by default). While it is off nothing is
  claimed, the editor shows no owner row and no owner buttons, and everybody may edit. Claims that
  are already stored in a world are kept untouched and come back when the switch is turned on again.
  所有者系统总开关（`general.ownerSystem`，默认关闭）：关闭时不会认领任何火把，编辑界面不显示所有者一行与所有者按钮，任何人都能修改。世界里已经保存的认领数据原样保留，开关重新打开后恢复生效。

- The editor can be closed with **E**, the same key that closes the inventory.
  编辑界面可以用 **E** 关闭，与关闭物品栏是同一个键。

- Every release now ships this changelog next to the README.
  每个版本都会在 README 旁附带本更新日志。

### Changed

- The slider highlight is a texture instead of a painted rectangle, so the whole editor is textured.
  滑条高亮改用贴图，不再用程序画矩形，整个编辑界面都是贴图 UI。

- The chat notice printed on world join now names the configuration screen as well: on 1.20-1.20.1 and
  on Fabric the screen comes from Cloth Config API.
  进入世界时的聊天提示现在也会说明配置界面的来源：1.20-1.20.1 与 Fabric 端由 Cloth Config API 提供。

- Only the chat notice is localized for English, Simplified Chinese and Traditional Chinese; every
  other language falls back to English.
  只有这条聊天提示做了英、简中、繁中本地化，其余语言回退到英文。

- `changelogs.md` is the single place that lists what changed between releases.
  `changelogs.md` 是唯一列出各版本改动的文件。

### Fixed

- A hovered slider no longer turns solid white (the highlight texture was drawn without blending, so
  its transparency was ignored and the white label on the track became unreadable).
  鼠标悬停的滑条不再变成纯白块（高亮贴图绘制时没开混合，透明度被忽略，轨道上的白字看不清）。

- A slider no longer stays highlighted after it was dragged: only the hover state lights it up now.
  拖动结束后滑条不再保持高亮，只有悬停状态才点亮。

- The redstone button no longer repeats its own label in a tooltip - the button text already
  states the current mode, so hovering it only got in the way.
  红石按钮不再用提示框重复自己的文案：按钮文字已经写明当前模式，悬停提示只会碍事。
