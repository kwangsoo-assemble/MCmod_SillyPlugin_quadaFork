package dev.celestial.silly.mixin;

import dev.celestial.silly.SillyPermissions;
import dev.celestial.silly.SillyPlugin;
import org.figuramc.figura.permissions.PermissionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = PermissionManager.class, remap = false)
public class PermissionManagerMixin {
    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lorg/figuramc/figura/utils/IOUtils;readCacheFile(Ljava/lang/String;Ljava/util/function/Consumer;)V"))
    private static void silly$addPermissions(CallbackInfo ci) {
        PermissionManager.CUSTOM_PERMISSIONS.put("sillyplugin", SillyPermissions.PERMISSIONS);
    }
}
