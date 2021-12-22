package net.flytre.salutem;

import net.fabricmc.api.ClientModInitializer;
import net.flytre.flytre_lib.api.base.util.KeyBindUtils;
import net.flytre.flytre_lib.api.config.ConfigHandler;
import net.flytre.flytre_lib.api.config.ConfigRegistry;
import net.flytre.flytre_lib.api.config.GsonHelper;
import net.flytre.flytre_lib.api.event.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

public class Salutem implements ClientModInitializer {

    private static KeyBinding toggleKey;
    public static final ConfigHandler<SalutemConfig> CONFIG = new ConfigHandler<>(new SalutemConfig(), "salutem", "config.salutem", GsonHelper.GSON);


    static {
        //configure debug logging if certain flags are set. this also ensures compatibility with mainline Mesh-Library debug behaviour, without directly depending on the library
        if(Boolean.getBoolean("fabric.development") || Boolean.getBoolean("orderly.debug") || Boolean.getBoolean("mesh.debug") || Boolean.getBoolean("mesh.debug.logging")) {
            Configurator.setLevel("salutem", Level.ALL);
        }
    }

    @Override
    public void onInitializeClient() {
        toggleKey =  KeyBindUtils.register(new KeyBinding(
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
