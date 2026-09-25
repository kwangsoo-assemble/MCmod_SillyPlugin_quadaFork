package dev.celestial.silly.helper;

import dev.celestial.silly.SillyPermissions;
import dev.celestial.silly.SillyPlugin;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.permissions.Permissions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SillyBlockHandler {
    public final static Map<BlockPos, SillyBlockContainer> BLOCKS = new ConcurrentHashMap<>();
    public final static Map<BlockPos, SillyBlockContainer> REAL_BLOCKS = new ConcurrentHashMap<>();
    private static final Permissions PERM = SillyPermissions.FAKE_BLOCKS;

    public static void panic(ClientLevel level, boolean state) {
        if (SillyPlugin.hostInstance != null && SillyPlugin.hostInstance.fakeBlocksDisabled)
            state = true;
        setBlocks(level, state ? REAL_BLOCKS : BLOCKS);
    }

    public static void apply(ClientLevel level) {
        if (SillyPlugin.hostInstance != null && SillyPlugin.hostInstance.fakeBlocksDisabled)
            return;
        setBlocks(level, BLOCKS);
    }

    public static void revert(ClientLevel level) {
        setBlocks(level, REAL_BLOCKS);
    }

    public static void setBlocks(ClientLevel level, Map<BlockPos, SillyBlockContainer> blocks) {
        for (var entry : blocks.values()) {
            setBlock(entry, level);
        }
    }

    public static void removeOf(UUID owner, ClientLevel lvl) {
        for (var key : BLOCKS.keySet()) {
            var entry = BLOCKS.get(key);
            if (entry.owner == owner) {
                BLOCKS.remove(key);
                setBlock(REAL_BLOCKS.get(key), lvl);
            }
        }
    }

    public static boolean setBlock(SillyBlockContainer container, ClientLevel level) {
        if (container.owner != null) {
            var owner = AvatarManager.getAvatarForPlayer(container.owner);
            if (owner.permissions.get(PERM) == 0) {
                owner.noPermissions.add(PERM);
                return false;
            } else
                owner.noPermissions.remove(PERM);
        }

        level.setBlock(container.coordinates, container.state, 2);
        if (container.entity != null) {
            level.setBlockEntity(container.entity);
        }
        return true;
    }
}
