package me.cioco.playerindicatorplus;

import me.cioco.playerindicatorplus.config.PlayerIndicatorConfig;
import me.cioco.playerindicatorplus.gui.PlayerIndicatorScreen;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class Main implements ModInitializer {
    public static final PlayerIndicatorConfig config = new PlayerIndicatorConfig();
    public static KeyBinding guiKeyBinding;

    public static final KeyBinding.Category CATEGORY_PLAYERINDICATOR = KeyBinding.Category.create(Identifier.of("playerindicator", "key_category"));


    @Override
    public void onInitialize() {

        config.loadConfiguration();


        guiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.playerindicator.open_gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY_PLAYERINDICATOR
        ));


        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiKeyBinding.wasPressed()) {
                client.setScreen(new PlayerIndicatorScreen(client.currentScreen));
            }
        });
    }
}