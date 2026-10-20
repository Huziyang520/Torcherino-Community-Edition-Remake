## 1.6.0

### Added

- **Owner system master switch** (`general.ownerSystem`, off by default). While it is off nothing is
  claimed, the editor shows no owner row and no owner buttons, and everybody may edit. Claims that
  are already stored in a world are kept untouched and come back when the switch is turned on again.
- The editor can be closed with **E**, the same key that closes the inventory.
- Every release now ships this changelog next to the README.

### Changed

- The slider highlight is a texture instead of a painted rectangle, so the whole editor is textured.
- The chat notice printed on world join now names the configuration screen as well: on 1.20-1.20.1 and
  on Fabric the screen comes from Cloth Config API.
- Only the chat notice is localized for English, Simplified Chinese and Traditional Chinese; every
  other language falls back to English.
- `changelogs.md` is the single place that lists what changed between releases.

### Fixed

- A hovered slider no longer turns solid white (the highlight texture was drawn without blending, so
  its transparency was ignored and the white label on the track became unreadable).
- A slider no longer stays highlighted after it was dragged: only the hover state lights it up now.
- The redstone button no longer repeats its own label in a tooltip - the button text already
  states the current mode, so hovering it only got in the way.
