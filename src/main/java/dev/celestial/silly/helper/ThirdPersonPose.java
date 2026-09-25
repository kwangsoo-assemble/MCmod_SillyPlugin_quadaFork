package dev.celestial.silly.helper;

import dev.celestial.silly.mixin.PlayerRendererInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.ParentType;

/**
 * <b>Computes the vanilla pose as it would be in third person, even while in first person.</b>
 *
 * <p>Problem: in first person, Figura's {@code vanilla_model.*:getOriginRot()} <b>does not give the third-person
 * pose.</b> (1) {@code LivingEntityRendererMixin.preRender} calls {@code ci.cancel()} before
 * {@code part.save(model)} when {@code Avatar.firstPerson} is set, and (2) {@code PlayerRendererMixin.onRenderHand}
 * calls {@code part.save(model)} during the first-person hand render (that model is set up to draw a single arm
 * only). As a result, in first person the arms freeze or get mixed with the hand pose.
 *
 * <p>Solution: <b>without touching the renderer's model</b>, bake our own separate {@link PlayerModel} and run
 * {@code setupAnim} on it at most once per frame to read the angles. Because this reuses the vanilla computation,
 * there is no need to copy the formulas (the computation keeps up when the game version changes).
 *
 * <p>Warning: this class is <b>client-only</b>. Do not call it from the server thread.
 * Results are <b>cached per frame</b>: querying several parts in one frame still computes only once.
 */
public final class ThirdPersonPose {

    private ThirdPersonPose() {}

    /** Our own model, never shared with the renderer's; sharing it would break the first-person hand render. */
    private static PlayerModel model;
    private static PlayerModel modelSlim;
    /** Reused render state, refilled every frame. */
    private static PlayerRenderState state;

    /** Frame cache: never compute twice in the same frame. */
    private static long cachedFrame = Long.MIN_VALUE;
    private static boolean cachedOk;

    /** Last computed angles (in degrees, with signs matching Figura's originRot convention). */
    private static final FiguraVec3 head = FiguraVec3.of();
    private static final FiguraVec3 body = FiguraVec3.of();
    private static final FiguraVec3 rightArm = FiguraVec3.of();
    private static final FiguraVec3 leftArm = FiguraVec3.of();
    private static final FiguraVec3 rightLeg = FiguraVec3.of();
    private static final FiguraVec3 leftLeg = FiguraVec3.of();

    /**
     * Last computed <b>positions</b> (pivots), using the same convention as {@code getOriginPos()}.
     * <p>Warning: rotation alone <b>does not reproduce crouching</b>. When crouching, vanilla {@code setupAnim}
     *   lowers <b>the pivots themselves</b> ({@code body.y}, {@code head.y}, etc.). Observed in game on
     *   2026-09-20: without adjusting the positions, crouching looked wrong.
     */
    private static final FiguraVec3 headPos = FiguraVec3.of();
    private static final FiguraVec3 bodyPos = FiguraVec3.of();
    private static final FiguraVec3 rightArmPos = FiguraVec3.of();
    private static final FiguraVec3 leftArmPos = FiguraVec3.of();
    private static final FiguraVec3 rightLegPos = FiguraVec3.of();
    private static final FiguraVec3 leftLegPos = FiguraVec3.of();

    private static final float RAD_TO_DEG = (float) (180.0 / Math.PI);

    /**
     * Returns the third-person rotation of a part by name. Names are the same as in Figura's
     * {@code vanilla_model}.
     *
     * @param part {@code HEAD}, {@code BODY}, {@code RIGHT_ARM}, {@code LEFT_ARM}, {@code RIGHT_LEG}, {@code LEFT_LEG}
     * @return rotation in degrees, or {@code null} (nil in Lua) if it cannot be computed
     */
    public static FiguraVec3 getRot(String part) {
        if (!update()) return null;
        if (part == null) return null;
        switch (part.toUpperCase()) {
            case "HEAD": return head.copy();
            case "BODY": return body.copy();
            case "RIGHT_ARM": return rightArm.copy();
            case "LEFT_ARM": return leftArm.copy();
            case "RIGHT_LEG": return rightLeg.copy();
            case "LEFT_LEG": return leftLeg.copy();
            default: return null;
        }
    }

