package dev.celestial.silly.mixin;

import dev.celestial.silly.SillyPlugin;
import dev.celestial.silly.SillySettings;
import org.figuramc.figura.lua.api.ClientAPI;
import org.figuramc.figura.utils.Version;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.regex.Pattern;

@Mixin(value = ClientAPI.class, remap = false)
public class ClientAPIMixin {
    @Unique
    private static Pattern silly$lenientPattern = Pattern.compile(
            "^(?<major>0|[1-9]\\d*)(?:\\.(?<minor>0|[1-9]\\d*)(?:\\.(?<patch>0|[1-9]\\d*))?)?.*"
    );

    @Inject(method = "getFiguraVersion", at = @At("RETURN"), cancellable = true)
    private static void silly$patchOutTheEvilLetterB(CallbackInfoReturnable<String> cir) {
        if (!SillySettings.LENIENT_MODE.getBool()) return;
        var ret = cir.getReturnValue();
        if ((new Version(ret)).invalid) {
            // we have another 0.1.5b incident here.
            // do our very best to fix it up...
            var matcher = silly$lenientPattern.matcher(ret);
            if (matcher.matches()) {
                var finalVersion = matcher.group("major");
                if (matcher.group("minor") != null)
                    finalVersion += "." + matcher.group("minor");
                if (matcher.group("patch") != null)
                    finalVersion += "." + matcher.group("patch");
                cir.setReturnValue(finalVersion);
            } else {
                // if it doesn't match, we've failed our mission.
                SillyPlugin.LOGGER.error("Failed to fix version {}!", ret);
            }

        }
    }
}
