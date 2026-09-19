/*
 * 本文件：模组开关配置界面（仅客户端加载）。
 * 说明：把 config/torcherino.toml 里的 7 个布尔开关做成按钮，关闭时写回文件；文案全部走语言文件（13 语种）。
 *      它是给 Forge/NeoForge 的 Configured、以及 Fabric 的 Mod Menu 读取的「配置」入口；未安装前置时不影响使用。
 */
package com.sci.torcherino.client.screen;

import com.sci.torcherino.TorcherinoConfig;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Plain toggle list over the boolean switches of the configuration file.
 *
 * <p>Every label is translatable ({@code gui.torcherino.config.*}), so the screen follows
 * the player's language instead of showing raw key names.</p>
 */
public class TorcherinoConfigScreen extends Screen {

    private static final int WIDTH = 220;
    private static final int ROW = 22;

    private final Screen parent;

    public TorcherinoConfigScreen(Screen parent) {
        super(Component.translatable("gui.torcherino.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        final int left = (this.width - WIDTH) / 2;
        int y = (this.height - ROW * 8) / 2;

        y = this.addToggle(left, y, "useGui", () -> TorcherinoConfig.useGui, value -> TorcherinoConfig.useGui = value);
        y = this.addToggle(left, y, "smoothSlider", () -> TorcherinoConfig.smoothSlider,
                value -> TorcherinoConfig.smoothSlider = value);
        y = this.addToggle(left, y, "logPlacement", () -> TorcherinoConfig.logPlacement,
                value -> TorcherinoConfig.logPlacement = value);
        y = this.addToggle(left, y, "overPoweredRecipe", () -> TorcherinoConfig.overPoweredRecipe,
                value -> TorcherinoConfig.overPoweredRecipe = value);
        y = this.addToggle(left, y, "compressedTorcherino", () -> TorcherinoConfig.compressedTorcherino,
                value -> TorcherinoConfig.compressedTorcherino = value);
        y = this.addToggle(left, y, "doubleCompressedTorcherino", () -> TorcherinoConfig.doubleCompressedTorcherino,
                value -> TorcherinoConfig.doubleCompressedTorcherino = value);
        y = this.addToggle(left, y, "tripleCompressedTorcherino", () -> TorcherinoConfig.tripleCompressedTorcherino,
                value -> TorcherinoConfig.tripleCompressedTorcherino = value);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .bounds(left, y + 10, WIDTH, 20).build());
    }

    private int addToggle(int left, int y, String key, BooleanSupplier getter, Consumer<Boolean> setter) {
        this.addRenderableWidget(Button.builder(label(key, getter.getAsBoolean()), button -> {
            setter.accept(!getter.getAsBoolean());
            button.setMessage(label(key, getter.getAsBoolean()));
        }).bounds(left, y, WIDTH, 20).build());
        return y + ROW;
    }

    private static Component label(String key, boolean value) {
        return Component.translatable("gui.torcherino.config.entry",
                Component.translatable("gui.torcherino.config." + key),
                Component.translatable(value ? "gui.torcherino.config.on" : "gui.torcherino.config.off"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - ROW * 5, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        TorcherinoConfig.save();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
