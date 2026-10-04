/*
 * 本文件：基于 Cloth Config API 的模组配置界面（可选联动）。
 * 说明：只在 Cloth Config 已安装时才会被加载（调用点全部带存在性判断），模组不内置也不依赖它。
 *      结构分两级：分类（服务端配置 / 客户端配置）→ 服务端子分类下的可折叠分组（配方 / 性能 / 黑名单）。
 *      条目读写的是本模组自己的静态字段；保存时先强制刷新每个控件，再走 TorcherinoConfig 落盘，
 *      因此"保存并退出"不会漏掉刚输入但还没失去焦点的值。
 *      自建的开关 / 数字条目只在鼠标位于左侧名称上时显示提示（悬停到控件上不弹），见 LabelTooltipToggle。
 */
package com.sci.torcherino.client.screen;

import com.sci.torcherino.TorcherinoConfig;
import com.sci.torcherino.TorcherinoRegistry;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The configuration screen, built with the optional Cloth Config API.
 *
 * <p>Two levels of structure: categories, which Cloth renders as tabs, and - inside the
 * server category - collapsible groups for the recipe switches, for the performance and
 * governance entries (both acceleration caps with their switches, the adaptive brake with
 * its threshold and divisor, the saturation skip, the probabilistic random tick model, the
 * owner gate and the diagnostic log interval) and for the two black lists. The entries do
 * not own any storage: they read and write the shared {@link TorcherinoConfig} fields, and
 * closing the screen hands the values back to the ordinary save path of the loader.</p>
 *
 * <p><b>Optional integration.</b> Every caller checks that Cloth Config is installed before
 * touching this class, so the class itself is never loaded - and its absence never noticed -
 * on a client without the API.</p>
 */
public final class TorcherinoClothConfig {

    /**
     * Width of the control column on the right hand side of a row: the widget plus its reset
     * button. The tooltips of the rows built below stop at that line, so a long explanation
     * can never cover the control the player is aiming at.
     */
    private static final int CONTROL_COLUMN_WIDTH = 165;

    /** The label Cloth puts on the reset button of an entry. */
    private static final Component RESET_KEY = Component.translatable("text.cloth-config.reset_value");

    private TorcherinoClothConfig() {
    }

    /**
     * @param parent the screen the configuration returns to when it is closed.
     * @return a ready to display configuration screen.
     */
    public static Screen create(Screen parent) {
        // Every entry that owns a value is collected here: saving flushes them explicitly,
        // see save(List).
        final List<AbstractConfigListEntry> entriesToFlush = new ArrayList<>();

        final ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("gui.torcherino.config.title"))
                .setDoesConfirmSave(false)
                .setSavingRunnable(() -> save(entriesToFlush));
        final ConfigEntryBuilder entries = builder.entryBuilder();

        // ---- server ---------------------------------------------------------------------
        final ConfigCategory server = builder.getOrCreateCategory(Component.translatable("gui.torcherino.config.server"));

        final List<AbstractConfigListEntry> recipes = new ArrayList<>();
        recipes.add(toggle(entriesToFlush, "overPoweredRecipe", () -> TorcherinoConfig.overPoweredRecipe,
                value -> TorcherinoConfig.overPoweredRecipe = value, true));
        recipes.add(toggle(entriesToFlush, "compressedTorcherino", () -> TorcherinoConfig.compressedTorcherino,
                value -> TorcherinoConfig.compressedTorcherino = value, true));
        recipes.add(toggle(entriesToFlush, "doubleCompressedTorcherino",
                () -> TorcherinoConfig.doubleCompressedTorcherino,
                value -> TorcherinoConfig.doubleCompressedTorcherino = value, true));
        recipes.add(toggle(entriesToFlush, "tripleCompressedTorcherino",
                () -> TorcherinoConfig.tripleCompressedTorcherino,
                value -> TorcherinoConfig.tripleCompressedTorcherino = value, true));
        server.addEntry(entries.startSubCategory(group("recipes"), recipes).build());

