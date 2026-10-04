/*
 * 本文件：加速火把的可视化编辑界面（仅客户端加载）。
 * 说明：外观延续原版的浅灰界面 —— 面板用 assets/torcherino/textures/gui/torcherino_panel.png
 *      （以旧的 torcherino.png 为底，去掉右上角为红石按钮预留的凸块，成为完整矩形），以九宫格
 *      方式拉伸；滑条凹槽与手柄同样使用贴图；按钮直接使用原版 Button 控件。
 *      布局排在一条统一栅格上：标题 / 所有者行 / 四条等宽滑条 / 一行按钮；面板宽度只由按钮行
 *      （按"最多按钮 + 最长标签"计算）与滑条列决定，所以无论有没有所有者、红石模式是哪一个，
 *      面板尺寸都完全一样。
 *      滑条为自绘控件：手柄位置（连续）与数值（整档）分开保存 —— 自由倍率模式下手柄可在档位之间
 *      自由拖动、数值仍取整；否则手柄被吸附到最近整档。文字一律无阴影绘制，避免叠加发虚。
 */
package com.sci.torcherino.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/**
 * The Torcherino editor.
 *
 * <p>Deliberately a plain {@link Screen}: no container menu is involved, the block entity
 * stays where it is and the values travel back through one small packet when the screen
 * closes.</p>
 *
 * <p>Look and feel follow the vanilla light GUI: the panel, the slider track and the slider
 * handle are textures (see {@code textures/gui}), the buttons are ordinary {@link Button}s.
 * The panel is a nine slice, so one 220x123 artwork covers every language: its corners stay
 * put while the middle is stretched to the size the text needs.</p>
 */
public class TorcherinoScreen extends Screen {

    // ---- textures ------------------------------------------------------------

    /** Panel artwork: the classic light grey frame, with the old power-button notch removed. */
    private static final ResourceLocation PANEL_TEXTURE =
            new ResourceLocation("torcherino", "textures/gui/torcherino_panel.png");
    /** Recessed track of a slider. */
    private static final ResourceLocation TRACK_TEXTURE =
            new ResourceLocation("torcherino", "textures/gui/torcherino_slider_track.png");
    /** Raised handle of a slider. */
    private static final ResourceLocation HANDLE_TEXTURE =
            new ResourceLocation("torcherino", "textures/gui/torcherino_slider_handle.png");

    /** Nine slice: 4 px corners and edges, the 212x115 centre is stretched. */
    private static final int PANEL_SLICE = 4;
    /** The artwork covers only this part of the PNG file. */
    private static final int PANEL_SOURCE_WIDTH = 220;
    private static final int PANEL_SOURCE_HEIGHT = 123;
    private static final int PANEL_FILE_WIDTH = 256;
    private static final int PANEL_FILE_HEIGHT = 256;

    /** The track texture is cut into a 2 px left cap, a stretched middle and a 2 px right cap. */
    private static final int TRACK_CAP = 2;
    private static final int TRACK_TEXTURE_WIDTH = 32;
    private static final int TRACK_TEXTURE_HEIGHT = 16;
    /** The handle texture is used 1:1. */
    private static final int HANDLE_WIDTH = 8;
    private static final int HANDLE_TEXTURE_WIDTH = 8;
    private static final int HANDLE_TEXTURE_HEIGHT = 16;

    // ---- layout --------------------------------------------------------------

    /** Margin between the panel edge and every piece of content. */
    private static final int PADDING = 9;
    /** Height of the title row. */
    private static final int TITLE_HEIGHT = 12;
    /** Height reserved for the owner line. */
    private static final int OWNER_HEIGHT = 10;
    private static final int SLIDER_HEIGHT = 16;
    /** Vertical distance between two slider rows. */
    private static final int SLIDER_STEP = 20;
    private static final int BUTTON_HEIGHT = 18;
    private static final int BUTTON_GAP = 4;
    /** The slider column never becomes narrower than this, whatever the buttons ask for. */
    private static final int MIN_SLIDER_WIDTH = 176;
    /** The panel never becomes narrower than this. */
    private static final int MIN_PANEL_WIDTH = 218;
    /**
     * Number of buttons the row is measured with: "Done", "Unclaim", the permission switch
     * and the redstone switch. The panel keeps this width even while only two of them are
     * shown, so a viewer who does not own the Torcherino sees exactly the same panel.
     */
    private static final int MAX_BUTTONS = 4;

