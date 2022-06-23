package net.flytre.salutem;

import com.google.common.collect.ImmutableMap;
import net.flytre.salutem.mixin.RenderLayerAccessor;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;

import static net.minecraft.client.render.VertexFormats.*;

public class SalutemRenderPhase extends RenderPhase {

    public static final Identifier HEALTH_BAR_TEXTURE = new Identifier("salutem", "textures/ui/health_bar_texture.png");

    public static final VertexFormat POSITION_TEXTURE_COLOR_NORMAL_LIGHT = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", POSITION_ELEMENT).put("UV0", TEXTURE_ELEMENT).put("Color", COLOR_ELEMENT).put("Normal", NORMAL_ELEMENT).put("UV2", LIGHT_ELEMENT).put("Padding", PADDING_ELEMENT).build());
    private static net.minecraft.client.render.Shader SHADER_INTERNAL_HEALTHBAR;
    private static final RenderPhase.Shader SHADER_HEALTH_BAR = new RenderPhase.Shader(() -> SHADER_INTERNAL_HEALTHBAR);

    public SalutemRenderPhase(String string, Runnable beginAction, Runnable endAction) {
        super(string, beginAction, endAction);
    }

    public static void loadShader(ResourceManager manager) {
        try {
            SHADER_INTERNAL_HEALTHBAR = new net.minecraft.client.render.Shader(manager, "rendertype_salutem_health_bar", POSITION_TEXTURE_COLOR_NORMAL_LIGHT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static RenderLayer getHealthBarLayer(Identifier location) {
        RenderLayer.MultiPhaseParameters parameters = RenderLayer.MultiPhaseParameters.builder().shader(SHADER_HEALTH_BAR).texture(new Texture(location, false, false)).transparency(TRANSLUCENT_TRANSPARENCY).lightmap(RenderPhase.ENABLE_LIGHTMAP).build(true);
        return RenderLayerAccessor.ofInvoker("salutem_health_bar", POSITION_TEXTURE_COLOR_NORMAL_LIGHT, VertexFormat.DrawMode.QUADS, 256, true, true, parameters);
    }
}