        final List<AbstractConfigListEntry> performance = new ArrayList<>();
        // Both caps are a switch plus a value: the switch decides whether the cap is used at
        // all, the value says how much. Off by default, so an untouched configuration plays
        // the speed back exactly as configured.
        performance.add(toggle(entriesToFlush, "maxAcceleratedTicks",
                () -> TorcherinoConfig.maxAcceleratedTicksEnabled,
                value -> TorcherinoConfig.maxAcceleratedTicksEnabled = value, false,
                tooltip("maxAcceleratedTicks")));
        performance.add(intField(entriesToFlush, "maxAcceleratedTicksValue",
                TorcherinoConfig.maxAcceleratedTicksPerTick, 16384, 0, Integer.MAX_VALUE,
                value -> TorcherinoConfig.maxAcceleratedTicksPerTick = value));
        performance.add(toggle(entriesToFlush, "maxAcceleratedTicksGlobal",
                () -> TorcherinoConfig.maxAcceleratedTicksGlobalEnabled,
                value -> TorcherinoConfig.maxAcceleratedTicksGlobalEnabled = value, false,
                tooltip("maxAcceleratedTicksGlobal")));
        performance.add(intField(entriesToFlush, "maxAcceleratedTicksGlobalValue",
                TorcherinoConfig.maxAcceleratedTicksPerTickGlobal, 65536, 0, Integer.MAX_VALUE,
                value -> TorcherinoConfig.maxAcceleratedTicksPerTickGlobal = value));
        performance.add(toggle(entriesToFlush, "adaptiveThrottle", () -> TorcherinoConfig.adaptiveThrottle,
                value -> TorcherinoConfig.adaptiveThrottle = value, false, tooltip("adaptiveThrottle")));
        performance.add(intField(entriesToFlush, "adaptiveThrottleThreshold",
                TorcherinoConfig.adaptiveThrottleThresholdMs, 50, 10, 10000,
                value -> TorcherinoConfig.adaptiveThrottleThresholdMs = value,
                tooltip("adaptiveThrottleThreshold")));
        performance.add(intField(entriesToFlush, "adaptiveThrottleMaxDivisor",
                TorcherinoConfig.adaptiveThrottleMaxDivisor, 256, 1, 100000,
                value -> TorcherinoConfig.adaptiveThrottleMaxDivisor = value,
                tooltip("adaptiveThrottleMaxDivisor")));
        performance.add(toggle(entriesToFlush, "skipSaturatedPositions", () -> TorcherinoConfig.skipSaturatedPositions,
                value -> TorcherinoConfig.skipSaturatedPositions = value, false,
                tooltipWithWarning("skipSaturatedPositions", "skipSaturatedPositions.warning")));
        performance.add(toggle(entriesToFlush, "probabilisticRandomTick", () -> TorcherinoConfig.probabilisticRandomTick,
                value -> TorcherinoConfig.probabilisticRandomTick = value, true, tooltip("probabilisticRandomTick")));
        performance.add(toggle(entriesToFlush, "ownerOnlyWhenOnline", () -> TorcherinoConfig.ownerOnlyWhenOnline,
                value -> TorcherinoConfig.ownerOnlyWhenOnline = value, false, tooltip("ownerOnlyWhenOnline")));
        performance.add(intField(entriesToFlush, "statsLogInterval", TorcherinoConfig.statsLogIntervalSeconds,
                0, 0, 86400, value -> TorcherinoConfig.statsLogIntervalSeconds = value,
                tooltip("statsLogInterval")));
        server.addEntry(entries.startSubCategory(group("performance"), performance).build());

        final List<AbstractConfigListEntry> blacklist = new ArrayList<>();
        blacklist.add(strList(entriesToFlush, entries, "blacklistedBlocks", TorcherinoConfig.blacklistedBlocks,
                value -> TorcherinoConfig.blacklistedBlocks = value, tooltip("blacklistedBlocks")));
        blacklist.add(strList(entriesToFlush, entries, "blacklistedTiles", TorcherinoConfig.blacklistedTiles,
                value -> TorcherinoConfig.blacklistedTiles = value, tooltip("blacklistedTiles")));
        server.addEntry(entries.startSubCategory(group("blacklist"), blacklist).build());

        server.addEntry(text(entries, "gui.torcherino.config.serverNote"));
        server.addEntry(text(entries, "gui.torcherino.config.serverReload"));

        // ---- client ---------------------------------------------------------------------
        final ConfigCategory client = builder.getOrCreateCategory(Component.translatable("gui.torcherino.config.client"));
        client.addEntry(toggle(entriesToFlush, "useGui", () -> TorcherinoConfig.useGui,
                value -> TorcherinoConfig.useGui = value, true));
        client.addEntry(toggle(entriesToFlush, "freeSpeedMultiplier", () -> TorcherinoConfig.freeSpeedMultiplier,
                value -> TorcherinoConfig.freeSpeedMultiplier = value, false));
        client.addEntry(toggle(entriesToFlush, "joinNotice", () -> TorcherinoConfig.joinNotice,
                value -> TorcherinoConfig.joinNotice = value, true));

