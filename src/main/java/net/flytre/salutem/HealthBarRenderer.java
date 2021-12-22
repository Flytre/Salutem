package net.flytre.salutem;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.flytre.flytre_lib.api.base.util.EntityUtils;
import net.flytre.flytre_lib.api.config.reference.entity.ConfigEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;
import java.util.Stack;
import java.util.stream.StreamSupport;

public class HealthBarRenderer {

    public static final float SCALE_MULTIPLIER = 0.026666672F;


    public static void render(MatrixStack matrices, float partialTicks, Camera camera, Matrix4f projection, Frustum capturedFrustum) {
        MinecraftClient client = MinecraftClient.getInstance();
        SalutemConfig config = Salutem.CONFIG.getConfig();

        if (client.world == null || (!config.renderInF1 && !MinecraftClient.isHudEnabled()) || !config.draw)
            return;

        final Entity cameraEntity = camera.getFocusedEntity() != null ? camera.getFocusedEntity() : client.player; //possible fix for Optifine
        assert cameraEntity != null : "Camera Entity must not be null!";

        if (config.showOnlyFocused) {
            Entity focused = EntityUtils.getEntityLookedAt(cameraEntity, 32);
            if (focused instanceof LivingEntity && focused.isAlive())
                renderHealthBar((LivingEntity) focused, matrices, partialTicks, camera, cameraEntity);
        } else {
            Vec3d cameraPos = camera.getPos();
            final Frustum frustum;
            if (capturedFrustum != null) {
                frustum = capturedFrustum;
            } else {
                frustum = new Frustum(matrices.peek().getPositionMatrix(), projection);
                frustum.setPosition(cameraPos.getX(), cameraPos.getY(), cameraPos.getZ());
            }
            StreamSupport
                    .stream(client.world.getEntities().spliterator(), false)
                    .filter(entity -> entity instanceof LivingEntity &&
                            entity != cameraEntity &&
                            entity.isAlive() &&
                            entity.getPassengerList().isEmpty() &&
                            entity.shouldRender(cameraPos.getX(), cameraPos.getY(), cameraPos.getZ()) &&
                            (entity.ignoreCameraFrustum || frustum.isVisible(entity.getBoundingBox())))
                    .map(LivingEntity.class::cast)
                    .forEach(entity -> renderHealthBar(entity, matrices, partialTicks, camera, cameraEntity));
        }
    }


    private static void renderHealthBar(LivingEntity original, MatrixStack matrices, float partialTicks, Camera camera, Entity viewPoint) {
        Preconditions.checkNotNull(original, "Tried to render a health bar for null entity");
        SalutemConfig config = Salutem.CONFIG.getConfig();
        MinecraftClient client = MinecraftClient.getInstance();

        Stack<LivingEntity> ridingStack = new Stack<>();


        {
            Entity entity = original;
            while (entity instanceof LivingEntity) {
                ridingStack.push((LivingEntity) entity);
                entity = entity.getVehicle();
            }
        }


        matrices.push();
        while (!ridingStack.isEmpty()) {
            LivingEntity entity = ridingStack.pop();

            if (!entity.isAlive())
                continue;

            boolean boss = ConfigEntity.contains(config.bosses, entity.getType(), original.world);

            if (ConfigEntity.contains(config.blacklist, entity.getType(), original.world))
                continue;


            processing:
            {
                float distance = original.distanceTo(viewPoint);

                if (distance > config.maxDistance || !original.canSee(viewPoint) || entity.isInvisible())
                    break processing;

                if (boss && !config.showOnBosses)
                    break processing;

                if (!config.showOnPlayers && entity instanceof PlayerEntity)
                    break processing;

                if (entity.getMaxHealth() <= 0.0F)
                    break processing;

                if (!config.showOnMaxHealthEnemies && entity.getHealth() == entity.getMaxHealth())
                    break processing;

                double x = original.prevX + (original.getX() - original.prevX) * partialTicks;
                double y = original.prevY + (original.getY() - original.prevY) * partialTicks;
                double z = original.prevZ + (original.getZ() - original.prevZ) * partialTicks;

                EntityRenderDispatcher renderManager = MinecraftClient.getInstance().getEntityRenderDispatcher();

                {
                    matrices.push();
                    matrices.translate(x - renderManager.camera.getPos().x, y - renderManager.camera.getPos().y + original.getHeight() + config.heightAbove, z - renderManager.camera.getPos().z);
                    DiffuseLighting.disableGuiDepthLighting();
                    VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
                    ItemStack icon = SalutemUtil.getIcon(entity, boss);
                    final int light = 0xF000F0;
                    if (boss)
                        renderBossEntity(matrices, immediate, camera, config, entity, light, icon);
                    else
                        renderEntity(matrices, immediate, camera, config, entity, light, icon);
                    matrices.pop();
                }
                int gearCount = gearCount(entity);
                if (gearCount > 0)
                    matrices.translate(0, SCALE_MULTIPLIER * 8, 0);
                matrices.translate(0.0D, SCALE_MULTIPLIER * (config.backgroundHeight + config.barHeight + config.backgroundPadding), 0.0D);

            }
        }
        matrices.pop();
    }

