package net.flytre.salutem.mixin;

import net.flytre.salutem.SalutemTrackedData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedList;
import java.util.Queue;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements SalutemTrackedData {

    @Unique
    private Queue<Float> healthSnapshot;

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    public abstract float getHealth();

    @Inject(method = "tick", at = @At("TAIL"))
    public void salutem$health_animation_tracker(CallbackInfo ci) {
        if(healthSnapshot == null)
            healthSnapshot = new LinkedList<>();

        if (healthSnapshot.size() > 10)
            healthSnapshot.remove();
        healthSnapshot.add(getHealth());
    }

    @Override
    public float getPastHealth() {
        if(healthSnapshot == null)
            healthSnapshot = new LinkedList<>();

        return healthSnapshot.size() > 0 ? healthSnapshot.peek() : getHealth();
    }
}
