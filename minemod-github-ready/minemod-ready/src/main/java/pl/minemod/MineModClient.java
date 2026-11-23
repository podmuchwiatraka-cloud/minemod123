package pl.minemod;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import pl.minemod.config.Config;
import pl.minemod.cosmetics.CosmeticRegistry;
import pl.minemod.gui.MineModScreen;
import pl.minemod.hud.HudRenderer;

public class MineModClient implements ClientModInitializer {
    public static final String MOD_ID = "minemod";

    private static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        Config.load();
        CosmeticRegistry.init();
        HudRenderer.register();

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.minemod.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.create(Identifier.of(MOD_ID, "main"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new MineModScreen());
                }
            }
        });
    }
}
