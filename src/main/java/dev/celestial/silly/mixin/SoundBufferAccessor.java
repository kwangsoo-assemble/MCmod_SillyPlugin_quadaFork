package dev.celestial.silly.mixin;

import com.mojang.blaze3d.audio.SoundBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.sound.sampled.AudioFormat;

@Mixin(SoundBuffer.class)
public interface SoundBufferAccessor {
    @Accessor("format")
    public AudioFormat silly$getFormat();
}
