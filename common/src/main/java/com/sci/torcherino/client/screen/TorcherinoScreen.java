/*
 * 本文件：加速火把的可视化编辑界面（仅客户端加载）。
 * 说明：面板贴图为 assets/torcherino/textures/gui/torcherino.png（245x123），四条滑条（速度 / X / Z / Y）
 *      加一个红石模式按钮；关闭界面时把数值回传服务端。滑条手感由 gui.smoothSlider 决定：
 *      false（默认）= 每档吸附，true = 连续拖动、松手取整。所有文字自绘且不带阴影，避免叠加发虚。
 */
package com.sci.torcherino.client.screen;

import com.sci.torcherino.TorcherinoConfig;
import com.sci.torcherino.blocks.tiles.TileTorcherino;
import com.sci.torcherino.platform.Services;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/**
 * The Torcherino editor.
 *
 * <p>Deliberately a plain {@link Screen}: no container menu is involved, the block entity
 * stays where it is and the values travel back through one small packet when the screen
 * closes.</p>
 */
public class TorcherinoScreen extends Screen {

    private static final ResourceLocation PANEL = new ResourceLocation("torcherino", "textures/gui/torcherino.png");
    private static final int PANEL_WIDTH = 245;
    private static final int PANEL_HEIGHT = 123;
    private static final int SLIDER_LEFT = 8;
    private static final int SLIDER_WIDTH = 205;
    private static final int SLIDER_HEIGHT = 20;
    private static final int FIRST_ROW = 20;
    private static final int ROW_STEP = 25;
    private static final int MODE_BUTTON_LEFT = 217;
    private static final int BUTTON_HEIGHT = 20;
    /** Panel plus the "done" button row underneath it. */
    private static final int TOTAL_HEIGHT = PANEL_HEIGHT + 4 + BUTTON_HEIGHT;

    private final BlockPos pos;
    /** Translation key of the block this editor belongs to, e.g. a compressed Lanterino. */
    private final String titleKey;
    /** Tier factor (1 / 9 / 81 / 729) of the block, used for the percentage label. */
    private final int tierMultiplier;

    private int xRange;
    private int zRange;
    private int yRange;
    private int speed;
    private int redstoneMode;

    private int left;
    private int top;

