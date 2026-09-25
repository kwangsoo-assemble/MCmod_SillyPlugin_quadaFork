package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.celestial.silly.SillySettings;
import net.minecraft.world.item.ItemStack;
import org.figuramc.figura.lua.api.world.ItemStackAPI;
import org.figuramc.figura.utils.LuaUtils;
import org.luaj.vm2.LuaError;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = LuaUtils.class, remap = false)
public class LuaUtilsMixin {
    //? if >=1.20.5 {
    @WrapMethod(method = "parseItemStackMap")
    private static ItemStackAPI silly$lenientMode(String methodName, Object item, Operation<ItemStackAPI> original) {
    //?} else {
    /*@WrapMethod(method = "parseItemStack")
    private static ItemStack silly$lenientMode(String methodName, Object item, Operation<ItemStack> original) {
    *///?}
        try {
            return original.call(methodName, item);
        } catch (LuaError e) {
            if (SillySettings.LENIENT_MODE.getBool() && e.getMessage().contains("Could not parse"))
                //? if >=1.20.5 {
                return new ItemStackAPI(ItemStack.EMPTY);
                //?} else {
                /*return ItemStack.EMPTY;
                *///?}

            throw e;
        }
    }
}
