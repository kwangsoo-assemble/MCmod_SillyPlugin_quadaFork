package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.celestial.silly.helper.SillyBlockHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelHeightAccessor.class)
public interface ClientLevelMixin {
//    @WrapMethod(method = "isOutsideBuildHeight(I)Z")
//    default boolean silly$unlock(int i, Operation<Boolean> original) {
//        if (SillyBlockHandler.UNLOCK_HEIGHT)
//            return false;
//        return original.call(i);
//    }
}
