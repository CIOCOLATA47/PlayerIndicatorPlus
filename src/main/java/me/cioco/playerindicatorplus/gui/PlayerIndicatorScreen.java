package me.cioco.playerindicatorplus.gui;

import me.cioco.playerindicatorplus.Main;
import me.cioco.playerindicatorplus.config.PlayerIndicatorConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PlayerIndicatorScreen extends Screen {

    private static final int SPACING_Y   = 24;
    private static final int SECTION_GAP = 35;
    private static final int TITLE_HEIGHT = 20;

    private final Screen parent;
    private final PlayerIndicatorConfig config = new PlayerIndicatorConfig();
    private final List<AbstractWidget> scrollableWidgets = new ArrayList<>();

    private int scrollOffset = 0;
    private int maxScroll;
    private int contentHeight;
    private Button doneButton;
    private Button globalToggleButton;

    private final int[] sectionY    = new int[4];
    private final int[] sectionRows = new int[4];

    public PlayerIndicatorScreen(Screen parent) {
        super(Component.literal("PlayerIndicatorPlus Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        this.scrollableWidgets.clear();

        int centerX  = width / 2;
        int leftCol  = centerX - 175;
        int rightCol = centerX + 5;
        int y = 70;

        sectionY[0] = y; sectionRows[0] = 5;
        addToggleButton(leftCol,  y, "Show Health",     "Numeric health display.",        PlayerIndicatorConfig.showHealthNumbers,    v -> PlayerIndicatorConfig.showHealthNumbers = v);
        addToggleButton(rightCol, y, "Show Invisibles", "Render for invisible players.",  PlayerIndicatorConfig.showInvisiblePlayers, v -> PlayerIndicatorConfig.showInvisiblePlayers = v);
        y += SPACING_Y;
        addSlider(leftCol,  y, 170, "Size",       PlayerIndicatorConfig.healthTextSize,       0.01f, 0.05f, v -> PlayerIndicatorConfig.healthTextSize = v);
        addSlider(rightCol, y, 170, "Range",      PlayerIndicatorConfig.healthVisibilityRange, 16f,  128f,  v -> PlayerIndicatorConfig.healthVisibilityRange = v);
        y += SPACING_Y;
        addSlider(leftCol,  y, 170, "Height",     PlayerIndicatorConfig.heightAboveHead,       0.2f,  2.0f, v -> PlayerIndicatorConfig.heightAboveHead = v);
        addSlider(rightCol, y, 170, "Hue",        PlayerIndicatorConfig.healthTextHue,         0f,  360f,   v -> PlayerIndicatorConfig.healthTextHue = v);
        y += SPACING_Y;
        addSlider(rightCol, y, 170, "Brightness", PlayerIndicatorConfig.healthTextBrightness,  0f,   1f,    v -> PlayerIndicatorConfig.healthTextBrightness = v);
        y += SPACING_Y;
        addToggleButton(leftCol, y, "Tab Health", "Use health value from tab list (server-side).", PlayerIndicatorConfig.useTabListHealth, v -> PlayerIndicatorConfig.useTabListHealth = v);
        y += SPACING_Y + SECTION_GAP;

        sectionY[1] = y; sectionRows[1] = 3;
        addToggleButton(leftCol,  y, "Show Percent", "Armor durability percentage.", PlayerIndicatorConfig.showArmorPercentages, v -> PlayerIndicatorConfig.showArmorPercentages = v);
        addToggleButton(rightCol, y, "Show Text",    "Armor type names.",            PlayerIndicatorConfig.showArmorText,        v -> PlayerIndicatorConfig.showArmorText = v);
        y += SPACING_Y;
        addSlider(leftCol,  y, 170, "Height",    PlayerIndicatorConfig.armorheightAboveHead, 0.2f, 2.0f,  v -> PlayerIndicatorConfig.armorheightAboveHead = v);
        addSlider(rightCol, y, 170, "Text Size", PlayerIndicatorConfig.armorTextSize,        0.01f, 0.05f, v -> PlayerIndicatorConfig.armorTextSize = v);
        y += SPACING_Y;
        addSlider(leftCol,  y, 170, "Hue",        PlayerIndicatorConfig.armorTextHue,        0f, 360f, v -> PlayerIndicatorConfig.armorTextHue = v);
        addSlider(rightCol, y, 170, "Saturation", PlayerIndicatorConfig.armorTextSaturation, 0f, 1f,   v -> PlayerIndicatorConfig.armorTextSaturation = v);
        y += SPACING_Y + SECTION_GAP;

        sectionY[2] = y; sectionRows[2] = 2;
        addToggleButton(leftCol,  y, "Main Hand", "Show held item.",    PlayerIndicatorConfig.showMainHand, v -> PlayerIndicatorConfig.showMainHand = v);
        addToggleButton(rightCol, y, "Off Hand",  "Show offhand item.", PlayerIndicatorConfig.showOffHand,  v -> PlayerIndicatorConfig.showOffHand = v);
        y += SPACING_Y;
        addSlider(leftCol,  y, 170, "Height",    PlayerIndicatorConfig.equipmentHeightAboveHead, 0.2f, 2.0f,  v -> PlayerIndicatorConfig.equipmentHeightAboveHead = v);
        addSlider(rightCol, y, 170, "Text Size", PlayerIndicatorConfig.equipmentTextSize,        0.01f, 0.05f, v -> PlayerIndicatorConfig.equipmentTextSize = v);
        y += SPACING_Y + SECTION_GAP;

        sectionY[3] = y; sectionRows[3] = 4;
        addToggleButton(leftCol,  y, "Show Ping",     "Latency display.",     PlayerIndicatorConfig.showPing,     v -> PlayerIndicatorConfig.showPing = v);
        addToggleButton(rightCol, y, "Show Distance", "Distance in blocks.",  PlayerIndicatorConfig.showDistance, v -> PlayerIndicatorConfig.showDistance = v);
        y += SPACING_Y;
        addSlider(leftCol,  y, 170, "Height",    PlayerIndicatorConfig.infoHeightAboveHead, 0.1f, 2.0f,  v -> PlayerIndicatorConfig.infoHeightAboveHead = v);
        addSlider(rightCol, y, 170, "Hue",       PlayerIndicatorConfig.infoTextHue,         0f,  360f,   v -> PlayerIndicatorConfig.infoTextHue = v);
        y += SPACING_Y;
        addSlider(leftCol,  y, 170, "Text Size",  PlayerIndicatorConfig.infoTextSize,        0.01f, 0.05f, v -> PlayerIndicatorConfig.infoTextSize = v);
        addSlider(rightCol, y, 170, "Saturation", PlayerIndicatorConfig.infoTextSaturation,  0f,   1f,    v -> PlayerIndicatorConfig.infoTextSaturation = v);
        y += SPACING_Y;
        addSlider(rightCol, y, 170, "Brightness", PlayerIndicatorConfig.infoTextBrightness,  0f,   1f,    v -> PlayerIndicatorConfig.infoTextBrightness = v);
        y += SPACING_Y + SECTION_GAP;

        contentHeight = y + 40;
        maxScroll = Math.max(0, contentHeight - (height - 90));
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        int centerBtnX = centerX - 100;

        globalToggleButton = Button.builder(getGlobalToggleText(), b -> {
            PlayerIndicatorConfig.toggled = !PlayerIndicatorConfig.toggled;
            b.setMessage(getGlobalToggleText());
        }).bounds(centerBtnX, height - 60, 200, 20).build();
        addRenderableWidget(globalToggleButton);

        doneButton = Button.builder(
                Component.literal("SAVE & EXIT").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                b -> this.onClose()
        ).bounds(centerBtnX, height - 30, 200, 20).build();
        addRenderableWidget(doneButton);

        for (AbstractWidget widget : scrollableWidgets) {
            widget.setY(widget.getY() - scrollOffset);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        ctx.fillGradient(0, 0, width, height, 0xC0101010, 0xD0101010);

        int cx     = width / 2;
        int panelW = 360;
        int panelX = cx - (panelW / 2);

        ctx.centeredText(font,
                Component.literal("PLAYER INDICATOR+").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
                cx, 15, 0xFFFFFFFF);

        ctx.enableScissor(0, 40, width, height - 70);

        String[] titles = {"Health", "Armor", "Equipment", "Connection & Info"};
        for (int i = 0; i < sectionY.length; i++) {
            renderSectionGroup(ctx, panelX, sectionY[i] - scrollOffset, panelW, sectionRows[i], titles[i]);
        }

        for (AbstractWidget widget : scrollableWidgets) {
            if (widget.getY() + widget.getHeight() > 40 && widget.getY() < height - 70) {
                widget.visible = true;
                widget.extractRenderState(ctx, mouseX, mouseY, delta);
            } else {
                widget.visible = false;
            }
        }

        ctx.disableScissor();

        globalToggleButton.extractRenderState(ctx, mouseX, mouseY, delta);
        doneButton.extractRenderState(ctx, mouseX, mouseY, delta);

        drawScrollBar(ctx);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (maxScroll > 0) {
            int oldOffset = scrollOffset;
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - (verticalAmount * 25)));
            int diff = oldOffset - scrollOffset;
            for (AbstractWidget widget : scrollableWidgets) {
                widget.setY(widget.getY() + diff);
            }
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void drawScrollBar(GuiGraphicsExtractor ctx) {
        if (maxScroll <= 0) return;
        int trackX      = width - 6;
        int trackY      = 40;
        int trackHeight = height - 110;
        int thumbHeight = Math.max(20, (int) ((float) trackHeight * (trackHeight / (float) contentHeight)));
        int thumbY      = trackY + (int) ((trackHeight - thumbHeight) * ((float) scrollOffset / maxScroll));
        ctx.fill(trackX, trackY, width - 2, trackY + trackHeight, 0x40000000);
        ctx.fill(trackX, thumbY, width - 2, thumbY + thumbHeight, 0xFFFFAA00);
    }

    private void addToggleButton(int x, int y, String label, String desc, boolean val, Consumer<Boolean> action) {
        Button btn = Button.builder(getToggleText(label, val), b -> {
            boolean currentlyOn = b.getMessage().getString().contains("ON");
            action.accept(!currentlyOn);
            b.setMessage(getToggleText(label, !currentlyOn));
        }).bounds(x, y, 170, 20).tooltip(Tooltip.create(Component.literal("§e" + desc))).build();
        scrollableWidgets.add(btn);
        addRenderableWidget(btn);
    }

    private void addSlider(int x, int y, int w, String label, float cur, float min, float max, Consumer<Float> action) {
        CompoundSlider compound = new CompoundSlider(x, y, w, 20, label, cur, min, max, action);
        scrollableWidgets.add(compound.slider);
        scrollableWidgets.add(compound.textField);
        addRenderableWidget(compound.slider);
        addRenderableWidget(compound.textField);
    }

    private void renderSectionGroup(GuiGraphicsExtractor ctx, int x, int y, int w, int rows, String title) {
        int contentH = rows * SPACING_Y;
        drawStyledPanel(ctx, x, y - TITLE_HEIGHT - 5, w, contentH + TITLE_HEIGHT + 10);
        ctx.text(font, "§6§l» §f" + title, x + 8, y - TITLE_HEIGHT + 1, 0xFFFFFFFF);
        ctx.fill(x + 5, y - 6, x + w - 5, y - 5, 0x80FFAA00);
    }

    private void drawStyledPanel(GuiGraphicsExtractor ctx, int x, int y, int width, int height) {
        ctx.fill(x, y, x + width, y + height, 0x90000000);
        ctx.fill(x, y, x + 2, y + height, 0xFFFFAA00);
        ctx.fill(x + width - 2, y, x + width, y + height, 0xFFFFAA00);
    }

    private Component getToggleText(String label, boolean value) {
        return Component.literal(label + ": ").append(
                value ? Component.literal("ON").withStyle(ChatFormatting.GREEN)
                        : Component.literal("OFF").withStyle(ChatFormatting.RED)
        );
    }

    private Component getGlobalToggleText() {
        return Component.literal("PlayerIndicator: ").append(
                PlayerIndicatorConfig.toggled ? Component.literal("Enabled").withStyle(ChatFormatting.GREEN)
                        : Component.literal("Disabled").withStyle(ChatFormatting.RED)
        );
    }

    public void refreshGlobalToggle() {
        if (globalToggleButton != null) {
            globalToggleButton.setMessage(getGlobalToggleText());
        }
    }

    @Override
    public void onClose() {
        config.saveConfiguration();
        if (minecraft != null) minecraft.setScreen(parent);
    }

    private class CompoundSlider {
        public final CustomSlider slider;
        public final EditBox textField;
        private boolean isUpdating = false;

        public CompoundSlider(int x, int y, int w, int h, String label, float cur, float min, float max, Consumer<Float> action) {
            int textWidth = 45;
            this.textField = new EditBox(font, x + w - textWidth, y, textWidth, h, Component.empty());
            this.textField.setValue(String.format("%.2f", cur));

            this.slider = new CustomSlider(x, y, w - textWidth - 2, h, label, cur, min, max, val -> {
                if (!isUpdating) {
                    isUpdating = true;
                    textField.setValue(String.format("%.2f", val));
                    action.accept(val);
                    isUpdating = false;
                }
            });

            this.textField.setResponder(text -> {
                if (isUpdating) return;
                try {
                    float val = Float.parseFloat(text);
                    isUpdating = true;
                    double sliderPos = (val - min) / (max - min);
                    slider.forceValue(Math.max(0.0, Math.min(1.0, sliderPos)));
                    action.accept(val);
                    isUpdating = false;
                } catch (NumberFormatException ignored) {}
            });
        }
    }

    private class CustomSlider extends AbstractSliderButton {
        private final String label;
        private final float min, max;
        private final Consumer<Float> callback;

        public CustomSlider(int x, int y, int w, int h, String label, float cur, float min, float max, Consumer<Float> callback) {
            super(x, y, w, h, Component.empty(), (double) (cur - min) / (max - min));
            this.label    = label;
            this.min      = min;
            this.max      = max;
            this.callback = callback;
            this.updateMessage();
        }

        public void forceValue(double val) {
            this.value = val;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            float val = min + (float) (this.value * (max - min));
            setMessage(Component.literal(label + ": §e" + String.format("%.2f", val)));
        }

        @Override
        protected void applyValue() {
            float val = min + (float) (this.value * (max - min));
            callback.accept(val);
        }
    }
}