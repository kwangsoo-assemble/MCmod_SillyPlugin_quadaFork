package dev.celestial.silly.mixin;

import dev.celestial.silly.helper.HandBob;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

// ★ silly:setHandSwayScale (2026-11-23, 하네스 mc_repositories M67) — 1인칭 손 패스의 시선 지연 흔들림에 배율.
//   renderHandsWithItems 머리의 rotationDegrees 둘 = (시선 X − xBob) × 0.1 · (시선 Y − yBob) × 0.1 (1.21.8 javap — 메서드 안 첫 두 호출).
//   코어 FirstPerson 파츠는 그 뒤 mulPose ordinal 1 AFTER 에서 그려져 같이 따른다(코어 CLAUDE.md 이력 22).
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {
    @ModifyArg(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lcom/mojang/math/Axis;rotationDegrees(F)Lorg/joml/Quaternionf;", ordinal = 0))
    private float silly$scaleSwayX(float degrees) {
        return degrees * HandBob.swayScale();
    }

    @ModifyArg(method = "renderHandsWithItems", at = @At(value = "INVOKE", target = "Lcom/mojang/math/Axis;rotationDegrees(F)Lorg/joml/Quaternionf;", ordinal = 1))
    private float silly$scaleSwayY(float degrees) {
        return degrees * HandBob.swayScale();
    }
}