        return builder.build();
    }

    /**
     * Hands both halves back to the ordinary save path of the loader.
     *
     * <p>The widgets are flushed first. Cloth runs the save consumers on its own as well, but a
     * value typed into a field that was never committed (no Enter, focus still inside) could
     * reach the file as the previous one - that is the "save and quit did not save it once"
     * report. Asking every entry to save right here reads what is on the screen at this moment.</p>
     */
    private static void save(List<AbstractConfigListEntry> entries) {
        for (AbstractConfigListEntry entry : entries) {
            entry.save();
        }
        TorcherinoConfig.saveServer();
        TorcherinoConfig.saveClient();
        // The black lists are copied out of the configuration into the registry, so rebuilding
        // it here makes an edited list take effect without a restart - the recipe switches
        // still need a data pack reload, which is what the note on the screen says.
        TorcherinoRegistry.registerDefaults();
    }

    private static AbstractConfigListEntry text(ConfigEntryBuilder entries, String key) {
        return entries.startTextDescription(Component.translatable(key)).build();
    }

    private static AbstractConfigListEntry toggle(List<AbstractConfigListEntry> flush, String key,
                                                  Supplier<Boolean> getter, Consumer<Boolean> setter,
                                                  boolean fallback, Component... tooltip) {
        final AbstractConfigListEntry entry =
                new LabelTooltipToggle(label(key), getter.get(), () -> fallback, setter::accept, tooltip);
        flush.add(entry);
        return entry;
    }

    private static AbstractConfigListEntry intField(List<AbstractConfigListEntry> flush, String key, int value,
                                                    int fallback, int min, int max, Consumer<Integer> setter,
                                                    Component... tooltip) {
        final AbstractConfigListEntry entry =
                new LabelTooltipIntField(label(key), value, fallback, min, max, setter::accept, tooltip);
        flush.add(entry);
        return entry;
    }

    private static AbstractConfigListEntry strList(List<AbstractConfigListEntry> flush, ConfigEntryBuilder entries,
                                                   String key, List<String> value, Consumer<List<String>> setter,
                                                   Component... tooltip) {
        final AbstractConfigListEntry entry = entries.startStrList(label(key), new ArrayList<>(value))
                .setDefaultValue(new ArrayList<String>())
                .setExpanded(false)
                .setDeleteButtonEnabled(true)
                .setTooltip(tooltip)
                .setSaveConsumer(list -> setter.accept(new ArrayList<>(list)))
                .build();
        flush.add(entry);
        return entry;
    }

    private static Component label(String key) {
        return Component.translatable("gui.torcherino.config." + key);
    }

    private static Component group(String key) {
        return Component.translatable("gui.torcherino.config.group." + key);
    }

    /**
     * Tooltip lines. A long explanation does not fit on one line, so the translation carries
     * explicit {@code \n} breaks; every break becomes its own component, because Cloth hands
     * each element of the array to the vanilla tooltip renderer as a separate line.
     */
    private static Component[] tooltip(String key) {
        final String text = Component.translatable("gui.torcherino.config.tooltip." + key).getString();
        final String[] lines = text.split("\n");
        final Component[] result = new Component[lines.length];
        for (int i = 0; i < lines.length; i++) {
            result[i] = Component.literal(lines[i]);
        }
        return result;
    }

    /**
     * Tooltip plus one red warning line. The warning is a translation of its own so only that
     * line is coloured - colouring the whole explanation would drown the warning out.
     */
    private static Component[] tooltipWithWarning(String key, String warningKey) {
        final Component[] lines = tooltip(key);
        final Component[] result = Arrays.copyOf(lines, lines.length + 1);
        result[lines.length] = Component.translatable("gui.torcherino.config.tooltip." + warningKey)
                .withStyle(ChatFormatting.RED);
        return result;
    }

    /**
     * Toggle whose tooltip only appears while the mouse is over the name of the row.
     *
     * <p>Cloth shows an entry tooltip whenever the mouse is anywhere inside the row, which
     * includes the button, the input field and the reset button: a long explanation then
     * covers exactly the control the player wants to click. These two subclasses cut the hover
     * area at the control column instead. Two of them are needed because Java has single
     * inheritance - one extends the toggle, one the integer field.</p>
     */
    private static final class LabelTooltipToggle extends BooleanListEntry {

        private LabelTooltipToggle(Component name, boolean value, Supplier<Boolean> fallback,
                                   Consumer<Boolean> setter, Component[] tooltip) {
            super(name, value, RESET_KEY, fallback, setter, () -> Optional.of(tooltip));
        }

        @Override
        public boolean isMouseInside(int mouseX, int mouseY, int x, int y, int width, int height) {
            return super.isMouseInside(mouseX, mouseY, x, y, width, height)
                    && mouseX < x + width - CONTROL_COLUMN_WIDTH;
        }
    }

    /** Integer field counterpart of {@link LabelTooltipToggle}. */
    private static final class LabelTooltipIntField extends IntegerListEntry {

        private LabelTooltipIntField(Component name, int value, int fallback, int min, int max,
                                     Consumer<Integer> setter, Component[] tooltip) {
            super(name, value, RESET_KEY, () -> fallback, setter, () -> Optional.of(tooltip));
            setMinimum(min);
            setMaximum(max);
        }

        @Override
        public boolean isMouseInside(int mouseX, int mouseY, int x, int y, int width, int height) {
            return super.isMouseInside(mouseX, mouseY, x, y, width, height)
                    && mouseX < x + width - CONTROL_COLUMN_WIDTH;
        }
    }
}
