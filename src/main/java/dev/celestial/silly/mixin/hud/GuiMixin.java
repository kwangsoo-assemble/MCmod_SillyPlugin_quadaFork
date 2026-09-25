package dev.celestial.silly.mixin.hud;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.celestial.silly.SillyEnums;
import dev.celestial.silly.SillyPlugin;
//? if >=1.21 {
import net.minecraft.client.DeltaTracker;
//?}
//? if >=1.21.6 {
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.gui.contextualbar.ExperienceBarRenderer;
import net.minecraft.client.gui.contextualbar.JumpableVehicleBarRenderer;
//?}
import dev.celestial.silly.lua.LuaGraphicsAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.player.Player;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.math.matrix.FiguraMat4;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Unique
    private static void silly$cancelIfHidden(SillyEnums.GUI_ELEMENT el, CallbackInfo ci) {
        if (SillyPlugin.shouldHide(el)) ci.cancel();
    }

    @WrapMethod(method = "render")
    //? if >=1.21 {
    public void render(GuiGraphics guiGraphics, DeltaTracker delta, Operation<Void> original) {
    //?} else {
    /*public void render(GuiGraphics guiGraphics, float delta, Operation<Void> original) {
    *///?}
        if (SillyPlugin.hostInstance != null && !AvatarManager.panic) {
            if (SillyPlugin.hostInstance.guiMat.isOverridden()) {
                FiguraMat4 value = SillyPlugin.hostInstance.guiMat.getValue();
                assert value != null;
                //? if >=1.21.6 {
                // 1.21.6+ GUI 포즈는 2D(Matrix3x2f) — 4x4 행렬의 2D 아핀 성분만 적용 (3D 기울임은 불가)
                guiGraphics.pose().pushMatrix();
                org.joml.Matrix4f silly$mat = value.toMatrix4f();
                guiGraphics.pose().mul(new org.joml.Matrix3x2f(
                        silly$mat.m00(), silly$mat.m01(), silly$mat.m10(), silly$mat.m11(), silly$mat.m30(), silly$mat.m31()));
                original.call(guiGraphics, delta);
                //?} else if >=1.21 {
                /*guiGraphics.pose().pushPose();
                guiGraphics.pose().mulPose(value.toMatrix4f());
                original.call(guiGraphics, delta);
                *///?} else {
                /*guiGraphics.pose().pushPose();
                guiGraphics.pose().mulPoseMatrix(value.toMatrix4f());
                original.call(guiGraphics, delta);
                *///?}
                Entity entity = Minecraft.getInstance().getCameraEntity();
                Avatar avatar = entity == null ? null : AvatarManager.getAvatar(entity);
                if (avatar != null && avatar.luaRuntime != null) {
                    try (LuaGraphicsAPI lg = new LuaGraphicsAPI(guiGraphics)) {
                        //? if >=1.21 {
                        avatar.run("GUI_RENDER", avatar.render, lg, delta.getGameTimeDeltaPartialTick(false));
                        //?} else {
                        /*avatar.run("GUI_RENDER", avatar.render, lg, delta);
                         *///?}
                    } catch (Exception e) {
                        avatar.luaRuntime.error(e);
                    }
                }

                //? if >=1.21.6 {
                guiGraphics.pose().popMatrix();
                //?} else {
                /*guiGraphics.pose().popPose();
                *///?}
                return;
            }
        }
        original.call(guiGraphics, delta);
        if (!AvatarManager.panic) {
            Entity entity = Minecraft.getInstance().getCameraEntity();
            Avatar avatar = entity == null ? null : AvatarManager.getAvatar(entity);
            if (avatar != null && avatar.luaRuntime != null) {
                try (LuaGraphicsAPI lg = new LuaGraphicsAPI(guiGraphics)) {
                    //? if >=1.21 {
                    avatar.run("GUI_RENDER", avatar.render, lg, delta.getGameTimeDeltaPartialTick(false));
                    //?} else {
                    /*avatar.run("GUI_RENDER", avatar.render, lg, delta);
                     *///?}
                } catch (Exception e) {
                    avatar.luaRuntime.error(e);
                }
            }
        }
    }

    //? if >=1.21 {
    @Inject(method="renderHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    public void renderHotbarMixin(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) { 
    //?} else if >=1.20.5 {
    /*@Inject(method="renderHotbarAndDecorations", at = @At("HEAD"), cancellable = true)
    public void renderHotbarMixin(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) { */
    //?} else {
    /*@Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    public void renderHotbarMixin(float f, GuiGraphics guiGraphics, CallbackInfo ci) {
    *///?}
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.HOTBAR, ci);
    }

    //? if <1.21.6 {
    /*@Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    public void renderExperienceBarMixin(GuiGraphics guiGraphics, int i, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.EXPERIENCE_BAR, ci);
    }
    *///?}

    //? if >=1.21 && <1.21.6 {
    /*@Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    public void renderExperienceLevelMixin(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.EXPERIENCE_BAR, ci);
    }
    *///?}

    //? if <1.21.6 {
    /*@Inject(method = "renderJumpMeter", at = @At("HEAD"), cancellable = true)
    public void renderJumpMeterMixin(PlayerRideableJumping playerRideableJumping, GuiGraphics guiGraphics, int i, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.JUMP_METER, ci);
    }
    *///?}

    //? if >=1.21.6 {
    // 1.21.6+: 경험치바/점프미터는 Gui 개별 메서드에서 ContextualBarRenderer 로 이동 —
    // renderHotbarAndDecorations 의 호출 지점을 래핑해 종류별로 숨김 처리
    @Unique
    private static boolean silly$shouldHideContextualBar(ContextualBarRenderer bar) {
        if (bar instanceof ExperienceBarRenderer) return SillyPlugin.shouldHide(SillyEnums.GUI_ELEMENT.EXPERIENCE_BAR);
        if (bar instanceof JumpableVehicleBarRenderer) return SillyPlugin.shouldHide(SillyEnums.GUI_ELEMENT.JUMP_METER);
        return false;
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    public void contextualBarBackgroundWrap(ContextualBarRenderer instance, GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (silly$shouldHideContextualBar(instance)) return;
        original.call(instance, guiGraphics, deltaTracker);
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    public void contextualBarRenderWrap(ContextualBarRenderer instance, GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (silly$shouldHideContextualBar(instance)) return;
        original.call(instance, guiGraphics, deltaTracker);
    }

    @WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"))
    public void experienceLevelWrap(GuiGraphics guiGraphics, Font font, int level, Operation<Void> original) {
        if (SillyPlugin.shouldHide(SillyEnums.GUI_ELEMENT.EXPERIENCE_BAR)) return;
        original.call(guiGraphics, font, level);
    }
    //?}

    @Inject(method = "renderSelectedItemName", at = @At("HEAD"), cancellable = true)
    public void renderSelectedItemNameMixin(GuiGraphics guiGraphics, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.SELECTED_ITEM_NAME, ci);
    }

    @Inject(method = "renderHearts", at = @At("HEAD"), cancellable = true)
    public void renderPlayerHealthMixin(GuiGraphics guiGraphics, Player player, int i, int j, int k, int l, float f, int m, int n, int o, boolean bl, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.PLAYER_HEALTH, ci);
    }
    //? if neoforge && >=1.21 {
    /*@Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void renderArmorMixin(GuiGraphics p_335393_, Player p_335672_, int p_335452_, int p_335846_, int p_335778_, int p_335859_, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.ARMOR_BAR, ci);
    }
    *///?} else if >=1.21 {
    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void renderArmorMixin(GuiGraphics p_335393_, Player p_335672_, int p_335452_, int p_335846_, int p_335778_, int p_335859_, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.ARMOR_BAR, ci);
    }
    //?} else {
    /*@WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getArmorValue()I"))
    public int renderArmorMixin(Player instance, Operation<Integer> original) {
        return SillyPlugin.shouldHide(SillyEnums.GUI_ELEMENT.ARMOR_BAR) ? 0 : original.call(instance);
    }
     *///?}

    //? if neoforge && >=1.21 {
    /*@WrapOperation(method = "renderFoodLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"))
    *///?} else {
    @WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;getVehicleMaxHearts(Lnet/minecraft/world/entity/LivingEntity;)I"))
    //?}
    public int renderPlayerHealthMixin(Gui instance, LivingEntity i, Operation<Integer> original) {
        if (SillyPlugin.shouldHide(SillyEnums.GUI_ELEMENT.PLAYER_HUNGER)) return 1;
        return original.call(instance, i);
    }

    @Inject(method = "renderVehicleHealth", at = @At("HEAD"), cancellable = true)
    public void renderVehicleHealthMixin(GuiGraphics guiGraphics, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.VEHICLE_HEALTH, ci);
    }

    @Inject(method = "renderVignette", at = @At("HEAD"), cancellable = true)
    public void renderVignetteMixin(GuiGraphics guiGraphics, Entity entity, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.VIGNETTE, ci);
    }

    @Inject(method = "renderSpyglassOverlay", at = @At("HEAD"), cancellable = true)
    public void renderSpyglassOverlayMixin(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.SPYGLASS_OVERLAY, ci);
    }

    @Inject(method = "renderTextureOverlay", at = @At("HEAD"), cancellable = true)
    public void renderTextureOverlayMixin(GuiGraphics guiGraphics, ResourceLocation resourceLocation, float f, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.TEXTURE_OVERLAY, ci);
    }

    @Inject(method = "renderPortalOverlay", at = @At("HEAD"), cancellable = true)
    public void renderPortalOverlayMixin(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.PORTAL_OVERLAY, ci);
    }



    @Inject(method = "renderEffects", at = @At("HEAD"), cancellable = true)
    //? if >=1.21 {
    public void renderEffectsMixin(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
    //?} else {
    /*public void renderEffectsMixin(GuiGraphics guiGraphics, CallbackInfo ci) {
    *///?}
        silly$cancelIfHidden(SillyEnums.GUI_ELEMENT.EFFECTS, ci);
    }
}
