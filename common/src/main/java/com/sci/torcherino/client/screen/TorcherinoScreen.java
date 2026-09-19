/*
 * 本文件：加速火把的可视化编辑界面（仅客户端加载）。
 * 说明：面板贴图为 assets/torcherino/textures/gui/torcherino.png（245x123），四条滑条（速度 / X / Z / Y）
 *      加一个红石模式按钮；关闭界面时把数值回传服务端。
 *      滑条为自绘控件：手柄位置（连续）与数值（整档）分开保存 —— gui.smoothSlider = true 时手柄可在档位之间
 *      自由拖动、数值仍取整；false 时手柄被吸附到最近整档。文字一律无阴影绘制，避免叠加发虚。
 */
package com.sci.torcherino.client.screen;

import com.sci.torcherino.TorcherinoConfig;
import com.sci.torcherino.blocks.tiles.TileTorcherino;
import com.sci.torcherino.platform.Services;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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
    /** Speed in hundredths of a level, so the free multiplier can hold any value. */
    private int speedScaled;
    private int redstoneMode;

    private int left;
    private int top;

    public TorcherinoScreen(BlockPos pos, String titleKey, int xRange, int zRange, int yRange, int speedScaled,
                            int redstoneMode, int tierMultiplier) {
        super(Component.translatable(titleKey));
        this.pos = pos;
        this.titleKey = titleKey;
        this.xRange = xRange;
        this.zRange = zRange;
        this.yRange = yRange;
        this.speedScaled = speedScaled;
        this.redstoneMode = redstoneMode;
        this.tierMultiplier = tierMultiplier;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Speed times the tier factor - the same number the action bar prints. */
    private Component speedLabel() {
        return Component.translatable("gui.torcherino.speed", this.speedScaled * this.tierMultiplier + "%");
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
        final boolean free = TorcherinoConfig.freeSpeedMultiplier;
        // Speed: eight classic gears by default; with the free multiplier the slider offers
        // every hundredth of a level, i.e. any value from 0 % up to the tier maximum.
        final int speedSteps = free ? TileTorcherino.MAX_SPEED_SCALED : TileTorcherino.MAX_SPEED;
        final int speedFactor = TileTorcherino.MAX_SPEED_SCALED / speedSteps;

        this.addRenderableWidget(new ValueSlider(this.left + SLIDER_LEFT, this.top + FIRST_ROW, SLIDER_WIDTH,
                speedSteps, free, Math.round(this.speedScaled / (float) speedFactor),
                value -> this.speedScaled = value * speedFactor, value -> this.speedLabel()));
        // The three ranges count whole blocks, so they always snap onto a step; a "3.5 block"
        // wide area would not mean anything.
        this.addRenderableWidget(new ValueSlider(this.left + SLIDER_LEFT, this.top + FIRST_ROW + ROW_STEP,
                SLIDER_WIDTH, TileTorcherino.MAX_XZ_RANGE, false, this.xRange, value -> this.xRange = value,
                value -> rangeLabel("X", value)));
        this.addRenderableWidget(new ValueSlider(this.left + SLIDER_LEFT, this.top + FIRST_ROW + ROW_STEP * 2,
                SLIDER_WIDTH, TileTorcherino.MAX_XZ_RANGE, false, this.zRange, value -> this.zRange = value,
                value -> rangeLabel("Z", value)));
        this.addRenderableWidget(new ValueSlider(this.left + SLIDER_LEFT, this.top + FIRST_ROW + ROW_STEP * 3,
                SLIDER_WIDTH, TileTorcherino.MAX_Y_RANGE, false, this.yRange, value -> this.yRange = value,
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
                this.speedScaled, this.redstoneMode);
        super.onClose();
    }

    /**
     * A slider whose handle position and reported value are kept apart.
     *
     * <p>The handle position is a raw {@code 0..1} fraction taken straight from the mouse,
     * while the value handed to {@code onChange} is always a whole step. In "continuous"
     * mode the handle is drawn at the raw fraction, so it can be parked anywhere inside a
     * step while the number stays rounded; otherwise the handle is drawn on the step it
     * snapped to. This is written by hand instead of using {@code AbstractSliderButton}
     * because that class derives its drawing position from the value, which makes the two
     * modes indistinguishable.</p>
     */
    public static class ValueSlider extends AbstractWidget {

        private final int steps;
        private final boolean continuous;
        private final IntConsumer onChange;
        private final IntFunction<Component> labeller;

        /** Raw handle position in {@code [0,1]}, only ever used for drawing. */
        private double position;
        /** The rounded value that is reported and shown in the label. */
        private int value;

        public ValueSlider(int x, int y, int width, int steps, boolean continuous, int initial,
                           IntConsumer onChange, IntFunction<Component> labeller) {
            super(x, y, width, SLIDER_HEIGHT, Component.empty());
            this.steps = steps;
            this.continuous = continuous;
            this.onChange = onChange;
            this.labeller = labeller;
            this.value = Mth.clamp(initial, 0, steps);
            this.position = this.steps == 0 ? 0.0 : (double) this.value / this.steps;
            this.updateLabel();
        }

        private void updateLabel() {
            this.setMessage(this.labeller.apply(this.value));
        }

        /** Where the handle is drawn: freely in continuous mode, on the step otherwise. */
        private double handlePosition() {
            if (this.continuous || this.steps == 0) {
                return this.position;
            }
            return (double) this.value / this.steps;
        }

        private void moveHandleTo(double mouseX) {
            this.position = Mth.clamp((mouseX - (double) (this.getX() + 4)) / (double) (this.getWidth() - 8), 0.0, 1.0);
            this.applyPosition();
        }

        private void applyPosition() {
            final int rounded = (int) Math.round(this.position * this.steps);
            if (rounded != this.value) {
                this.value = rounded;
                this.onChange.accept(rounded);
            }
            this.updateLabel();
        }

        private void step(int direction) {
            final int next = Mth.clamp(this.value + direction, 0, this.steps);
            if (next == this.value) {
                return;
            }
            this.value = next;
            this.position = this.steps == 0 ? 0.0 : (double) next / this.steps;
            this.onChange.accept(next);
            this.updateLabel();
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            this.moveHandleTo(mouseX);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narration) {
            this.defaultButtonNarrationText(narration);
        }

        @Override
        protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
            this.moveHandleTo(mouseX);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT) {
                this.step(keyCode == GLFW.GLFW_KEY_LEFT ? -1 : 1);
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
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

            final int handleX = x + 1 + (int) Math.round(this.handlePosition() * (width - 10));
            graphics.fill(handleX, y + 1, handleX + 8, y + height - 1, 0xFFCFCFCF);
            graphics.fill(handleX, y + 1, handleX + 1, y + height - 1, 0xFFFFFFFF);

            final var font = Minecraft.getInstance().font;
            final Component message = this.getMessage();
            graphics.drawString(font, message, x + (width - font.width(message)) / 2,
                    y + (height - 8) / 2, 0xFFFFFF, false);
        }
    }
}