    public TorcherinoScreen(BlockPos pos, String titleKey, int xRange, int zRange, int yRange, int speed,
                            int redstoneMode, int tierMultiplier) {
        super(Component.translatable(titleKey));
        this.pos = pos;
        this.titleKey = titleKey;
        this.xRange = xRange;
        this.zRange = zRange;
        this.yRange = yRange;
        this.speed = speed;
        this.redstoneMode = redstoneMode;
        this.tierMultiplier = tierMultiplier;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Speed level times the tier factor - the same number the action bar prints. */
    private Component speedLabel() {
        return Component.translatable("gui.torcherino.speed", this.speed * this.tierMultiplier * 100 + "%");
    }

    private static Component rangeLabel(String axis, int range) {
        return Component.translatable("gui.torcherino.range", axis, range * 2 + 1);
    }

    private Component redstoneModeName() {
        return Component.translatable(switch (this.redstoneMode) {
            case TileTorcherino.REDSTONE_INVERTED -> "gui.torcherino.redstone.inverted";
            case TileTorcherino.REDSTONE_IGNORED -> "gui.torcherino.redstone.ignored";
            case TileTorcherino.REDSTONE_OFF -> "gui.torcherino.redstone.off";
            default -> "gui.torcherino.redstone.normal";
        });
    }

    private Component redstoneButtonLabel() {
        final String name = this.redstoneModeName().getString();
        // The notch in the panel only fits one character; the full name lives in the tooltip.
        return Component.literal(name.isEmpty() ? "?" : name.substring(0, 1));
    }

    @Override
    protected void init() {
        this.left = (this.width - PANEL_WIDTH) / 2;
        this.top = (this.height - TOTAL_HEIGHT) / 2;
        final boolean smooth = TorcherinoConfig.smoothSlider;

        this.addRenderableWidget(new ValueSlider(this.left + SLIDER_LEFT, this.top + FIRST_ROW, SLIDER_WIDTH,
                TileTorcherino.MAX_SPEED, smooth, this.speed, value -> this.speed = value, value -> this.speedLabel()));
        this.addRenderableWidget(new ValueSlider(this.left + SLIDER_LEFT, this.top + FIRST_ROW + ROW_STEP,
                SLIDER_WIDTH, TileTorcherino.MAX_XZ_RANGE, smooth, this.xRange, value -> this.xRange = value,
                value -> rangeLabel("X", value)));
        this.addRenderableWidget(new ValueSlider(this.left + SLIDER_LEFT, this.top + FIRST_ROW + ROW_STEP * 2,
                SLIDER_WIDTH, TileTorcherino.MAX_XZ_RANGE, smooth, this.zRange, value -> this.zRange = value,
                value -> rangeLabel("Z", value)));
        this.addRenderableWidget(new ValueSlider(this.left + SLIDER_LEFT, this.top + FIRST_ROW + ROW_STEP * 3,
                SLIDER_WIDTH, TileTorcherino.MAX_Y_RANGE, smooth, this.yRange, value -> this.yRange = value,
                value -> rangeLabel("Y", value)));

        this.addRenderableWidget(Button.builder(this.redstoneButtonLabel(), button -> {
            this.redstoneMode = (this.redstoneMode + 1) % TileTorcherino.REDSTONE_MODES;
            button.setMessage(this.redstoneButtonLabel());
        }).bounds(this.left + MODE_BUTTON_LEFT, this.top + FIRST_ROW, 20, SLIDER_HEIGHT)
                .tooltip(Tooltip.create(Component.translatable("gui.torcherino.redstone", this.redstoneModeName())))
                .build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .bounds(this.left, this.top + PANEL_HEIGHT + 4, PANEL_WIDTH, BUTTON_HEIGHT).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.blit(PANEL, this.left, this.top, 0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        // Own text rendering (no drop shadow): the vanilla shadow turns into a visible
        // double image on top of the dark slider track.
        final Component title = Component.translatable(this.titleKey);
        graphics.drawString(this.font, title, this.left + PANEL_WIDTH / 2 - this.font.width(title) / 2,
                this.top + 7, 0x404040, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        Services.PLATFORM.sendTorcherinoValues(this.pos, this.xRange, this.zRange, this.yRange,
                this.speed, this.redstoneMode);
        super.onClose();
    }

    /**
     * A slider over a fixed number of whole steps.
     *
     * <p>In "snap" mode the handle is pulled onto the nearest step as soon as the value is
     * applied. In smooth mode the handle keeps the dragged position and only the reported
     * value is rounded. Drawing is done here instead of by the vanilla implementation so
     * the label can be rendered without a drop shadow.</p>
     */
    public static class ValueSlider extends AbstractSliderButton {

        private final int steps;
        private final double stepSize;
        private final boolean smooth;
        private final IntConsumer onChange;
        private final IntFunction<Component> labeller;
        private int current;

        public ValueSlider(int x, int y, int width, int steps, boolean smooth, int initial,
                           IntConsumer onChange, IntFunction<Component> labeller) {
            super(x, y, width, SLIDER_HEIGHT, Component.empty(), steps == 0 ? 0.0 : (double) initial / steps);
            this.steps = steps;
            this.stepSize = steps == 0 ? 0.0 : 1.0 / steps;
            this.smooth = smooth;
            this.onChange = onChange;
            this.labeller = labeller;
            this.current = Mth.clamp(initial, 0, steps);
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(this.labeller.apply(this.current));
        }

        @Override
        protected void applyValue() {
            final int rounded = (int) Math.round(this.value * this.steps);
            if (rounded != this.current) {
                this.current = rounded;
                this.onChange.accept(rounded);
            }
            if (!this.smooth) {
                this.value = this.current * this.stepSize;
            }
            this.updateMessage();
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            final int x = this.getX();
            final int y = this.getY();
            final int width = this.getWidth();
            final int height = this.getHeight();

            graphics.fill(x, y, x + width, y + height, 0xFF000000);
            graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1,
                    this.isHoveredOrFocused() ? 0xFF5B5B5B : 0xFF4C4C4C);

            final int handleX = x + 1 + (int) Math.round(this.value * (width - 10));
            graphics.fill(handleX, y + 1, handleX + 8, y + height - 1, 0xFFCFCFCF);
            graphics.fill(handleX, y + 1, handleX + 1, y + height - 1, 0xFFFFFFFF);

            final var font = Minecraft.getInstance().font;
            final Component message = this.getMessage();
            graphics.drawString(font, message, x + (width - font.width(message)) / 2,
                    y + (height - 8) / 2, 0xFFFFFF, false);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT) {
                final int next = Mth.clamp(this.current + (keyCode == GLFW.GLFW_KEY_LEFT ? -1 : 1), 0, this.steps);
                if (next != this.current) {
                    this.current = next;
                    this.value = next * this.stepSize;
                    this.onChange.accept(next);
                    this.updateMessage();
                }
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }
}
