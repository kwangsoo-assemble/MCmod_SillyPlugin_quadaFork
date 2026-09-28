package dev.celestial.silly.mixin;

import dev.celestial.silly.lua.SillyAPI;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// silly:setBoundingBoxScale for mob (CEM) avatars.
//   PlayerDimensionsMixin only hooks Player.getDefaultDimensions, so it did nothing for mobs.
//   LivingEntity.getDimensions is final, so every mob passes through here (including mobs such as zombies that override getDefaultDimensions).
//   The scale comes from SillyAPI.MOB_BOX_SCALES (entity UUID -> scale) - the size calculation never looks up or creates an avatar.
//   Players and the sleeping pose (SLEEPING_DIMENSIONS) are left alone. The eye height stays vanilla (same reason as PlayerDimensionsMixin).
//   Client-side display only - the server's actual hitbox is unchanged.
@Mixin(LivingEntity.class)
public abstract class MobDimensionsMixin {

    //? if >=1.20.5 {
    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void silly$scaleMobHitbox(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (SillyAPI.MOB_BOX_SCALES.isEmpty() || pose == Pose.SLEEPING) return;
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player) return;
        // This mixin also applies to server-side entities on the integrated-server (singleplayer) thread - client side only
        if (self.level() == null || !self.level().isClientSide()) return;
        FiguraVec2 scale = SillyAPI.MOB_BOX_SCALES.get(self.getUUID());
        if (scale == null) return;
        EntityDimensions base = cir.getReturnValue();
        cir.setReturnValue(base.scale((float) scale.x, (float) scale.y).withEyeHeight(base.eyeHeight()));
    }
    //?} else {
    /*@Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void silly$scaleMobHitbox(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (SillyAPI.MOB_BOX_SCALES.isEmpty() || pose == Pose.SLEEPING) return;
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player) return;
        if (self.level() == null || !self.level().isClientSide()) return;
        FiguraVec2 scale = SillyAPI.MOB_BOX_SCALES.get(self.getUUID());
        if (scale == null) return;
        cir.setReturnValue(cir.getReturnValue().scale((float) scale.x, (float) scale.y));
    }
    *///?}
}
