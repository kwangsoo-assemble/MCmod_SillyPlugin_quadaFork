package dev.celestial.silly.mixin;

import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.not_a_mixin.AvatarExtensions;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// ★ silly:setSprintParticles(false) — 그 아바타 착용 엔티티의 달리기 파티클을 끈다 (2026-10-30, 하네스 본격플러팅강좌 스태프 판넬).
//   바닐라는 baseTick 에서 canSpawnSprintParticle() 이 참이면 spawnSprintParticle() 을 부른다 — 보는 클라가 엔티티마다 따로.
//   그래서 RETURN 에서 참일 때만 그 엔티티의 로드된 아바타를 찾아 false 로 바꾼다(달리지 않는 대부분의 틱은 조회 없이 지나간다).
//   아바타 조회는 PlayerDimensionsMixin 과 같은 꼴 — getLoadedAvatar(부작용 없음) · panic 이면 안 건드린다.
//   착지 파티클(서버가 보내는 것)은 여기서 못 끈다.
@Mixin(Entity.class)
public abstract class SprintParticleMixin {

    @Inject(method = "canSpawnSprintParticle", at = @At("RETURN"), cancellable = true)
    private void silly$hideSprintParticle(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;
        Entity self = (Entity) (Object) this;
        // 통합 서버(싱글플레이) 스레드의 서버측 엔티티에도 이 믹스인이 적용된다 — AvatarManager 는 클라 전용이라 클라 쪽만
        if (self.level() == null || !self.level().isClientSide()) return;
        if (AvatarManager.panic) return;
        Avatar avatar = AvatarManager.getLoadedAvatar(self.getUUID());
        if (avatar == null) return;
        SillyAPI silly = ((AvatarExtensions) avatar).silly$getSilly();
        if (silly == null || silly.sprintParticles) return;
        cir.setReturnValue(false);
    }
}
