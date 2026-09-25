package dev.celestial.silly;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.network.chat.Component;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.backend2.NetworkStuff;
import org.figuramc.figura.gui.FiguraToast;
import org.figuramc.figura.utils.FiguraClientCommandSource;
import org.figuramc.figura.utils.FiguraText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Extra {@code /figura} subcommands contributed by SillyPlugin.
 *
 * <p>These live here rather than in the Figura fork on purpose: everything they need is already
 * public API on the fork side, so keeping them out of it avoids widening the diff that has to be
 * carried across upstream updates.
 *
 * <pre>
 *   /figura upload &lt;main|server|both&gt;      upload the avatar you are wearing
 *   /figura reload [self|nonhost|all]      scope arguments for the fork's reload command
 * </pre>
 *
 * <p><b>{@code /figura nonhost_load} moved to the fork on 2026-09-22</b> (fork
 * {@code commands/NonhostLoadCommand}, with {@code nonhost_unload} and {@code netlock}).
 * The version that lived here cleared the target and compiled the folder onto it, but could not
 * put the fork's private "already fetched" mark back, so the next render frame fetched the cloud
 * avatar and replaced the local one. The fix needs that mark plus a per-user pin that every
 * cloud/FSB entry point honours — all private state of the fork, so the command lives beside
 * {@code /figura load} now. <b>Do not re-add it here:</b> Brigadier merges same-named literals and
 * the later registration (this one) would win, bringing the overwrite bug back.
 */
public class SillyCommands {

    public static void register(LiteralArgumentBuilder<FiguraClientCommandSource> root) {
        root.then(upload());
        extendReload(root);
    }

    // -- /figura reload [self|nonhost|all] -------------------------------------------------

    /**
     * Adds scope arguments to the reload command that Figura already registered.
     *
     * <p>The node is a finished {@link CommandNode} by the time the root builder comes back, but
     * Brigadier lets children be attached to one, so the extra forms go on without touching the
     * fork. Bare {@code /figura reload} keeps its old meaning (reload yourself).
     */
    private static void extendReload(LiteralArgumentBuilder<FiguraClientCommandSource> root) {
        CommandNode<FiguraClientCommandSource> reload = null;
        for (CommandNode<FiguraClientCommandSource> child: root.getArguments()) {
            if ("reload".equals(child.getName())) { reload = child; break; }
        }
        if (reload == null) return;   // upstream renamed or dropped it; leave things alone

        reload.addChild(LiteralArgumentBuilder.<FiguraClientCommandSource>literal("self")
                .executes(ctx -> reloadSelf(ctx.getSource())).build());
        reload.addChild(LiteralArgumentBuilder.<FiguraClientCommandSource>literal("nonhost")
                .executes(ctx -> reloadNonhost(ctx.getSource())).build());
        reload.addChild(LiteralArgumentBuilder.<FiguraClientCommandSource>literal("all")
                .executes(ctx -> reloadAll(ctx.getSource())).build());
    }

    private static int reloadSelf(FiguraClientCommandSource source) {
        AvatarManager.reloadAvatar(FiguraMod.getLocalPlayerUUID());
        FiguraToast.sendToast(FiguraText.of("toast.reload"));
        return 1;
    }

    /**
     * Everyone except the local player, so your own avatar is not interrupted.
     *
     * <p>Avatars pinned with {@code /figura nonhost_load} come back from their pinned folder
     * (the fork restores them on the next fetch); under {@code /figura netlock} the others stay
     * cleared, because the lock means "nothing more from the network".
     */
    private static int reloadNonhost(FiguraClientCommandSource source) {
        UUID self = FiguraMod.getLocalPlayerUUID();
        List<UUID> targets = new ArrayList<>();
        for (Avatar avatar: AvatarManager.getLoadedAvatars()) {
            if (avatar.owner != null && !avatar.owner.equals(self)) targets.add(avatar.owner);
        }
        // Collected first: clearing mutates the same table the list came from.
        for (UUID uuid: targets) AvatarManager.clearAvatars(uuid);
        source.figura$sendFeedback(Component.literal("Reloaded " + targets.size() + " non-host avatar(s)"));
        return targets.size();
    }

    private static int reloadAll(FiguraClientCommandSource source) {
        AvatarManager.clearAllAvatars();
        FiguraToast.sendToast(FiguraText.of("toast.reload_all"));
        return 1;
    }

    // -- /figura upload ------------------------------------------------------------------

    private static LiteralArgumentBuilder<FiguraClientCommandSource> upload() {
        LiteralArgumentBuilder<FiguraClientCommandSource> node =
                LiteralArgumentBuilder.literal("upload");
        node.then(destination("main", NetworkStuff.Destination.BACKEND));
        node.then(destination("server", NetworkStuff.Destination.FSB));
        node.then(destination("both", NetworkStuff.Destination.BOTH));
        return node;
    }

    private static LiteralArgumentBuilder<FiguraClientCommandSource> destination(
            String name, NetworkStuff.Destination destination) {
        LiteralArgumentBuilder<FiguraClientCommandSource> node = LiteralArgumentBuilder.literal(name);
        node.executes(ctx -> runUpload(ctx, destination));
        return node;
    }

    private static int runUpload(CommandContext<FiguraClientCommandSource> context,
                                 NetworkStuff.Destination destination) {
        FiguraClientCommandSource source = context.getSource();
        Avatar avatar = AvatarManager.getLoadedAvatar(FiguraMod.getLocalPlayerUUID());
        if (avatar == null || avatar.nbt == null) {
            source.figura$sendError(Component.literal("No avatar equipped."));
            return 0;
        }
        try {
            NetworkStuff.uploadAvatar(avatar, destination);
            source.figura$sendFeedback(Component.literal(
                    "Uploading avatar to " + destination.name().toLowerCase(Locale.ROOT)));
            return 1;
        } catch (Exception e) {
            source.figura$sendError(Component.literal("Upload failed: " + e.getMessage()));
            return 0;
        }
    }
}
