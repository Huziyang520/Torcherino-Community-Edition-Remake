## 1.6.0

### Added

- **Owner system master switch** (`general.ownerSystem`, off by default). While it is off nothing is
  claimed, the editor shows no owner row and no owner buttons, and everybody may edit. Claims that
  are already stored in a world are kept untouched and come back when the switch is turned on again.
- The editor can be closed with **E**, the same key that closes the inventory.
- On NeoForge the configuration screen comes from the loader (`ConfigurationScreen`); on Fabric it is
  still Cloth Config.

### Changed

- The slider highlight is a texture instead of a painted rectangle, so the whole editor is textured.
- The chat notice printed on world join now names the configuration screen as well: on 1.20-1.20.1 and
  on Fabric the screen comes from Cloth Config API.
- Only the chat notice is localized for English, Simplified Chinese and Traditional Chinese; every
  other language falls back to English.

### Fixed

- A hovered slider no longer turns solid white (the highlight texture was drawn without blending, so
  its transparency was ignored and the white label on the track became unreadable) and no longer
  stays lit after a drag.
- The editor panel is no longer treated as world content: the blurred background pass made the panel
  soft while the widgets stayed sharp. The screen now dims without the blur pass.
- The NeoForge configuration screen no longer shows raw translation keys, and every section and entry
  has a label.
- The redstone button no longer repeats its own label in a tooltip - the button text already
  states the current mode, so hovering it only got in the way.
- The NeoForge configuration screen tooltips are localized as well; earlier only the labels were.
