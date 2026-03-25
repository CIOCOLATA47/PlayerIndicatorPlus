package me.cioco.playerindicatorplus;

import me.cioco.playerindicatorplus.config.PlayerIndicatorConfig;
import me.cioco.playerindicatorplus.gui.PlayerIndicatorScreen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class Main implements ModInitializer {
    public static final PlayerIndicatorConfig config = new PlayerIndicatorConfig();

    public static final KeyMapping.Category CATEGORY_PLAYERINDICATOR =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("playerindicator", "key_category"));

    public static KeyMapping toggleKeyBinding;
    public static KeyMapping guiKeyBinding;

    public static boolean toggled = false;

    @Override
    public void onInitialize() {
        config.loadConfiguration();

        toggleKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.playerindicator.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_PLAYERINDICATOR
        ));

        guiKeyBinding = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.playerindicator.open_gui",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_PLAYERINDICATOR
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (toggleKeyBinding.consumeClick()) {
                toggled = !toggled;
                if (client.screen instanceof PlayerIndicatorScreen screen) {
                    screen.refreshGlobalToggle();
                }
                Component status = Component.literal("PlayerIndicator: ")
                        .append(Component.literal(toggled ? "Enabled" : "Disabled")
                                .withStyle(toggled ? ChatFormatting.GREEN : ChatFormatting.RED));
                client.player.sendSystemMessage(status);
            }

            if (guiKeyBinding.consumeClick()) {
                client.setScreen(new PlayerIndicatorScreen(client.screen));
            }
        });
    }
}