    /**
     * Returns the third-person <b>position</b> (pivot) of a part by name. Same convention as
     * {@code vanilla_model.X:getOriginPos()}.
     *
     * @param part {@code HEAD}, {@code BODY}, {@code RIGHT_ARM}, {@code LEFT_ARM}, {@code RIGHT_LEG}, {@code LEFT_LEG}
     * @return position in model-part units, or {@code null} if it cannot be computed
     */
    public static FiguraVec3 getPos(String part) {
        if (!update()) return null;
        if (part == null) return null;
        switch (part.toUpperCase()) {
            case "HEAD": return headPos.copy();
            case "BODY": return bodyPos.copy();
            case "RIGHT_ARM": return rightArmPos.copy();
            case "LEFT_ARM": return leftArmPos.copy();
            case "RIGHT_LEG": return rightLegPos.copy();
            case "LEFT_LEG": return leftLegPos.copy();
            default: return null;
        }
    }

    /** Computes this frame's pose (skipped if already done). Returns true on success. */
    private static boolean update() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.level == null) return false;

        // Frame key: run only once per render frame.
        long frame = mc.getFrameTimeNs();
        if (frame == cachedFrame) return cachedOk;
        cachedFrame = frame;
        cachedOk = false;

        try {
            AbstractClientPlayer player = mc.player;
            EntityRenderer<?, ?> renderer = mc.getEntityRenderDispatcher().getRenderer(player);
            if (!(renderer instanceof PlayerRenderer playerRenderer)) return false;
            PlayerRendererInvoker invoker = (PlayerRendererInvoker) playerRenderer;

            if (state == null) state = invoker.silly$createRenderState();
            float delta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
            invoker.silly$extractRenderState(player, state, delta);

            PlayerModel m = pickModel(state);
            if (m == null) return false;
            m.setupAnim(state);

            // Note: match the sign convention of Figura's VanillaModelPart.save EXACTLY:
            //     originRot.set(-part.xRot, -part.yRot, part.zRot), then scaled by 180/π.
            //   Otherwise avatar scripts end up with two different coordinate systems and silently drift apart.
            store(head, headPos, m.head, ParentType.Head);
            store(body, bodyPos, m.body, ParentType.Body);
            store(rightArm, rightArmPos, m.rightArm, ParentType.RightArm);
            store(leftArm, leftArmPos, m.leftArm, ParentType.LeftArm);
            store(rightLeg, rightLegPos, m.rightLeg, ParentType.RightLeg);
            store(leftLeg, leftLegPos, m.leftLeg, ParentType.LeftLeg);

            cachedOk = true;
            return true;
        } catch (Throwable ignored) {
            // Warning: never let exceptions escape the render path; one avatar must not take down the screen.
            return false;
        }
    }

    /**
     * Stores the angles and position using <b>literally the same formulas</b> as {@code VanillaModelPart.save}.
     * <pre>
     *   originRot = (-xRot, -yRot, +zRot) * 180/π
     *   originPos = (parentType.offset - part.pos) * (1, -1, -1)
     * </pre>
     * Warning: if even one character differs, avatar scripts end up with two different coordinate systems and
     * <b>silently</b> drift apart.
     */
    private static void store(FiguraVec3 rotOut, FiguraVec3 posOut, ModelPart part, ParentType parentType) {
        rotOut.set(-part.xRot * RAD_TO_DEG, -part.yRot * RAD_TO_DEG, part.zRot * RAD_TO_DEG);
        FiguraVec3 pivot = parentType.offset.copy();
        pivot.subtract(part.x, part.y, part.z);
        pivot.multiply(1, -1, -1);
        posOut.set(pivot);
    }

    /**
     * The model differs between slim and wide arms. This barely affects the angle computation, but the shape
     * is matched anyway.
     * <p>Warning: in 1.21.8 the constructor is {@code PlayerModel(ModelPart, boolean slim)} (found through a
     *   build error). A single layer, {@code ModelLayers.PLAYER}, is used and slimness is passed as an argument.
     */
    private static PlayerModel pickModel(PlayerRenderState st) {
        boolean slim = isSlim(st);
        if (slim) {
            if (modelSlim == null) modelSlim = bake(true);
            return modelSlim;
        }
        if (model == null) model = bake(false);
        return model;
    }

    private static boolean isSlim(PlayerRenderState st) {
        try {
            return st.skin != null && "slim".equals(st.skin.model().name().toLowerCase());
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static PlayerModel bake(boolean slim) {
        try {
            ModelPart root = Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER);
            return new PlayerModel(root, slim);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
