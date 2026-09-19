/*
 * 本文件：模组配置界面（仅客户端加载）。
 * 说明：只显示「客户端」设置（gui.useGui / gui.smoothSlider），关闭时写回 torcherino-client.toml；
 *      服务端的配方开关与黑名单在 config/torcherino-server.toml，界面里用两行提示说明位置与生效时机。
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
 * Toggle list over the client preferences.
 *
 * <p>Only client settings are shown. The recipe switches and the blacklist belong to the
 * server file and are read while the data pack loads, so a client side toggle could never
 * affect them - that is why they are not buttons here, only a hint.</p>
 */
public class TorcherinoConfigScreen extends Screen {

    private static final int WIDTH = 240;
    private static final int ROW = 22;
    private static final int TOGGLES = 2;

    private final Screen parent;
    private int left;
    private int top;

    public TorcherinoConfigScreen(Screen parent) {
        super(Component.translatable("gui.torcherino.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.left = (this.width - WIDTH) / 2;
        this.top = (this.height - ROW * (TOGGLES + 1)) / 2 - 12;

        int y = this.top;
        y = this.addToggle(y, "useGui", () -> TorcherinoConfig.useGui, value -> TorcherinoConfig.useGui = value);
        y = this.addToggle(y, "smoothSlider", () -> TorcherinoConfig.smoothSlider,
                value -> TorcherinoConfig.smoothSlider = value);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .bounds(this.left, y + 10, WIDTH, 20).build());
    }

    private int addToggle(int y, String key, BooleanSupplier getter, Consumer<Boolean> setter) {
        this.addRenderableWidget(Button.builder(label(key, getter.getAsBoolean()), button -> {
            setter.accept(!getter.getAsBoolean());
            button.setMessage(label(key, getter.getAsBoolean()));
        }).bounds(this.left, y, WIDTH, 20).build());
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
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.top - 26, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);

        // Two hint lines about the server file, drawn above the buttons.
        final int hintY = this.top + ROW * TOGGLES + 2;
        graphics.drawCenteredString(this.font, Component.translatable("gui.torcherino.config.serverFile"),
                this.width / 2, hintY, 0xA0A0A0);
        graphics.drawCenteredString(this.font, Component.translatable("gui.torcherino.config.serverReload"),
                this.width / 2, hintY + 11, 0xA0A0A0);
    }

    @Override
    public void onClose() {
        TorcherinoConfig.saveClient();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
