package net.flytre.salutem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.flytre.flytre_lib.config.ConfigHandler;
import net.flytre.flytre_lib.config.ConfigRegistry;
import net.flytre.salutem.config.SalutemConfigImpl;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

public class Salutem implements ClientModInitializer {

    public static final String MODID = "salutem";
    private static KeyBinding toggleKey;
    public static final ConfigHandler<SalutemConfigImpl> CONFIG = new ConfigHandler<>(new SalutemConfigImpl(), "salutem");


    static {
        //configure debug logging if certain flags are set. this also ensures compatibility with mainline Mesh-Library debug behaviour, without directly depending on the library
        if(Boolean.getBoolean("fabric.development") || Boolean.getBoolean("orderly.debug") || Boolean.getBoolean("mesh.debug") || Boolean.getBoolean("mesh.debug.logging")) {
            Configurator.setLevel(MODID, Level.ALL);
        }
    }

    @Override
    public void onInitializeClient() {
        toggleKey =  KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "salutem.keybind.toggle",
                InputUtil.Type.KEYSYM,
                InputUtil.UNKNOWN_KEY.getCode(),
                "key.categories.misc"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(event -> {
            if (event.isWindowFocused() && toggleKey.wasPressed()) {
                CONFIG.getConfig().toggleDraw();
            }
        });

        CONFIG.handle();
        ConfigRegistry.registerClientConfig(CONFIG);
    }
}
