/*
 * 本文件：模组配置界面（仅客户端加载）。
 * 说明：分两段显示 —— 上面「服务端配置」（配方开关，关闭界面时写回 torcherino-server.toml，需重启或 /reload；
 *      多人游戏里若无权限，改的其实是本机文件，界面下方有提示），下面「客户端配置」（界面 / 倍率自由调节 / 入世提示）。
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
 * The configuration screen.
 *
 * <p>Two sections: the recipe switches belong to the server file and are saved back into it
 * (on a server without the needed permission the local file is what actually changes, which
 * the hint under the section says), and the three client preferences below them.</p>
 */
public class TorcherinoConfigScreen extends Screen {

    private static final int WIDTH = 240;
    private static final int ROW = 20;
    private static final int HEADER = 14;
    private static final int HINT = 10;
    private static final int TITLE = 14;
    private static final int SERVER_TOGGLES = 4;
    private static final int SERVER_HINTS = 3;
    private static final int CLIENT_TOGGLES = 3;
    private static final int PADDING = 8;
    private static final int TOTAL = TITLE + HEADER * 2 + HINT * SERVER_HINTS
            + ROW * (SERVER_TOGGLES + CLIENT_TOGGLES + 1) + PADDING;

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
        this.top = Math.max(4, (this.height - TOTAL) / 2);

        // Server section: the recipe switches, saved into torcherino-server.toml.
        int y = this.top + TITLE + HEADER;
        y = this.addToggle(y, "overPoweredRecipe", () -> TorcherinoConfig.overPoweredRecipe,
                value -> TorcherinoConfig.overPoweredRecipe = value);
        y = this.addToggle(y, "compressedTorcherino", () -> TorcherinoConfig.compressedTorcherino,
                value -> TorcherinoConfig.compressedTorcherino = value);
        y = this.addToggle(y, "doubleCompressedTorcherino", () -> TorcherinoConfig.doubleCompressedTorcherino,
                value -> TorcherinoConfig.doubleCompressedTorcherino = value);
        y = this.addToggle(y, "tripleCompressedTorcherino", () -> TorcherinoConfig.tripleCompressedTorcherino,
                value -> TorcherinoConfig.tripleCompressedTorcherino = value);

        // Three hint lines live in this gap, see render().
        y += HINT * SERVER_HINTS + HEADER;

        // Client section: local preferences only.
        y = this.addToggle(y, "useGui", () -> TorcherinoConfig.useGui,
                value -> TorcherinoConfig.useGui = value);
        y = this.addToggle(y, "freeSpeedMultiplier", () -> TorcherinoConfig.freeSpeedMultiplier,
                value -> TorcherinoConfig.freeSpeedMultiplier = value);
        y = this.addToggle(y, "joinNotice", () -> TorcherinoConfig.joinNotice,
                value -> TorcherinoConfig.joinNotice = value);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .bounds(this.left, y + 4, WIDTH, 20).build());
    }

    private int addToggle(int y, String key, BooleanSupplier getter, Consumer<Boolean> setter) {
        this.addRenderableWidget(Button.builder(label(key, getter.getAsBoolean()), button -> {
            setter.accept(!getter.getAsBoolean());
            button.setMessage(label(key, getter.getAsBoolean()));
        }).bounds(this.left, y, WIDTH, ROW - 2).build());
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
        graphics.drawCenteredString(this.font, this.title, this.width / 2, this.top, 0xFFFFFF);

        int y = this.top + TITLE;
        drawHeader(graphics, "gui.torcherino.config.server", y);
        y += HEADER + ROW * SERVER_TOGGLES;
        graphics.drawCenteredString(this.font, Component.translatable("gui.torcherino.config.serverFile"),
                this.width / 2, y, 0xA0A0A0);
        graphics.drawCenteredString(this.font, Component.translatable("gui.torcherino.config.serverNote"),
                this.width / 2, y + HINT, 0xA0A0A0);
        graphics.drawCenteredString(this.font, Component.translatable("gui.torcherino.config.serverReload"),
                this.width / 2, y + HINT * 2, 0xA0A0A0);

        drawHeader(graphics, "gui.torcherino.config.client", y + HINT * SERVER_HINTS);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawHeader(GuiGraphics graphics, String key, int y) {
        graphics.drawCenteredString(this.font, Component.translatable(key), this.width / 2, y, 0xFFD080);
    }

    @Override
    public void onClose() {
        // Both halves are edited here: the switches go back to the server file (or to the
        // Forge spec), the three preferences to the client file.
        TorcherinoConfig.saveServer();
        TorcherinoConfig.saveClient();
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
