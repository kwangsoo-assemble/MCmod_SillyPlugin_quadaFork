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
        // 통합 서버(싱글플레이) 스레드의 ServerPlayer 에도 이 믹스인이 적용되므로 클라 사이드로 제한한다.
        // (AvatarManager 는 클라 전용 API — 서버 스레드에서 호출 시 NPE 크래시)
        if (self.level() == null || !self.level().isClientSide()) return null;
        if (AvatarManager.panic) return null;
        // getAvatarForPlayer 는 백엔드 fetch 부작용이 있어 핫패스에 부적합 — 로드된 아바타만 조회
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
        // 눈높이는 바닐라 값 유지 — 카메라/시선 레이가 서버와 어긋나지 않도록 (custom_hitbox 모드와 동일)
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
