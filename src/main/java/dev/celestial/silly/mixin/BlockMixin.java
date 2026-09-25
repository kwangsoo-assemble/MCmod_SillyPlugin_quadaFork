package dev.celestial.silly.mixin;

import dev.celestial.silly.SillyPlugin;
import dev.celestial.silly.lua.SillyAPI;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.figuramc.figura.avatar.AvatarManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(method = "getFriction", at = @At("HEAD"), cancellable = true)
    public void getFrictionMixin(CallbackInfoReturnable<Float> cir) {
        if (SillyPlugin.hostInstance != null) {
            if (AvatarManager.panic) return;
            SillyAPI api = SillyPlugin.hostInstance;
            if (!api.cheatsEnabled()) return;
            if (!api.frictionValue.isOverridden()) return;
            cir.setReturnValue(api.frictionValue.getValue());
        }
    }
}
