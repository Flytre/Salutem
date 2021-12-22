package net.flytre.salutem;

import com.google.gson.annotations.SerializedName;
import net.flytre.flytre_lib.api.config.annotation.Description;
import net.flytre.flytre_lib.api.config.reference.entity.ConfigEntity;
import net.flytre.flytre_lib.api.config.reference.entity.EntityReference;
import net.minecraft.entity.EntityType;

import java.util.Set;

public class SalutemConfig {


    @Description("List of mobs that will not render their health bar")
    public Set<ConfigEntity> blacklist = Set.of(
            new EntityReference(EntityType.ARMOR_STAND),
            new EntityReference(EntityType.BEE),
            new EntityReference(EntityType.COD),
            new EntityReference(EntityType.PUFFERFISH),
            new EntityReference(EntityType.SALMON),
            new EntityReference(EntityType.SHULKER),
            new EntityReference(EntityType.TROPICAL_FISH)
    );

    @Description("List of mobs to use the boss health bar style for")
    public Set<ConfigEntity> bosses = Set.of(
            new EntityReference(EntityType.ENDER_DRAGON),
            new EntityReference(EntityType.WITHER)
    );

    @Description("Whether the mod should draw healthbars or not (effectively toggling the mod on/off.)")
    public boolean draw = true;

    @Description("The max distance an entity can be and still have its healthbar rendered.")
    @SerializedName("max_distance")
    public int maxDistance = 24;


    @Description("Whether to render health bars when the HUD is disabled by pressing F1.")
    @SerializedName("render_in_f1")
    public boolean renderInF1 = false;

    @Description("Scale modifier for the health bar.")
    @SerializedName("health_bar_scale")
    public float healthBarScale = 1.0F;


    @Description("How high above the entity to draw the healthbar.")
    @SerializedName("height_above")
    public double heightAbove = 0.6;

    @SerializedName("background_padding")
    public int backgroundPadding = 2;

    @SerializedName("background_height")
    public int backgroundHeight = 6;

    @SerializedName("bar_height")
    public int barHeight = 4;
    @SerializedName("plate_size")
    public int plateSize = 25;
    @SerializedName("plate_size_boss")
    public int plateSizeBoss = 50;

    @Description("Whether to show the icon for the \"class\" of the entity, i.e. spider eye for arthropods or rotten flesh for undead.")
    @SerializedName("show_class")
    public boolean showClassIcon = true;

    @Description("Whether to display the armor value for the entity.")
    @SerializedName("show_armor")
    public boolean showArmor = true;

    @Description("When enabled, 5 armor icons = 1 diamond armor icon to reduce clutter.")
    @SerializedName("group_armor")
    public boolean groupArmor = true;


    @SerializedName("show_equipped_gear")
    public boolean showEquippedGear = true;

    @SerializedName("color_style")
    @Description("type_of_mob colors by passive/hostile/boss. health_percent colors red -> yellow -> green based on health. damage_animated colors green with animations based on recent health lost/gained.")
    public ColorStyle colorStyle = ColorStyle.RECENT_DAMAGE;


    @SerializedName("hp_text_height")
    @Description("Negative offset for the health bar text relative to the entity name.")
    public int hpTextHeight = 14;

    @SerializedName("show_max_hp")
    public boolean showMaxHP = true;

    @SerializedName("show_current_hp")
    public boolean showCurrentHP = true;

    @SerializedName("show_percentage")
    public boolean showPercentage = true;

    @SerializedName("show_on_players")
    public boolean showOnPlayers = true;

    @SerializedName("show_on_bosses")
    public boolean showOnBosses = true;

    @SerializedName("show_only_focused")
    @Description("Whether to only show the health of the entity you are looking at or all entities.")
    public boolean showOnlyFocused = false;

    @SerializedName("enable_debug_info")
    public boolean enableDebugInfo = false;

    @SerializedName("show_on_max_health_enemies")
    public boolean showOnMaxHealthEnemies = true;

    public void toggleDraw() {
        draw = !draw;
    }

    public enum ColorStyle {
        @SerializedName("type_of_mob") MOB_TYPE,
        @SerializedName("damage_animated") RECENT_DAMAGE,
        @SerializedName("health_percent") HEALTH_PERCENT
    }
}