    // ---- colours -------------------------------------------------------------

    private static final int COLOR_TITLE = 0x404040;
    private static final int COLOR_TEXT = 0x404040;
    private static final int COLOR_MUTED = 0x707070;
    private static final int COLOR_TRACK_TEXT = 0xFFFFFF;

    private final BlockPos pos;
    /** Translation key of the block this editor belongs to, e.g. a compressed Lanterino. */
    private final String titleKey;
    /** Tier factor (1 / 9 / 729) of the block, used for the percentage label. */
    private final int tierMultiplier;
    /** Display name of the owner, or an empty string while the Torcherino is unclaimed. */
    private String ownerDisplay;
    /** Whether players other than the owner may edit; only the owner may change it. */
    private boolean othersCanEdit;
    /** Whether the player looking at this screen is the recorded owner. */
    private boolean viewerIsOwner;

    private int xRange;
    private int zRange;
    private int yRange;
    /** Speed in hundredths of a level, so the free multiplier can hold any value. */
    private int speedScaled;
    private int redstoneMode;

    private int left;
    private int top;
    private int panelWidth;
    private int panelHeight;
    /** Absolute y of the owner line, kept for the render pass. */
    private int ownerRowY;

    public TorcherinoScreen(BlockPos pos, String titleKey, int xRange, int zRange, int yRange, int speedScaled,
                            int redstoneMode, int tierMultiplier, String ownerDisplay, boolean othersCanEdit,
                            boolean viewerIsOwner) {
        super(Component.translatable(titleKey));
        this.pos = pos;
        this.titleKey = titleKey;
        this.xRange = xRange;
        this.zRange = zRange;
        this.yRange = yRange;
        this.speedScaled = speedScaled;
        this.redstoneMode = redstoneMode;
        this.tierMultiplier = tierMultiplier;
        this.ownerDisplay = ownerDisplay == null ? "" : ownerDisplay;
        this.othersCanEdit = othersCanEdit;
        this.viewerIsOwner = viewerIsOwner;
    }

    /**
     * @return {@code true} when this editor may change anything: an unclaimed Torcherino is
     *         open to everyone, a claimed one only to its owner (unless the owner allowed
     *         everyone).
     */
    private boolean editable() {
        return this.viewerIsOwner || this.ownerDisplay.isEmpty() || this.othersCanEdit;
    }

    /** "Owner: name", or "No owner" while the Torcherino is unclaimed. */
    private Component ownerLabel() {
        return this.ownerDisplay.isEmpty()
                ? Component.translatable("gui.torcherino.owner.none")
                : Component.translatable("gui.torcherino.owner", this.ownerDisplay);
    }

