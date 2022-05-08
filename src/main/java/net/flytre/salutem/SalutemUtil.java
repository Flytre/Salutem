package net.flytre.salutem;

import net.flytre.salutem.config.SalutemConfig;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public final class SalutemUtil {

    private static final ItemStack ICON_ILLAGER = new ItemStack(Items.EMERALD);
    private static final ItemStack ICON_AQUATIC = new ItemStack(Items.WATER_BUCKET);
    private static final ItemStack ICON_ARTHROPODS = new ItemStack(Items.SPIDER_EYE);
    private static final ItemStack ICON_UNDEAD = new ItemStack(Items.ROTTEN_FLESH);
    private static final ItemStack ICON_DEFAULT = ItemStack.EMPTY;
    private static final ItemStack ICON_HOSTILE = new ItemStack(Items.IRON_SWORD);
    private static final ItemStack ICON_BOSSES = new ItemStack(Items.WITHER_SKELETON_SKULL);
    public static final ItemStack ICON_IRON_ARMOR = new ItemStack(Items.IRON_CHESTPLATE);
    public static final ItemStack ICON_DIAMOND_ARMOR = new ItemStack(Items.DIAMOND_CHESTPLATE);


    public static ItemStack getIcon(LivingEntity entity, boolean boss) {

        if (boss)
            return ICON_BOSSES;

        EntityGroup group = entity.getGroup();
        if (group == EntityGroup.ARTHROPOD)
            return ICON_ARTHROPODS;
        else if (group == EntityGroup.UNDEAD)
            return ICON_UNDEAD;
        else if (group == EntityGroup.AQUATIC)
            return ICON_AQUATIC;
        else if (group == EntityGroup.ILLAGER)
            return ICON_ILLAGER;
        else if (entity instanceof Monster)
            return ICON_HOSTILE;
        else
            return ICON_DEFAULT;
    }


    public static int getColor(LivingEntity entity, SalutemConfig.ColorStyle colorStyle, boolean boss) {

        assert colorStyle != SalutemConfig.ColorStyle.RECENT_DAMAGE;

        record rgb(int r, int g, int b) {
        }

        if (colorStyle == SalutemConfig.ColorStyle.MOB_TYPE) {
            rgb rgb = new rgb(0, 255, 0);

            if (boss)
                rgb = new rgb(128, 0, 128);
            else if (entity instanceof Monster)
                rgb = new rgb(255, 0, 0);

            return 0xff000000 | rgb.r << 16 | rgb.g << 8 | rgb.b;
        } else {
            float health = MathHelper.clamp(entity.getHealth(), 0.0F, entity.getMaxHealth());
            float hue = Math.max(0.0F, (health / entity.getMaxHealth()) / 3.0F - 0.07F);
            return Color.HSBtoRGB(hue, 1.0F, 1.0F);
        }
    }

    public static int getAlpha(int argb) {
        return (argb >> 24) & 0xFF;
    }

    public static int getRed(int argb) {
        return (argb >> 16) & 0xFF;
    }

    public static int getGreen(int argb) {
        return (argb >> 8) & 0xFF;
    }

    public static int getBlue(int argb) {
        return argb & 0xFF;
    }
}
