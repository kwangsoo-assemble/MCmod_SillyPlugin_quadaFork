package dev.celestial.silly.mixin;

import dev.celestial.silly.SillySettings;
import net.minecraft.client.Minecraft;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.utils.FiguraText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Polls the SillyPlugin keybinds once per client tick.
 *
 * <p>Sits alongside Figura's own handling in its {@code MinecraftMixin}: the same tick method,
 * the same {@code consumeClick} pattern. Kept in a mixin of its own so the Figura fork stays
 * untouched.
 */
@Mixin(Minecraft.class)
public class SillyKeybindMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void silly$pollKeybinds(CallbackInfo ci) {
        // Same action as the wardrobe permissions tab button: drop every loaded avatar so they
        // all get fetched again. Figura's own reload key only covers the local player.
        if (SillySettings.RELOAD_ALL_BUTTON.keyBind.consumeClick()) {
            AvatarManager.clearAllAvatars();
            FiguraToast.sendToast(FiguraText.of("toast.reload_all"));
        }
    }
}
