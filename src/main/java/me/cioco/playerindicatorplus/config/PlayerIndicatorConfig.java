package me.cioco.playerindicatorplus.config;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class PlayerIndicatorConfig {

    public static final String CONFIG_FILE = "playerindicatorplus-config.properties";
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerIndicatorConfig.class);

    public static boolean showHealthNumbers = true;
    public static float healthTextSize = 0.025F;
    public static float healthVisibilityRange = 64.0F;
    public static float heightAboveHead = 0.85F;
    public static float healthTextHue = 0f;
    public static float healthTextSaturation = 1f;
    public static float healthTextBrightness = 1f;
    public static boolean useTabListHealth = true;

    public static boolean showArmorPercentages = true;
    public static boolean showArmorText = true;
    public static float armorheightAboveHead = 0.65F;
    public static float armorTextSize = 0.02F;
    public static float armorTextHue = 180F;
    public static float armorTextSaturation = 1f;
    public static float armorTextBrightness = 1f;

    public static boolean showMainHand = true;
    public static boolean showOffHand = true;
    public static float equipmentHeightAboveHead = 1.0F;
    public static float equipmentTextSize = 0.02F;

    public static boolean showPing = true;
    public static boolean showDistance = true;
    public static float infoHeightAboveHead = 1.20F;
    public static float infoTextSize = 0.02F;
    public static float infoTextHue = 60F;
    public static float infoTextSaturation = 1f;
    public static float infoTextBrightness = 1f;

    public static boolean showInvisiblePlayers = false;

    public void saveConfiguration() {
        try {
            Path configPath = getConfigPath();
            Files.createDirectories(configPath.getParent());
            try (OutputStream output = Files.newOutputStream(configPath)) {
                Properties props = new Properties();

                props.setProperty("showHealthNumbers", String.valueOf(showHealthNumbers));
                props.setProperty("healthTextSize", String.valueOf(healthTextSize));
                props.setProperty("healthVisibilityRange", String.valueOf(healthVisibilityRange));
                props.setProperty("heightAboveHead", String.valueOf(heightAboveHead));
                props.setProperty("healthTextHue", String.valueOf(healthTextHue));
                props.setProperty("healthTextSaturation", String.valueOf(healthTextSaturation));
                props.setProperty("healthTextBrightness", String.valueOf(healthTextBrightness));
                props.setProperty("useTabListHealth", String.valueOf(useTabListHealth));

                props.setProperty("showArmorPercentages", String.valueOf(showArmorPercentages));
                props.setProperty("showArmorText", String.valueOf(showArmorText));
                props.setProperty("armorheightAboveHead", String.valueOf(armorheightAboveHead));
                props.setProperty("armorTextSize", String.valueOf(armorTextSize));
                props.setProperty("armorTextHue", String.valueOf(armorTextHue));
                props.setProperty("armorTextSaturation", String.valueOf(armorTextSaturation));
                props.setProperty("armorTextBrightness", String.valueOf(armorTextBrightness));

                props.setProperty("showMainHand", String.valueOf(showMainHand));
                props.setProperty("showOffHand", String.valueOf(showOffHand));
                props.setProperty("equipmentHeightAboveHead", String.valueOf(equipmentHeightAboveHead));
                props.setProperty("equipmentTextSize", String.valueOf(equipmentTextSize));

                props.setProperty("showPing", String.valueOf(showPing));
                props.setProperty("showDistance", String.valueOf(showDistance));
                props.setProperty("infoHeightAboveHead", String.valueOf(infoHeightAboveHead));
                props.setProperty("infoTextSize", String.valueOf(infoTextSize));
                props.setProperty("infoTextHue", String.valueOf(infoTextHue));
                props.setProperty("infoTextSaturation", String.valueOf(infoTextSaturation));
                props.setProperty("infoTextBrightness", String.valueOf(infoTextBrightness));

                props.setProperty("showInvisiblePlayers", String.valueOf(showInvisiblePlayers));

                props.store(output, "PlayerIndicatorPlus Config");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }

    public void loadConfiguration() {
        Path configPath = getConfigPath();
        if (!Files.exists(configPath)) return;

        try (InputStream input = Files.newInputStream(configPath)) {
            Properties props = new Properties();
            props.load(input);

            showHealthNumbers = Boolean.parseBoolean(props.getProperty("showHealthNumbers", "true"));
            healthTextSize = Float.parseFloat(props.getProperty("healthTextSize", "0.025"));
            healthVisibilityRange = Float.parseFloat(props.getProperty("healthVisibilityRange", "64.0"));
            heightAboveHead = Float.parseFloat(props.getProperty("heightAboveHead", "0.85"));
            healthTextHue = Float.parseFloat(props.getProperty("healthTextHue", "0"));
            healthTextSaturation = Float.parseFloat(props.getProperty("healthTextSaturation", "1"));
            healthTextBrightness = Float.parseFloat(props.getProperty("healthTextBrightness", "1"));
            useTabListHealth = Boolean.parseBoolean(props.getProperty("useTabListHealth", "true"));

            showArmorPercentages = Boolean.parseBoolean(props.getProperty("showArmorPercentages", "true"));
            showArmorText = Boolean.parseBoolean(props.getProperty("showArmorText", "true"));
            armorheightAboveHead = Float.parseFloat(props.getProperty("armorheightAboveHead", "0.65"));
            armorTextSize = Float.parseFloat(props.getProperty("armorTextSize", "0.02"));
            armorTextHue = Float.parseFloat(props.getProperty("armorTextHue", "180"));
            armorTextSaturation = Float.parseFloat(props.getProperty("armorTextSaturation", "1"));
            armorTextBrightness = Float.parseFloat(props.getProperty("armorTextBrightness", "1"));

            showMainHand = Boolean.parseBoolean(props.getProperty("showMainHand", "true"));
            showOffHand = Boolean.parseBoolean(props.getProperty("showOffHand", "true"));
            equipmentHeightAboveHead = Float.parseFloat(props.getProperty("equipmentHeightAboveHead", "1.0"));
            equipmentTextSize = Float.parseFloat(props.getProperty("equipmentTextSize", "0.02"));

            showPing = Boolean.parseBoolean(props.getProperty("showPing", "true"));
            showDistance = Boolean.parseBoolean(props.getProperty("showDistance", "true"));
            infoHeightAboveHead = Float.parseFloat(props.getProperty("infoHeightAboveHead", "1.20"));
            infoTextSize = Float.parseFloat(props.getProperty("infoTextSize", "0.02"));
            infoTextHue = Float.parseFloat(props.getProperty("infoTextHue", "60"));
            infoTextSaturation = Float.parseFloat(props.getProperty("infoTextSaturation", "1"));
            infoTextBrightness = Float.parseFloat(props.getProperty("infoTextBrightness", "1"));

            showInvisiblePlayers = Boolean.parseBoolean(props.getProperty("showInvisiblePlayers", "false"));
        } catch (Exception e) {
            LOGGER.error("Failed to load config", e);
        }
    }

    private Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE);
    }
}