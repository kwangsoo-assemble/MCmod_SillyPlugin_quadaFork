package dev.celestial.silly.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.celestial.silly.SillyPlugin;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
//? if >=1.21.4 {
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
//?}
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.lua.api.world.WorldAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=1.21.4 {
@Mixin(LivingEntityRenderer.class)
public class PlayerRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
    public void silly$hidePlayers(S livingEntityRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (livingEntityRenderState instanceof PlayerRenderState prs) {
            // maybe this is problematic...?
            // not sure how else to do it. maybe attaching something to PlayerRenderState?
            var ent = WorldAPI.getCurrentWorld().getEntity(prs.id);
            if (ent == null) return; // hm
            if (ent instanceof AbstractClientPlayer plr) {
                var hsilly = SillyPlugin.hostInstance;
                if (hsilly != null && !AvatarManager.panic) {
                    // potential CM exception?
                    if (hsilly.hiddenPlayers.contains(plr.getStringUUID()) || hsilly.hiddenPlayers.contains(plr.getGameProfile().getName())) {
                        ci.cancel();
                    }
                }
            }
        }
    }
}//?} else {
/*@Mixin(LivingEntityRenderer.class)
public class PlayerRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
    public void silly$hidePlayers(LivingEntity livingEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        if (livingEntity instanceof AbstractClientPlayer plr) {
            var hsilly = SillyPlugin.hostInstance;
            if (hsilly != null && !AvatarManager.panic) {
                // potential CM exception?
                if (hsilly.hiddenPlayers.contains(plr.getStringUUID()) || hsilly.hiddenPlayers.contains(plr.getGameProfile().getName())) {
                    ci.cancel();
                }
            }
        }
    }
}
*///?}