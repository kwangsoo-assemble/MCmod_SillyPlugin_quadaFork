package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.celestial.silly.helper.HandBob;
import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.not_a_mixin.AvatarExtensions;
//? if >1.21 {
import net.minecraft.client.DeltaTracker;
//?}

import net.minecraft.client.renderer.GameRenderer;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "render", at = @At("HEAD"))
    //? if >1.21 {
    private void silly$prerenderEvent(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci) {
    //?} else {
    /*private void silly$prerenderEvent(float f, long l, boolean bl, CallbackInfo ci) {
    *///?}
        if (AvatarManager.panic) return;
        AvatarManager.executeAll("preRenderBackport", a -> {
            SillyAPI silly = ((AvatarExtensions)a).silly$getSilly();
            if (silly == null) return;
            if (a.luaRuntime == null) return;
            if (a.luaRuntime.getUser() == null) return;
            //? if >1.21 {
            a.run(silly.backports.PRE_RENDER, a.render, deltaTracker.getGameTimeDeltaPartialTick(bl), a.renderMode.name());
            //?} else {
            /*a.run(silly.backports.PRE_RENDER, a.render, f, a.renderMode.name());
            *///?}

        });
    }

    // ★ silly:setHandBobScale (2026-11-23, 하네스 mc_repositories M67) — 걸음 흔들림 진폭(lerp(oBob, bob))에 배율.
    //   bobView 의 이동 · Z 회전 · X 회전이 모두 이 진폭 하나에 비례해 함께 준다.
    //   호출 자리가 아니라 메서드 안에서 바꾼다 — 손 패스를 누가 그리든(바닐라 renderItemInHand · 셰이더팩 손 경로) 같은 배율이 걸리게.
    //   카메라 쪽 호출(renderLevel)만 아래 감싸기로 뺀다.
    @ModifyExpressionValue(method = "bobView", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F"))
    private float silly$scaleHandBob(float bob) {
        if (HandBob.cameraPass) return bob;
        return bob * HandBob.bobScale();
    }

    // 카메라(월드 투영) 흔들림은 바닐라 그대로 — renderLevel 의 bobView 호출 동안만 표시를 켠다.
    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    private void silly$cameraBob(GameRenderer instance, PoseStack poseStack, float partialTick, Operation<Void> original) {
        HandBob.cameraPass = true;
        try {
            original.call(instance, poseStack, partialTick);
        } finally {
            HandBob.cameraPass = false;
        }
    }
}
