package dev.celestial.silly.mixin;

import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.not_a_mixin.AvatarExtensions;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.math.vector.FiguraVec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerDimensionsMixin {

    @Unique
    private FiguraVec2 silly$getBoundingBoxScale() {
        Player self = (Player) (Object) this;
        // This mixin also applies to the ServerPlayer on the integrated-server (singleplayer) thread,
        // so restrict it to the client side.
        // (AvatarManager is a client-only API; calling it from the server thread crashes with an NPE)
        if (self.level() == null || !self.level().isClientSide()) return null;
        if (AvatarManager.panic) return null;
        // getAvatarForPlayer has a backend-fetch side effect, which makes it unsuitable for this hot path,
        // so only look up avatars that are already loaded
        Avatar avatar = AvatarManager.getLoadedAvatar(self.getUUID());
        if (avatar == null) return null;
        SillyAPI silly = ((AvatarExtensions) avatar).silly$getSilly();
        if (silly == null) return null;
        return silly.boundingBoxScale;
    }

    //? if >=1.20.5 {
    @Inject(method = "getDefaultDimensions", at = @At("RETURN"), cancellable = true)
    private void silly$scaleHitbox(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        FiguraVec2 scale = silly$getBoundingBoxScale();
        if (scale == null) return;
        EntityDimensions base = cir.getReturnValue();
        // Keep the vanilla eye height so the camera and look ray do not drift out of sync with the server
        cir.setReturnValue(base.scale((float) scale.x, (float) scale.y).withEyeHeight(base.eyeHeight()));
    }
    //?} else {
    /*@Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void silly$scaleHitbox(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        FiguraVec2 scale = silly$getBoundingBoxScale();
        if (scale == null) return;
        cir.setReturnValue(cir.getReturnValue().scale((float) scale.x, (float) scale.y));
    }
    *///?}
}