    private static int gearCount(LivingEntity entity) {
        return Salutem.CONFIG.getConfig().showEquippedGear ? (int) Arrays.stream(EquipmentSlot.values()).map(entity::getEquippedStack).filter(i -> !i.isEmpty()).count() : 0;
    }


    private static void render(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, Camera camera, SalutemConfig config, LivingEntity entity, int light, ItemStack icon, boolean boss) {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        MinecraftClient client = MinecraftClient.getInstance();
        Quaternion rotation = camera.getRotation().copy();
        rotation.scale(-1.0F);
        matrices.multiply(rotation);
        float scale = HealthBarRenderer.SCALE_MULTIPLIER * config.healthBarScale;
        matrices.scale(-scale, -scale, scale);
        float health = MathHelper.clamp(entity.getHealth(), 0.0F, entity.getMaxHealth());
        float percent = (health / entity.getMaxHealth()) * 100.0F;
        float size = boss ? config.plateSizeBoss : config.plateSize;
        float textScale = 0.5F;

        String name = (entity.hasCustomName() ? ((MutableText) Objects.requireNonNull(entity.getCustomName())).formatted(Formatting.ITALIC) : entity.getDisplayName()).getString();
        float nameSize = client.textRenderer.getWidth(name) * textScale;
        if (nameSize + 20 > size * 2)
            size = nameSize / 2.0F + 10.0F;

        float healthSize = size * (entity.getHealth() / entity.getMaxHealth());
        MatrixStack.Entry entry = matrices.peek();
        Matrix4f modelViewMatrix = entry.getPositionMatrix();
        Vec3f normal = new Vec3f(0.0F, 1.0F, 0.0F);
        normal.transform(entry.getNormalMatrix());
        VertexConsumer buffer = immediate.getBuffer(SalutemRenderPhase.getHealthBarLayer(SalutemRenderPhase.HEALTH_BAR_TEXTURE)); // VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL
        float padding = config.backgroundPadding;
        int backgroundHeight = config.backgroundHeight;
        int barHeight = config.barHeight;

        int gearCount = gearCount(entity);

        // Background
        if (config.draw) {
            float maxY = barHeight + padding;
            float minY = -backgroundHeight + (gearCount > 0 ? -8 : 0);
            buffer.vertex(modelViewMatrix, -size - padding, minY, 0.01F).texture(0.0F, 0.0F).color(0, 0, 0, 64).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
            buffer.vertex(modelViewMatrix, -size - padding, maxY, 0.01F).texture(0.0F, 0.5F).color(0, 0, 0, 64).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
            buffer.vertex(modelViewMatrix, size + padding, maxY, 0.01F).texture(1.0F, 0.5F).color(0, 0, 0, 64).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
            buffer.vertex(modelViewMatrix, size + padding, minY, 0.01F).texture(1.0F, 0.0F).color(0, 0, 0, 64).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
        }

        if (gearCount > 0)
            matrices.translate(0, -8, 0);

        // Health Bar
        if (!(config.colorStyle == SalutemConfig.ColorStyle.RECENT_DAMAGE)) {
            int argb = SalutemUtil.getColor(entity, config.colorStyle, boss);
            int r = SalutemUtil.getRed(argb);
            int g = SalutemUtil.getGreen(argb);
            int b = SalutemUtil.getBlue(argb);
            buffer.vertex(modelViewMatrix, -size, 0, 0.001F).texture(0.0F, 0.75F).color(r, g, b, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
            buffer.vertex(modelViewMatrix, -size, barHeight, 0.001F).texture(0.0F, 1.0F).color(r, g, b, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
            buffer.vertex(modelViewMatrix, healthSize * 2 - size, barHeight, 0.001F).texture(1.0F, 1.0F).color(r, g, b, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
            buffer.vertex(modelViewMatrix, healthSize * 2 - size, 0, 0.001F).texture(1.0F, 0.75F).color(r, g, b, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
        } else {
            float pastSize = size * (((SalutemTrackedData) entity).getPastHealth() / entity.getMaxHealth());
            float healthDiff = pastSize - healthSize;

            if (healthDiff >= 0) {
                greenQuadHelper(light, size, modelViewMatrix, normal, buffer, barHeight, healthSize);
                buffer.vertex(modelViewMatrix, healthSize * 2 - size, 0, 0.001F).texture(0.0F, 0.75F).color(255, 0, 0, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
                buffer.vertex(modelViewMatrix, healthSize * 2 - size, barHeight, 0.001F).texture(0.0F, 1.0F).color(255, 0, 0, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
                buffer.vertex(modelViewMatrix, healthSize * 2 - size + healthDiff * 2, barHeight, 0.001F).texture(1.0F, 1.0F).color(255, 0, 0, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
                buffer.vertex(modelViewMatrix, healthSize * 2 - size + healthDiff * 2, 0, 0.001F).texture(1.0F, 0.75F).color(255, 0, 0, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
            } else {
                greenQuadHelper(light, size, modelViewMatrix, normal, buffer, barHeight, pastSize);
                buffer.vertex(modelViewMatrix, pastSize * 2 - size, 0, 0.001F).texture(0.0F, 0.75F).color(0, 0, 255, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
                buffer.vertex(modelViewMatrix, pastSize * 2 - size, barHeight, 0.001F).texture(0.0F, 1.0F).color(0, 0, 255, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
                buffer.vertex(modelViewMatrix, pastSize * 2 - size - healthDiff * 2, barHeight, 0.001F).texture(1.0F, 1.0F).color(0, 0, 255, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
                buffer.vertex(modelViewMatrix, pastSize * 2 - size - healthDiff * 2, 0, 0.001F).texture(1.0F, 0.75F).color(0, 0, 255, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
            }
        }
        //Health bar background
        if (healthSize < size) {
            buffer.vertex(modelViewMatrix, -size + healthSize * 2, 0, 0.001F).texture(0.0F, 0.5F).color(0, 0, 0, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
            buffer.vertex(modelViewMatrix, -size + healthSize * 2, barHeight, 0.001F).texture(0.0F, 0.75F).color(0, 0, 0, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
            buffer.vertex(modelViewMatrix, size, barHeight, 0.001F).texture(1.0F, 0.75F).color(0, 0, 0, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
            buffer.vertex(modelViewMatrix, size, 0, 0.001F).texture(1.0F, 0.5F).color(0, 0, 0, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
        }

        // Foreground
        matrices.push();
        {
            int white = 0xFFFFFF;
            int black = 0x000000;
            matrices.translate(-size, -4.5F, 0.0F);
            matrices.scale(textScale, textScale, textScale);
            modelViewMatrix = matrices.peek().getPositionMatrix();
            client.textRenderer.draw(name, 0, 0, white, false, modelViewMatrix, immediate, false, black, light);

            float s1 = 0.75F;
            matrices.push();
            {
                matrices.scale(s1, s1, s1);
                modelViewMatrix = matrices.peek().getPositionMatrix();
                int textHeight = config.hpTextHeight;
                String maxHpString = String.format("%.2f", entity.getMaxHealth()).replaceAll("\\.00$", "");
                String hpString = String.format("%.2f", health).replaceAll("\\.00$", "");
                String percentString = String.format("%.2f%%", percent).replace(".00%", "%");

                if (maxHpString.endsWith(".00"))
                    maxHpString = maxHpString.substring(0, maxHpString.length() - 3);

                if (hpString.endsWith(".00"))
                    hpString = hpString.substring(0, hpString.length() - 3);


                if (config.showCurrentHP)
                    client.textRenderer.draw(hpString, 2, textHeight, white, false, modelViewMatrix, immediate, false, black, light);
                if (config.showMaxHP)
                    client.textRenderer.draw(maxHpString, (int) (size / (textScale * s1) * 2) - 2 - client.textRenderer.getWidth(maxHpString), textHeight, white, false, modelViewMatrix, immediate, false, black, light);
                if (config.showPercentage)
                    client.textRenderer.draw(percentString, (int) (size / (textScale * s1)) - client.textRenderer.getWidth(percentString) / 2.0F, textHeight, white, false, modelViewMatrix, immediate, false, black, light);
                if (config.enableDebugInfo && client.options.debugEnabled)
                    client.textRenderer.draw(String.format("ID: \"%s\"", Registry.ENTITY_TYPE.getId(entity.getType())), 0, textHeight + 16, white, false, modelViewMatrix, immediate, false, black, light);
            }
            matrices.pop();

            matrices.push();
            int off = -8;
            s1 = 0.5F;
            matrices.scale(s1, s1, s1);

            matrices.translate(size / (textScale * s1) * 2, 0.0F, 0.0F);

            RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
            if (icon != null && config.showClassIcon) {
                renderIcon(off, config.backgroundPadding * 2, icon, matrices, immediate, light);
                off -= 16;
            }

            int armor = entity.getArmor();
            if (armor > 0 && config.showArmor) {
                for (int i = 0; i < (config.groupArmor ? armor % 5 : armor); i++, off -= 4)
                    renderIcon(off, config.backgroundPadding * 2, SalutemUtil.ICON_IRON_ARMOR, matrices, immediate, light);

                for (int i = 0; i < (config.groupArmor ? armor / 5 : 0); i++, off -= 4)
                    renderIcon(off, config.backgroundPadding * 2, SalutemUtil.ICON_DIAMOND_ARMOR, matrices, immediate, light);
            }
            matrices.pop();
        }

        //Gear
        if (gearCount > 0) {

            matrices.push();
            matrices.translate(size / textScale, 0.0F, 0.0F);
            matrices.scale(0.75f, 0.75f, 0.75f);
            int off = -gearCount * 8;
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (!entity.getEquippedStack(slot).isEmpty()) {
                    renderIcon(off, (int) (1 / 0.75f * (size + padding)), entity.getEquippedStack(slot), matrices, immediate, light);
                    off += 16;
                }
            }
            matrices.pop();

        }

        matrices.pop();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void greenQuadHelper(int light, float size, Matrix4f modelViewMatrix, Vec3f normal, VertexConsumer buffer, int barHeight, float barSize) {
        buffer.vertex(modelViewMatrix, -size, 0, 0.001F).texture(0.0F, 0.75F).color(0, 255, 0, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
        buffer.vertex(modelViewMatrix, -size, barHeight, 0.001F).texture(0.0F, 1.0F).color(0, 255, 0, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
        buffer.vertex(modelViewMatrix, barSize * 2 - size, barHeight, 0.001F).texture(1.0F, 1.0F).color(0, 255, 0, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
        buffer.vertex(modelViewMatrix, barSize * 2 - size, 0, 0.001F).texture(1.0F, 0.75F).color(0, 255, 0, 127).normal(normal.getX(), normal.getY(), normal.getZ()).light(light).next();
    }


    private static void renderIcon(int x, int y, @NotNull ItemStack icon, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        MinecraftClient client = MinecraftClient.getInstance();

        matrices.push();
        matrices.translate(8 + x, y, -1.0D);
        matrices.push();

        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-180));

        matrices.scale(16.0F, 16.0F, 1.0F);
        BakedModel bakedModel = client.getItemRenderer().getModels().getModel(icon);
        ItemRenderer renderer = client.getItemRenderer();
        renderer.renderItem(icon, ModelTransformation.Mode.GUI, false, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV, bakedModel);
        matrices.pop();
        matrices.pop();
    }

    private static void renderEntity(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, Camera camera, SalutemConfig config, LivingEntity entity, int light, ItemStack icon) {
        render(matrices, immediate, camera, config, entity, light, icon, false);
    }

    private static void renderBossEntity(MatrixStack matrices, VertexConsumerProvider.Immediate immediate, Camera camera, SalutemConfig config, LivingEntity entity, int light, ItemStack icon) {
        render(matrices, immediate, camera, config, entity, light, icon, true);
    }
}