    /** Current state of the "other players may edit" switch, as the button label shows it. */
    private Component permissionLabel() {
        return Component.translatable(this.othersCanEdit
                ? "gui.torcherino.owner.editable"
                : "gui.torcherino.owner.locked");
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

    private static Component redstoneModeName(int mode) {
        return Component.translatable(switch (mode) {
            case TileTorcherino.REDSTONE_INVERTED -> "gui.torcherino.redstone.inverted";
            case TileTorcherino.REDSTONE_IGNORED -> "gui.torcherino.redstone.ignored";
            case TileTorcherino.REDSTONE_OFF -> "gui.torcherino.redstone.off";
            default -> "gui.torcherino.redstone.normal";
        });
    }

    private static Component redstoneButtonLabel(int mode) {
        return Component.translatable("gui.torcherino.redstone", redstoneModeName(mode));
    }

    @Override
    protected void init() {
        final boolean editable = this.editable();
        // Speed: eight classic gears by default; with the free multiplier the slider offers
        // every hundredth of a level, i.e. any value from 0 % up to the tier maximum.
        final int speedSteps = TorcherinoConfig.freeSpeedMultiplier
                ? TileTorcherino.MAX_SPEED_SCALED : TileTorcherino.MAX_SPEED;
        final int speedFactor = TileTorcherino.MAX_SPEED_SCALED / speedSteps;

        // ---- the row is measured with every button it can ever hold ------------------
        // The two owner only buttons and every redstone mode name are included, so neither
        // the viewer's ownership nor the current redstone mode can change the panel size.
        int widestLabel = this.font.width(Component.translatable("gui.done"));
        widestLabel = Math.max(widestLabel,
                this.font.width(Component.translatable("gui.torcherino.owner.unclaim")));
        widestLabel = Math.max(widestLabel,
                this.font.width(Component.translatable("gui.torcherino.owner.editable")));
        widestLabel = Math.max(widestLabel,
                this.font.width(Component.translatable("gui.torcherino.owner.locked")));
        for (int mode = 0; mode < TileTorcherino.REDSTONE_MODES; mode++) {
            widestLabel = Math.max(widestLabel, this.font.width(redstoneButtonLabel(mode)));
        }
        final int naturalButtonWidth = widestLabel + 12;
        final int buttonRowWidth =
                naturalButtonWidth * MAX_BUTTONS + BUTTON_GAP * (MAX_BUTTONS - 1);

        // ---- panel size --------------------------------------------------------------
        final Component title = this.getTitle();
        final int widestText = Math.max(this.font.width(title), buttonRowWidth);
        this.panelWidth = Math.max(MIN_PANEL_WIDTH, Math.max(MIN_SLIDER_WIDTH, widestText) + PADDING * 2);
        this.panelWidth = Math.min(this.panelWidth, this.width - 16);
        final int sliderWidth = Math.max(40, this.panelWidth - PADDING * 2);
        // The narrowest button still has to hold its label; on a very small screen the row is
        // squeezed evenly instead of overflowing the panel.
        final int availableForButtons = sliderWidth - BUTTON_GAP * (MAX_BUTTONS - 1);
        final int buttonWidth = Math.max(20, Math.min(naturalButtonWidth, availableForButtons / MAX_BUTTONS));

        // ---- rows --------------------------------------------------------------------
        final int ownerRow = PADDING + TITLE_HEIGHT + 2;
        final int firstSliderRow = ownerRow + OWNER_HEIGHT + 6;
        final int buttonRow = firstSliderRow + 3 * SLIDER_STEP + SLIDER_HEIGHT + 8;
        this.panelHeight = buttonRow + BUTTON_HEIGHT + PADDING;
        this.left = (this.width - this.panelWidth) / 2;
        this.top = (this.height - this.panelHeight) / 2;
        this.ownerRowY = this.top + ownerRow;

        // ---- sliders -----------------------------------------------------------------
        final int sliderLeft = this.left + PADDING;
        final int sliderTop = this.top + firstSliderRow;
        final ValueSlider speedSlider = new ValueSlider(sliderLeft, sliderTop, sliderWidth,
                speedSteps, TorcherinoConfig.freeSpeedMultiplier, Math.round(this.speedScaled / (float) speedFactor),
                value -> this.speedScaled = value * speedFactor, value -> this.speedLabel());
        // The three ranges count whole blocks, so they always snap onto a step; a "3.5 block"
        // wide area would not mean anything.
        final ValueSlider xSlider = new ValueSlider(sliderLeft, sliderTop + SLIDER_STEP, sliderWidth,
                TileTorcherino.MAX_XZ_RANGE, false, this.xRange, value -> this.xRange = value,
                value -> rangeLabel("X", value));
        final ValueSlider zSlider = new ValueSlider(sliderLeft, sliderTop + SLIDER_STEP * 2, sliderWidth,
                TileTorcherino.MAX_XZ_RANGE, false, this.zRange, value -> this.zRange = value,
                value -> rangeLabel("Z", value));
        final ValueSlider ySlider = new ValueSlider(sliderLeft, sliderTop + SLIDER_STEP * 3, sliderWidth,
                TileTorcherino.MAX_Y_RANGE, false, this.yRange, value -> this.yRange = value,
                value -> rangeLabel("Y", value));

        speedSlider.active = editable;
        xSlider.active = editable;
        zSlider.active = editable;
        ySlider.active = editable;
        this.addRenderableWidget(speedSlider);
        this.addRenderableWidget(xSlider);
        this.addRenderableWidget(zSlider);
        this.addRenderableWidget(ySlider);

        // ---- button row ---------------------------------------------------------------
        final int buttonY = this.top + buttonRow;
        int x = this.left + PADDING;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
                .bounds(x, buttonY, buttonWidth, BUTTON_HEIGHT).build());
        x += buttonWidth + BUTTON_GAP;
        if (this.viewerIsOwner) {
            // Only the owner sees these two: everyone else just reads the owner line above.
            this.addRenderableWidget(Button.builder(Component.translatable("gui.torcherino.owner.unclaim"), button -> {
                this.ownerDisplay = "";
                this.viewerIsOwner = false;
                Services.PLATFORM.sendTorcherinoOwnerSettings(this.pos, true, true);
                // Rebuild so the owner only buttons disappear and the panel unlocks.
                this.rebuildWidgets();
            }).bounds(x, buttonY, buttonWidth, BUTTON_HEIGHT)
                    .tooltip(Tooltip.create(Component.translatable("gui.torcherino.owner.unclaim.tip")))
                    .build());
            x += buttonWidth + BUTTON_GAP;
            this.addRenderableWidget(Button.builder(this.permissionLabel(), button -> {
                this.othersCanEdit = !this.othersCanEdit;
                button.setMessage(this.permissionLabel());
                Services.PLATFORM.sendTorcherinoOwnerSettings(this.pos, this.othersCanEdit, false);
            }).bounds(x, buttonY, buttonWidth, BUTTON_HEIGHT)
                    .tooltip(Tooltip.create(Component.translatable("gui.torcherino.owner.permission.tip")))
                    .build());
            x += buttonWidth + BUTTON_GAP;
        }
        final Button redstoneButton = Button.builder(redstoneButtonLabel(this.redstoneMode), button -> {
            this.redstoneMode = (this.redstoneMode + 1) % TileTorcherino.REDSTONE_MODES;
            button.setMessage(redstoneButtonLabel(this.redstoneMode));
            // The tooltip is built once when the widget is created, so it has to be rebuilt
            // here as well - otherwise it keeps showing the mode the screen was opened with.
            button.setTooltip(Tooltip.create(redstoneButtonLabel(this.redstoneMode)));
        }).bounds(x, buttonY, buttonWidth, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(redstoneButtonLabel(this.redstoneMode)))
                .build();
        redstoneButton.active = editable;
        this.addRenderableWidget(redstoneButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        this.drawPanel(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        // Title and owner line last: nothing may be painted over them.
        final Component title = this.getTitle();
        graphics.drawString(this.font, title, this.left + (this.panelWidth - this.font.width(title)) / 2,
                this.top + PADDING, COLOR_TITLE, false);
        // The owner line can never bleed over the frame, however long the name is.
        graphics.enableScissor(this.left + 1, this.ownerRowY - 1, this.left + this.panelWidth - 1,
                this.ownerRowY + 9);
        graphics.drawString(this.font, this.ownerLabel(), this.left + PADDING, this.ownerRowY,
                this.ownerDisplay.isEmpty() ? COLOR_MUTED : COLOR_TEXT, false);
        graphics.disableScissor();
    }

    /**
     * Draws the panel as a nine slice of the artwork: four corners and four edges around a
     * stretched centre. Only the centre really changes size, so the frame stays crisp at any
     * panel size.
     */
    private void drawPanel(GuiGraphics graphics) {
        final int s = PANEL_SLICE;
        final int sourceWidth = PANEL_SOURCE_WIDTH;
        final int sourceHeight = PANEL_SOURCE_HEIGHT;
        final int middleWidth = sourceWidth - s * 2;
        final int middleHeight = sourceHeight - s * 2;
        final int x = this.left;
        final int y = this.top;
        final int width = this.panelWidth;
        final int height = this.panelHeight;

        panelPiece(graphics, x, y, s, s, 0, 0, s, s);
        panelPiece(graphics, x + width - s, y, s, s, sourceWidth - s, 0, s, s);
        panelPiece(graphics, x, y + height - s, s, s, 0, sourceHeight - s, s, s);
        panelPiece(graphics, x + width - s, y + height - s, s, s, sourceWidth - s, sourceHeight - s, s, s);

        panelPiece(graphics, x + s, y, width - s * 2, s, s, 0, middleWidth, s);
        panelPiece(graphics, x + s, y + height - s, width - s * 2, s, s, sourceHeight - s, middleWidth, s);
        panelPiece(graphics, x, y + s, s, height - s * 2, 0, s, s, middleHeight);
        panelPiece(graphics, x + width - s, y + s, s, height - s * 2, sourceWidth - s, s, s, middleHeight);

        panelPiece(graphics, x + s, y + s, width - s * 2, height - s * 2, s, s, middleWidth, middleHeight);
    }

    /** One piece of the panel: the source rectangle is stretched onto the target rectangle. */
    private static void panelPiece(GuiGraphics graphics, int x, int y, int width, int height,
                                   int u, int v, int uWidth, int vHeight) {
        graphics.blit(PANEL_TEXTURE, x, y, width, height, u, v, uWidth, vHeight,
                PANEL_FILE_WIDTH, PANEL_FILE_HEIGHT);
    }

    @Override
    public void onClose() {
        // Nothing was editable for this player, so there is nothing to send - and the server
        // would reject it anyway.
        if (this.editable()) {
            Services.PLATFORM.sendTorcherinoValues(this.pos, this.xRange, this.zRange, this.yRange,
                    this.speedScaled, this.redstoneMode);
        }
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

        /** Travel of the handle across the track, in pixels. */
        private int handleTravel() {
            return Math.max(1, this.getWidth() - 2 - HANDLE_WIDTH);
        }

        private void moveHandleTo(double mouseX) {
            // The centre of the handle follows the mouse, so grabbing it never makes it jump.
            final double offset = mouseX - (double) (this.getX() + 1) - HANDLE_WIDTH / 2.0;
            this.position = Mth.clamp(offset / this.handleTravel(), 0.0, 1.0);
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
            final boolean dimmed = !this.active;

            if (dimmed) {
                // Same texture, just darkened - the cheap way every vanilla widget does it.
                RenderSystem.setShaderColor(0.6F, 0.6F, 0.6F, 1.0F);
            }

            // Track: 2 px caps with a stretched middle, the texture is 16 px tall and used 1:1.
            graphics.blit(TRACK_TEXTURE, x, y, TRACK_CAP, height, 0, 0, TRACK_CAP,
                    TRACK_TEXTURE_HEIGHT, TRACK_TEXTURE_WIDTH, TRACK_TEXTURE_HEIGHT);
            graphics.blit(TRACK_TEXTURE, x + TRACK_CAP, y, width - TRACK_CAP * 2, height, TRACK_CAP, 0,
                    TRACK_TEXTURE_WIDTH - TRACK_CAP * 2, TRACK_TEXTURE_HEIGHT,
                    TRACK_TEXTURE_WIDTH, TRACK_TEXTURE_HEIGHT);
            graphics.blit(TRACK_TEXTURE, x + width - TRACK_CAP, y, TRACK_CAP, height,
                    TRACK_TEXTURE_WIDTH - TRACK_CAP, 0, TRACK_CAP, TRACK_TEXTURE_HEIGHT,
                    TRACK_TEXTURE_WIDTH, TRACK_TEXTURE_HEIGHT);

            // Handle: 1:1, centred on the current position.
            final int handleX = x + 1 + (int) Math.round(this.handlePosition() * this.handleTravel());
            graphics.blit(HANDLE_TEXTURE, handleX, y, HANDLE_WIDTH, height, 0, 0, HANDLE_TEXTURE_WIDTH,
                    HANDLE_TEXTURE_HEIGHT, HANDLE_TEXTURE_WIDTH, HANDLE_TEXTURE_HEIGHT);

            if (dimmed) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            } else if (this.isHoveredOrFocused()) {
                // A faint veil is all the feedback the textured track needs.
                graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0x14FFFFFF);
            }

            final var font = Minecraft.getInstance().font;
            final Component message = this.getMessage();
            graphics.drawString(font, message, x + (width - font.width(message)) / 2,
                    y + (height - 8) / 2, COLOR_TRACK_TEXT, false);
        }
    }
}
