package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import dev.celestial.silly.SillyPlugin;
import dev.celestial.silly.lua.SillyAPI;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.figuramc.figura.avatar.AvatarManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends Player {
    //? if >=1.21.5 {
    public LocalPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }
    //?} else {
    /*public LocalPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }
    *///?}

    //? if neoforge && >=1.21 {
    /*@WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;mayFly()Z"))
    public boolean mayFlyMixin(LocalPlayer instance, Operation<Boolean> original) {
        boolean orig = original.call(instance);
        SillyAPI silly = SillyPlugin.hostInstance;
        if (silly == null) return orig;
        if (!silly.mayFly.isOverridden()) return orig;
        if (!silly.cheatsEnabled()) return orig;
        if (AvatarManager.panic) return orig;
        return silly.mayFly.getValue();
    }
    *///?} else {
    @WrapOperation(
            method = "aiStep",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/player/Abilities;mayfly:Z",
                    opcode = Opcodes.GETFIELD
            )
    )
    public boolean getAbilitiesMixin(Abilities instance, Operation<Boolean> original) {
        boolean orig = original.call(instance);
        SillyAPI silly = SillyPlugin.hostInstance;
        if (silly == null) return orig;
        if (!silly.mayFly.isOverridden()) return orig;
        if (!silly.cheatsEnabled()) return orig;
        if (AvatarManager.panic) return orig;
        return silly.mayFly.getValue();
    }
    //?}

    // ★ silly:setSwimAllowed(false) (2026-11-06, 하네스 별의별난연구소_3화) — 바닐라 수영 판정은 «달리기 + 물속» 이고 서버도 같은 판정을 돌린다.
    //   클라 자세만 막으면 남 화면 · 리플레이에는 수영으로 보인다 → 물속 달리기 자체를 막아 서버에 달리기를 안 보낸다.
    //   달리기가 풀리면 updateSwimming 이 수영도 푼다. 수면에서는 달릴 수 있다(물에 잠기지 않으면 수영이 시작되지 않는다).
    @Unique
    private boolean silly$swimBlocked() {
        SillyAPI silly = SillyPlugin.hostInstance;
        if (silly == null || silly.swimAllowed) return false;
        return silly.cheatsEnabled() && !AvatarManager.panic;
    }

    //? if >=1.21.6 {
    @Inject(method = "canStartSprinting", at = @At("RETURN"), cancellable = true)
    private void silly$blockSwimSprintStart(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && (isUnderWater() || isSwimming()) && silly$swimBlocked()) cir.setReturnValue(false);
    }

    @Inject(method = "shouldStopSwimSprinting", at = @At("RETURN"), cancellable = true)
    private void silly$stopSwimSprint(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && silly$swimBlocked()) cir.setReturnValue(true);
    }

    @Inject(method = "shouldStopRunSprinting", at = @At("RETURN"), cancellable = true)
    private void silly$stopUnderwaterSprint(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && isUnderWater() && silly$swimBlocked()) cir.setReturnValue(true);
    }
    //?}
}
