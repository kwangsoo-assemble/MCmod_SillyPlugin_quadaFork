package dev.celestial.silly.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * {@link PlayerRenderer} 의 protected 렌더 상태 API 를 연다.
 *
 * <p>왜 필요한가 — <b>1인칭에서는 Figura 의 {@code vanilla_model} 이 「3인칭 포즈」가 아니라
 * 「1인칭 손 포즈」를 담는다.</b> 두 자리에서 그렇게 된다:
 * <ul>
 *   <li>{@code LivingEntityRendererMixin.preRender} 가 {@code Avatar.firstPerson} 이면
 *       {@code part.save(model)} <b>앞에서</b> {@code ci.cancel()} 한다 — 3인칭 스냅샷이 안 찍힌다.</li>
 *   <li>{@code PlayerRendererMixin.onRenderHand} 가 1인칭 손 렌더 중에 {@code part.save(model)} 를 부른다 —
 *       그 모델은 <b>팔 하나만 그리려고 setup 된 상태</b>다.</li>
 * </ul>
 * 그래서 아바타가 «내 걷기 스윙» 을 읽으려 하면 1인칭에서 멈춘 값이 나온다.
 *
 * <p>이 인보커는 {@code ThirdPersonPose} 가 <b>자기 몫의 모델을 따로 두고</b> 3인칭 기준으로
 * {@code setupAnim} 을 돌릴 수 있게 한다. 렌더러가 쓰는 모델은 건드리지 않는다.
 *
 * <p>⚠ 이름이 바뀌면 빌드가 깨진다(그게 낫다 — 조용히 틀리는 것보다). 포크 버전을 올릴 때
 * {@code javap -p} 로 실존을 확인하라는 프로젝트 규칙이 여기에도 적용된다.
 */
@Mixin(PlayerRenderer.class)
public interface PlayerRendererInvoker {

    /** 렌더 상태 객체를 새로 만든다 (렌더러가 매 프레임 쓰는 것과 별개의 인스턴스). */
    @Invoker("createRenderState")
    PlayerRenderState silly$createRenderState();

    /** 플레이어의 현재 상태를 렌더 상태로 뽑는다 — 걷기 위상·공격 시간·자세 등이 여기서 채워진다. */
    @Invoker("extractRenderState")
    void silly$extractRenderState(AbstractClientPlayer player, PlayerRenderState state, float partialTick);
}
