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

// ★ 몹(CEM) 아바타의 silly:setBoundingBoxScale (2026-09-28, 하네스 entity_avatar_template — 베리티 더미 히트박스 너비 3배).
//   PlayerDimensionsMixin 은 Player.getDefaultDimensions 만 잡아서 몹에는 아무 일도 안 했다.
//   LivingEntity.getDimensions 는 final 이라 모든 몹이 여기를 지난다(좀비처럼 getDefaultDimensions 를 덮어쓴 몹도).
//   스케일은 SillyAPI.MOB_BOX_SCALES(엔티티 UUID → 스케일)에서 읽는다 — 크기 계산에서 아바타를 찾거나 만들지 않는다.
//   플레이어 · 잠자는 자세(SLEEPING_DIMENSIONS)는 건드리지 않는다. 눈높이는 바닐라 그대로(PlayerDimensionsMixin 과 같은 이유).
//   클라 표시 전용 — 서버의 실제 히트박스는 그대로다.
@Mixin(LivingEntity.class)
public abstract class MobDimensionsMixin {

    //? if >=1.20.5 {
    @Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
    private void silly$scaleMobHitbox(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (SillyAPI.MOB_BOX_SCALES.isEmpty() || pose == Pose.SLEEPING) return;
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof Player) return;
        // 통합 서버(싱글플레이) 스레드의 서버측 엔티티에도 이 믹스인이 적용된다 — 클라 쪽만
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
