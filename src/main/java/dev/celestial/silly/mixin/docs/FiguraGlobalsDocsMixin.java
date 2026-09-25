package dev.celestial.silly.mixin.docs;

import dev.celestial.silly.lua.SillyAPI;
import org.figuramc.figura.lua.docs.FiguraGlobalsDocs;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = FiguraGlobalsDocs.class, remap = false)
public class FiguraGlobalsDocsMixin {
    @Unique
    @LuaFieldDoc("globals.silly")
    public SillyAPI silly;
}
