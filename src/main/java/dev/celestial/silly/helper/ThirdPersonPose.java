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
 * <b>1인칭에서도 「3인칭 기준」 바닐라 포즈를 구한다.</b>
 *
 * <p>문제 — Figura 의 {@code vanilla_model.*:getOriginRot()} 은 1인칭에서 <b>3인칭 포즈를 주지 않는다.</b>
 * ① {@code LivingEntityRendererMixin.preRender} 가 {@code Avatar.firstPerson} 일 때 {@code part.save(model)}
 * 앞에서 {@code ci.cancel()} 하고, ② {@code PlayerRendererMixin.onRenderHand} 가 1인칭 손 렌더 중에
 * {@code part.save(model)} 를 부른다(그 모델은 팔 하나만 그리려고 setup 된 상태다).
 * 그래서 1인칭에서는 팔이 굳거나 손 포즈가 섞인다.
 *
 * <p>해법 — <b>렌더러의 모델을 건드리지 않고</b>, 우리 몫의 {@link PlayerModel} 을 따로 구워 두고
 * 매 프레임 최대 한 번 {@code setupAnim} 을 돌려 각도를 읽는다. 바닐라 계산을 그대로 쓰므로
 * 공식을 베껴 적을 필요가 없다(버전이 올라가도 계산이 따라온다).
 *
 * <p>⚠ 이 클래스는 <b>클라이언트 전용</b>이다. 서버 스레드에서 부르지 마라.
 * ⚠ 결과는 <b>프레임 단위로 캐시</b>한다 — 한 프레임에 여러 파츠를 물어도 계산은 한 번이다.
 */
public final class ThirdPersonPose {

    private ThirdPersonPose() {}

    /** 우리 몫의 모델. 렌더러 것과 공유하지 않는다 — 공유하면 1인칭 손 렌더를 망친다. */
    private static PlayerModel model;
    private static PlayerModel modelSlim;
    /** 재사용하는 렌더 상태. 매 프레임 다시 채운다. */
    private static PlayerRenderState state;

    /** 프레임 캐시 — 같은 프레임에 두 번 계산하지 않는다. */
    private static long cachedFrame = Long.MIN_VALUE;
    private static boolean cachedOk;

    /** 마지막으로 계산한 각도 (도 단위, Figura 의 originRot 규약과 같게 부호를 맞춘다). */
    private static final FiguraVec3 head = FiguraVec3.of();
    private static final FiguraVec3 body = FiguraVec3.of();
    private static final FiguraVec3 rightArm = FiguraVec3.of();
    private static final FiguraVec3 leftArm = FiguraVec3.of();
    private static final FiguraVec3 rightLeg = FiguraVec3.of();
    private static final FiguraVec3 leftLeg = FiguraVec3.of();

    /**
     * 마지막으로 계산한 **위치**(피벗). {@code getOriginPos()} 와 같은 규약이다.
     * <p>⚠ 회전만으로는 <b>웅크리기가 재현되지 않는다</b> — 바닐라 {@code setupAnim} 은 웅크릴 때
     *   {@code body.y}·{@code head.y} 등 <b>피벗 자체</b>를 내린다(🙋 2026-09-20 실측:
     *   *"웅크리기 할때 pos는 하나도 건들이지 않아서 … 어색하게 보이더라고"*).
     */
    private static final FiguraVec3 headPos = FiguraVec3.of();
    private static final FiguraVec3 bodyPos = FiguraVec3.of();
    private static final FiguraVec3 rightArmPos = FiguraVec3.of();
    private static final FiguraVec3 leftArmPos = FiguraVec3.of();
    private static final FiguraVec3 rightLegPos = FiguraVec3.of();
    private static final FiguraVec3 leftLegPos = FiguraVec3.of();

    private static final float RAD_TO_DEG = (float) (180.0 / Math.PI);

    /**
     * 파츠 이름으로 3인칭 회전을 얻는다. 이름은 Figura 의 {@code vanilla_model} 과 같게 쓴다.
     *
     * @param part {@code HEAD} · {@code BODY} · {@code RIGHT_ARM} · {@code LEFT_ARM} · {@code RIGHT_LEG} · {@code LEFT_LEG}
     * @return 도 단위 회전. 계산할 수 없으면 {@code null} (Lua 에서 nil)
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
     * 파츠 이름으로 3인칭 <b>위치</b>(피벗)를 얻는다. {@code vanilla_model.X:getOriginPos()} 와 같은 규약.
     *
     * @param part {@code HEAD} · {@code BODY} · {@code RIGHT_ARM} · {@code LEFT_ARM} · {@code RIGHT_LEG} · {@code LEFT_LEG}
     * @return 파츠 단위 위치. 계산할 수 없으면 {@code null}
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

    /** 이번 프레임의 포즈를 계산한다(이미 했으면 건너뛴다). 성공하면 true. */
    private static boolean update() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.level == null) return false;

        // 프레임 구분 — 렌더 프레임마다 한 번만 돈다.
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

            // ★ 부호 규약을 Figura 의 VanillaModelPart.save 와 **같게** 맞춘다:
            //     originRot.set(-part.xRot, -part.yRot, part.zRot) 뒤 180/π 스케일.
            //   맞추지 않으면 아바타 쪽에서 «둘이 다른 좌표계» 를 만나 조용히 어긋난다.
            store(head, headPos, m.head, ParentType.Head);
            store(body, bodyPos, m.body, ParentType.Body);
            store(rightArm, rightArmPos, m.rightArm, ParentType.RightArm);
            store(leftArm, leftArmPos, m.leftArm, ParentType.LeftArm);
            store(rightLeg, rightLegPos, m.rightLeg, ParentType.RightLeg);
            store(leftLeg, leftLegPos, m.leftLeg, ParentType.LeftLeg);

            cachedOk = true;
            return true;
        } catch (Throwable ignored) {
            // ⚠ 렌더 경로에서 예외를 밖으로 내보내지 않는다 — 아바타 하나 때문에 화면이 죽으면 안 된다.
            return false;
        }
    }

    /**
     * 각도와 위치를 {@code VanillaModelPart.save} 와 <b>글자 그대로 같은 식</b>으로 담는다.
     * <pre>
     *   originRot = (-xRot, -yRot, +zRot) * 180/π
     *   originPos = (parentType.offset - part.pos) * (1, -1, -1)
     * </pre>
     * ⚠ 한 글자라도 다르면 아바타 쪽에서 «둘이 다른 좌표계» 를 만나 <b>조용히</b> 어긋난다.
     */
    private static void store(FiguraVec3 rotOut, FiguraVec3 posOut, ModelPart part, ParentType parentType) {
        rotOut.set(-part.xRot * RAD_TO_DEG, -part.yRot * RAD_TO_DEG, part.zRot * RAD_TO_DEG);
        FiguraVec3 pivot = parentType.offset.copy();
        pivot.subtract(part.x, part.y, part.z);
        pivot.multiply(1, -1, -1);
        posOut.set(pivot);
    }

    /**
     * 슬림/와이드 팔에 따라 모델이 다르다 — 각도 계산에는 거의 영향이 없지만 형태를 맞춘다.
     * <p>⚠ 1.21.8 의 생성자는 {@code PlayerModel(ModelPart, boolean slim)} 이다(빌드 오류가 알려 줬다).
     *   레이어는 {@code ModelLayers.PLAYER} 하나를 쓰고 슬림 여부는 인자로 준다.
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
