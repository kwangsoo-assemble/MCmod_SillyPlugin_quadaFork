package dev.celestial.silly.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes the protected render-state API of {@link PlayerRenderer}.
 *
 * <p>Why this is needed: <b>in first person, Figura's {@code vanilla_model} holds the first-person hand pose,
 * not the third-person pose.</b> This happens in two places:
 * <ul>
 *   <li>{@code LivingEntityRendererMixin.preRender} calls {@code ci.cancel()} <b>before</b>
 *       {@code part.save(model)} when {@code Avatar.firstPerson} is set, so no third-person snapshot is taken.</li>
 *   <li>{@code PlayerRendererMixin.onRenderHand} calls {@code part.save(model)} during the first-person hand
 *       render, and that model is <b>set up to draw a single arm only</b>.</li>
 * </ul>
 * So when an avatar tries to read the local player's walk swing, it gets frozen values in first person.
 *
 * <p>This invoker lets {@code ThirdPersonPose} <b>keep its own separate model</b> and run {@code setupAnim}
 * from the third-person point of view. The model used by the renderer is never touched.
 *
 * <p>Warning: if these names change, the build breaks (which is better than silently doing the wrong thing).
 * When moving to a new fork version, confirm with {@code javap -p} that these methods still exist.
 */
@Mixin(PlayerRenderer.class)
public interface PlayerRendererInvoker {

    /** Creates a new render-state object (a separate instance from the one the renderer uses every frame). */
    @Invoker("createRenderState")
    PlayerRenderState silly$createRenderState();

    /** Extracts the player's current state into a render state (walk phase, attack time, pose, etc.). */
    @Invoker("extractRenderState")
    void silly$extractRenderState(AbstractClientPlayer player, PlayerRenderState state, float partialTick);
}
