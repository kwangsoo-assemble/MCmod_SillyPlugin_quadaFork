package dev.celestial.silly;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import dev.celestial.silly.lua.SillyAPI;
import dev.celestial.silly.mixin.MinecraftAccessor;
import dev.celestial.silly.not_a_mixin.AvatarExtensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Services;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.entity.player.Player;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.mixin.render.TextureAtlasAccessor;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class SillyUtil {
    public static final boolean DEV_MODE = true;
    public static Services services;
    public static Avatar getAvatar(String username) {
        UUID uuid = getUUID(username);

        if (uuid == null) {
            return null;
        }

        return AvatarManager.getAvatarForPlayer(uuid);
    }

    public static void logAsAvatar(Avatar avatar, Consumer<String> logFn, String msg) {
        logFn.accept("[" + avatar.entityName + "] " + msg);
    }

    // figura TextureAPI.fromVanilla
    public static NativeImage getImage(ResourceLocation loc) {
        try {
            //? if >=1.21.5 {
            // 1.21.5+ GPU 파이프라인: 텍스처 다운로드는 커맨드 인코더 readback으로 대체 (Figura TextureAPI.fromVanilla 방식)
            TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(loc);
            com.mojang.blaze3d.textures.GpuTexture atlasGpuTexture = atlas.getTexture();
            TextureAtlasAccessor atlasAccessor = (TextureAtlasAccessor) atlas;
            int width = atlasAccessor.figura$getWidth();
            int height = atlasAccessor.figura$getHeight();
            NativeImage nativeImage = new NativeImage(width, height, false);
            com.mojang.blaze3d.systems.CommandEncoder encoder = com.mojang.blaze3d.systems.RenderSystem.getDevice().createCommandEncoder();
            com.mojang.blaze3d.buffers.GpuBuffer gpuBuffer = com.mojang.blaze3d.systems.RenderSystem.getDevice()
                    .createBuffer(() -> "SillyPlugin Atlas Read Buffer", 9, width * height * atlasGpuTexture.getFormat().pixelSize());
            encoder.copyTextureToBuffer(atlasGpuTexture, gpuBuffer, 0, () -> {
                try (com.mojang.blaze3d.buffers.GpuBuffer.MappedView readView = encoder.mapBuffer(gpuBuffer, true, false)) {
                    for (int k = 0; k < height; k++) {
                        for (int l = 0; l < width; l++) {
                            int m = readView.data().getInt((l + k * width) * atlasGpuTexture.getFormat().pixelSize());
                            nativeImage.setPixelABGR(l, height - k - 1, m | 0xFF000000);
                        }
                    }
                }
                gpuBuffer.close();
            }, 0);
            return nativeImage;
            //?} else {
            /*TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(loc);
            atlas.bind();
            TextureAtlasAccessor atlasAccessor = (TextureAtlasAccessor) atlas;
            NativeImage nativeImage = new NativeImage(atlasAccessor.getWidth(), atlasAccessor.getHeight(), false);
            nativeImage.downloadTexture(0, false);
            return nativeImage;
            *///?}
        } catch (Exception ignored) {}
        try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(loc);
            if (resource.isPresent())
                return NativeImage.read(resource.get().open());
        } catch (Exception e) {
            throw new LuaError(e.getMessage());
        }
        try {
            AbstractTexture tx = Minecraft.getInstance().getTextureManager().getTexture(loc);

            NativeImage img;
            if (tx instanceof SimpleTexture stx) {
                //? if >1.21.1 {
                TextureContents conts = stx.loadContents(Minecraft.getInstance().getResourceManager());
                img = conts.image();
                //?} else {
                /*SimpleTexture.TextureImage txImg = stx.getTextureImage(Minecraft.getInstance().getResourceManager());
                img = txImg.getImage();
                *///?}
            } else if (tx instanceof DynamicTexture dtx) {
                img = dtx.getPixels();
            } else {
                throw new RuntimeException("No handling for " + tx.getClass().getSimpleName() + " added in SillyUtil.getImage!");
            }
            assert img != null;
            return img.mappedCopy(i -> i);
        } catch (Exception e) {
            throw new LuaError(e.getMessage());
        }
    }

    public static UUID getUUID(String username) {
        Minecraft mc = Minecraft.getInstance();
        if (SillyUtil.services == null) {
            SillyUtil.services = Services.create(((MinecraftAccessor)mc).silly$getAuthenticationService(), mc.gameDirectory);
        }
        UUID uuid = null;
        if (mc.level == null) return null;

        try {
            uuid = UUID.fromString(username);
        } catch (IllegalArgumentException ignored) {
            assert mc.level != null;
            List<AbstractClientPlayer> players = mc.level.players();
            for (Player current : players) {
                if (current.getName().getString().equals(username)) {
                    uuid = current.getUUID();
                    break;
                }
            }

            if (uuid == null) {
                Optional<GameProfile> profile = services.profileCache().get(username);
                if (profile.isPresent())
                    uuid = profile.get().getId();
            }
        }
        return uuid;
    }

    public static UUID clientUUID() {
        return FiguraMod.getLocalPlayerUUID();
    }

    public static void Devlog(String message, Object... vars) {
        if (DEV_MODE && SillySettings.DEV_LOGS.getBool())
            SillyPlugin.LOGGER.info(message, vars);
    }

    private static Class<?> roltClass;
    public static LuaTable createReadOnlyLuaTable(LuaTable tbl) {
        if (roltClass != null) {
            try {
                return (LuaTable) roltClass.getConstructor(LuaValue.class).newInstance(tbl);
            } catch (Exception e) {
                throw new LuaError(e);
            }
        }
        try {
            roltClass = Class.forName("org.figuramc.figura.lua.ReadOnlyLuaTable");
        } catch (Exception e) {
            try {
                roltClass = Class.forName("org.figuramc.figura.lua.transfer.ReadOnlyLuaTable");
            } catch (ClassNotFoundException ex) {
                throw new LuaError("Could not create ReadOnlyLuaTable!");
            }
        }
        try {
            return (LuaTable) roltClass.getConstructor(LuaValue.class).newInstance(tbl);
        } catch (Exception e) {
            throw new LuaError(e);
        }
    }

    public static boolean canCheat(SillyAPI api) {
        if (AvatarManager.panic) return false;
        if (api == null) return false;
        return api.cheatsEnabled();
    }

    public static boolean canCheat(Avatar avatar) {
        SillyAPI api = ((AvatarExtensions)avatar).silly$getSilly();
        return canCheat(api);
    }

    public static boolean canCheat() {
        return canCheat(SillyPlugin.hostInstance);
    }

    public static ResourceLocation location(String namespace, String path) {
        //? if >=1.21 {
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(namespace, path);
        //?} else {
        /*ResourceLocation loc = ResourceLocation.tryBuild(namespace, path);
         *///?}
        return loc;
    }
}
