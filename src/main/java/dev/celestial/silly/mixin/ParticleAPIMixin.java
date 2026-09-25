package dev.celestial.silly.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.celestial.silly.SillySettings;
import net.minecraft.core.particles.ParticleOptions;
import org.figuramc.figura.lua.api.particle.LuaParticle;
import org.figuramc.figura.lua.api.particle.ParticleAPI;
import org.luaj.vm2.LuaError;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ParticleAPI.class, remap = false)
public class ParticleAPIMixin {
    @WrapMethod(method = "generate")
    public <T extends ParticleOptions> LuaParticle silly$lenientMode(String id, double x, double y, double z, double w, double t, double h, Operation<LuaParticle> original) {
        try {
            return original.call(id, x, y, z, w, t, h);
        } catch (LuaError e) {
            if (SillySettings.LENIENT_MODE.getBool() &&
                    (e.getMessage().contains("Could not parse") || e.getMessage().contains("Unknown particle"))
                ) {
                return original.call("end_rod", x, y, z, w, t, h);
            }
            throw e;
        }
    }
}
