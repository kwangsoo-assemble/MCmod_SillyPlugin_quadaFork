package dev.celestial.silly;

import dev.celestial.silly.helper.SillyBlockHandler;
import dev.celestial.silly.loaders.ISillyLoader;
import dev.celestial.silly.lua.SillyAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.font.Emojis;
import org.figuramc.figura.permissions.PermissionManager;
import org.figuramc.figura.permissions.Permissions;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SillyPlugin {
    public static final String MOD_ID = "sillyplugin";
    public static final Color COLOR = new Color(151,79,219);
    public static Logger LOGGER = LoggerFactory.getLogger("SillyPlugin");
    public static ISillyLoader Loader;
    @Nullable
    public static SillyAPI hostInstance;

    public static boolean shouldHide(SillyEnums.GUI_ELEMENT el) {
        if (hostInstance == null) return false;
        if (AvatarManager.panic) return false;
        return hostInstance.disabledElements.contains(el);
    }

    public static boolean fakeExistsAt(BlockPos pos) {
        synchronized (SillyBlockHandler.BLOCKS) {
            return SillyBlockHandler.BLOCKS.containsKey(pos);
        }
    }

    public static boolean shouldNoclip(Entity entity) {
        if (hostInstance == null) return false;
        if (AvatarManager.panic) return false;
        if (entity instanceof Player plr) {
            if (plr.isLocalPlayer() || (plr.getServer() != null && !plr.getServer().isDedicatedServer()))
                return hostInstance.noclip && hostInstance.cheatsEnabled();
        }
        return false;
    }

    public static void initialize(ISillyLoader loader) {
        Loader = loader;
        SillySettings.classload();
    }
}
