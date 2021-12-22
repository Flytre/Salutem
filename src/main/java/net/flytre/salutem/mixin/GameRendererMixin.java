package net.flytre.salutem.mixin;

import net.flytre.salutem.SalutemRenderPhase;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "loadShaders", at = @At("HEAD"))
    public void salutem$loadShaders(ResourceManager manager, CallbackInfo ci) {
        SalutemRenderPhase.loadShader(manager);
    }
}
