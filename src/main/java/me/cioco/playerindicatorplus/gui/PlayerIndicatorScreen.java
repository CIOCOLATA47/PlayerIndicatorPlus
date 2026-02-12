package me.cioco.playerindicatorplus.gui;

import me.cioco.playerindicatorplus.config.PlayerIndicatorConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class PlayerIndicatorScreen extends Screen {

    private final Screen parent;
    private final PlayerIndicatorConfig config = new PlayerIndicatorConfig();

    private int healthY;
    private int armorY;
    private int equipmentY;
    private int infoY;

    public PlayerIndicatorScreen(Screen parent) {
        super(Text.literal("PlayerIndicatorPlus Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int rowSpacing = 24;
        int sectionPadding = 15;

        this.healthY = 35;
        this.armorY = healthY + (rowSpacing * 4) + sectionPadding;
        this.equipmentY = armorY + (rowSpacing * 3) + sectionPadding;
        this.infoY = equipmentY + (rowSpacing * 2) + sectionPadding;

        this.addDrawableChild(createToggleButton(centerX - 155, healthY, "Show Health", "Numeric health display.", PlayerIndicatorConfig.showHealthNumbers, val -> PlayerIndicatorConfig.showHealthNumbers = val));
        this.addDrawableChild(createToggleButton(centerX + 5, healthY, "Show Invisibles", "Render for invisible players.", PlayerIndicatorConfig.showInvisiblePlayers, val -> PlayerIndicatorConfig.showInvisiblePlayers = val));

        this.addDrawableChild(new GenericSlider(centerX - 155, healthY + rowSpacing, 150, 20, "Size", PlayerIndicatorConfig.healthTextSize, 0.01F, 0.05F, val -> PlayerIndicatorConfig.healthTextSize = val));
        this.addDrawableChild(new GenericSlider(centerX + 5, healthY + rowSpacing, 150, 20, "Range", PlayerIndicatorConfig.healthVisibilityRange, 16F, 128F, val -> PlayerIndicatorConfig.healthVisibilityRange = val));

        this.addDrawableChild(new GenericSlider(centerX - 155, healthY + rowSpacing * 2, 150, 20, "Height", PlayerIndicatorConfig.heightAboveHead, 0.2F, 2.0F, val -> PlayerIndicatorConfig.heightAboveHead = val));
        this.addDrawableChild(new GenericSlider(centerX + 5, healthY + rowSpacing * 2, 150, 20, "Hue", PlayerIndicatorConfig.healthTextHue, 0F, 360F, val -> PlayerIndicatorConfig.healthTextHue = val));

        this.addDrawableChild(new GenericSlider(centerX + 5, healthY + rowSpacing * 3, 150, 20, "Brightness", PlayerIndicatorConfig.healthTextBrightness, 0F, 1F, val -> PlayerIndicatorConfig.healthTextBrightness = val));

        this.addDrawableChild(createToggleButton(centerX - 155, armorY, "Show Percent", "Armor durability percentage.", PlayerIndicatorConfig.showArmorPercentages, val -> PlayerIndicatorConfig.showArmorPercentages = val));
        this.addDrawableChild(createToggleButton(centerX + 5, armorY, "Show Text", "Armor type names.", PlayerIndicatorConfig.showArmorText, val -> PlayerIndicatorConfig.showArmorText = val));

        this.addDrawableChild(new GenericSlider(centerX - 155, armorY + rowSpacing, 150, 20, "Height", PlayerIndicatorConfig.armorheightAboveHead, 0.2F, 2.0F, val -> PlayerIndicatorConfig.armorheightAboveHead = val));
        this.addDrawableChild(new GenericSlider(centerX + 5, armorY + rowSpacing, 150, 20, "Text Size", PlayerIndicatorConfig.armorTextSize, 0.01F, 0.05F, val -> PlayerIndicatorConfig.armorTextSize = val));

        this.addDrawableChild(new GenericSlider(centerX - 155, armorY + rowSpacing * 2, 150, 20, "Hue", PlayerIndicatorConfig.armorTextHue, 0F, 360F, val -> PlayerIndicatorConfig.armorTextHue = val));
        this.addDrawableChild(new GenericSlider(centerX + 5, armorY + rowSpacing * 2, 150, 20, "Saturation", PlayerIndicatorConfig.armorTextSaturation, 0F, 1F, val -> PlayerIndicatorConfig.armorTextSaturation = val));

        this.addDrawableChild(createToggleButton(centerX - 155, equipmentY, "Main Hand", "Show held item.", PlayerIndicatorConfig.showMainHand, val -> PlayerIndicatorConfig.showMainHand = val));
        this.addDrawableChild(createToggleButton(centerX + 5, equipmentY, "Off Hand", "Show offhand item.", PlayerIndicatorConfig.showOffHand, val -> PlayerIndicatorConfig.showOffHand = val));

        this.addDrawableChild(new GenericSlider(centerX - 155, equipmentY + rowSpacing, 150, 20, "Height", PlayerIndicatorConfig.equipmentHeightAboveHead, 0.2F, 2.0F, val -> PlayerIndicatorConfig.equipmentHeightAboveHead = val));
        this.addDrawableChild(new GenericSlider(centerX + 5, equipmentY + rowSpacing, 150, 20, "Text Size", PlayerIndicatorConfig.equipmentTextSize, 0.01F, 0.05F, val -> PlayerIndicatorConfig.equipmentTextSize = val));

        this.addDrawableChild(createToggleButton(centerX - 155, infoY, "Show Ping", "Latency display.", PlayerIndicatorConfig.showPing, val -> PlayerIndicatorConfig.showPing = val));
        this.addDrawableChild(createToggleButton(centerX + 5, infoY, "Show Distance", "Distance in blocks.", PlayerIndicatorConfig.showDistance, val -> PlayerIndicatorConfig.showDistance = val));

        this.addDrawableChild(new GenericSlider(centerX - 155, infoY + rowSpacing, 150, 20, "Height", PlayerIndicatorConfig.infoHeightAboveHead, 0.1F, 2.0F, val -> PlayerIndicatorConfig.infoHeightAboveHead = val));
        this.addDrawableChild(new GenericSlider(centerX + 5, infoY + rowSpacing, 150, 20, "Hue", PlayerIndicatorConfig.infoTextHue, 0F, 360F, val -> PlayerIndicatorConfig.infoTextHue = val));

        this.addDrawableChild(new GenericSlider(centerX - 155, infoY + rowSpacing * 2, 150, 20, "Text Size", PlayerIndicatorConfig.infoTextSize, 0.01F, 0.05F, val -> PlayerIndicatorConfig.infoTextSize = val));
        this.addDrawableChild(new GenericSlider(centerX + 5, infoY + rowSpacing * 2, 150, 20, "Saturation", PlayerIndicatorConfig.infoTextSaturation, 0F, 1F, val -> PlayerIndicatorConfig.infoTextSaturation = val));

        this.addDrawableChild(new GenericSlider(centerX + 5, infoY + rowSpacing * 3, 150, 20, "Brightness", PlayerIndicatorConfig.infoTextBrightness, 0F, 1F, val -> PlayerIndicatorConfig.infoTextBrightness = val));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(context);

        int centerX = this.width / 2;
        int pW = 330;

        drawStyledPanel(context, centerX - 165, healthY - 15, pW, 110);
        drawStyledPanel(context, centerX - 165, armorY - 15, pW, 85);
        drawStyledPanel(context, centerX - 165, equipmentY - 15, pW, 60);
        drawStyledPanel(context, centerX - 165, infoY - 15, pW, 110);

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, "§6§lPLAYER INDICATOR+", centerX, 10, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "§b§l> §fHealth", centerX - 158, healthY - 10, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "§b§l> §fArmor", centerX - 158, armorY - 10, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "§b§l> §fEquipment", centerX - 158, equipmentY - 10, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "§b§l> §fConnection & Info", centerX - 158, infoY - 10, 0xFFFFFFFF);
    }

    private void drawStyledPanel(DrawContext context, int x, int y, int width, int height) {
        context.fill(x, y, x + width, y + height, 0x55000000);
        context.fill(x, y, x + 2, y + height, 0xFFFFAA00);
    }

    @Override
    public void close() {
        config.saveConfiguration();
        if (this.client != null) this.client.setScreen(this.parent);
    }

    private ButtonWidget createToggleButton(int x, int y, String label, String desc, boolean initVal, Consumer<Boolean> action) {
        return ButtonWidget.builder(getToggleText(label, initVal), button -> {
                    boolean newVal = !button.getMessage().getString().contains("ON");
                    action.accept(newVal);
                    button.setMessage(getToggleText(label, newVal));
                }).dimensions(x, y, 150, 20)
                .tooltip(Tooltip.of(Text.literal(desc)))
                .build();
    }

    private Text getToggleText(String label, boolean value) {
        return Text.literal(label + ": ")
                .append(value ? Text.literal("ON").formatted(Formatting.GREEN) : Text.literal("OFF").formatted(Formatting.RED));
    }

    private static class GenericSlider extends SliderWidget {
        private final String label;
        private final float min, max;
        private final Consumer<Float> updateAction;

        public GenericSlider(int x, int y, int w, int h, String label, float cur, float min, float max, Consumer<Float> action) {
            super(x, y, w, h, Text.empty(), (cur - min) / (max - min));
            this.label = label;
            this.min = min;
            this.max = max;
            this.updateAction = action;
            updateMessage();
        }

        @Override protected void updateMessage() {
            float val = min + (float) (this.value * (max - min));
            this.setMessage(Text.literal(label + ": §e" + String.format("%.2f", val)));
        }

        @Override protected void applyValue() {
            float val = min + (float) (this.value * (max - min));
            updateAction.accept(val);
        }
    }
}