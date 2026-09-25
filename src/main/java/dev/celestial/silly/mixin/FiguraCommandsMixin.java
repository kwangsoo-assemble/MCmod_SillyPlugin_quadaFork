package dev.celestial.silly.mixin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.celestial.silly.SillyCommands;
import org.figuramc.figura.commands.FiguraCommands;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hangs the SillyPlugin subcommands off the {@code /figura} tree.
 *
 * <p>The root builder is returned fully assembled, so appending to it here is enough - no change
 * to the Figura fork is needed for the commands themselves.
 */
@Mixin(value = FiguraCommands.class, remap = false)
public class FiguraCommandsMixin {

    @Inject(method = "getCommandRoot", at = @At("RETURN"))
    private static void silly$addCommands(
            CallbackInfoReturnable<LiteralArgumentBuilder<FiguraClientCommandSource>> cir) {
        SillyCommands.register(cir.getReturnValue());
    }
}
