package dev.celestial.silly.helper;

import dev.celestial.silly.SillyPlugin;
import dev.celestial.silly.lua.SillyAPI;
import org.figuramc.figura.avatar.AvatarManager;

/**
 * 1인칭 손 흔들림 배율 — silly:setHandBobScale · setHandSwayScale (2026-11-23, 하네스 mc_repositories M67).
 *
 * 바닐라 손 패스의 흔들림 두 갈래를 그 «원천» 에서 키우고 줄인다.
 *   걸음 흔들림 — GameRenderer.bobView 의 진폭(lerp(oBob, bob)). 이동 · Z 회전 · X 회전이 모두 진폭에 비례해 함께 준다.
 *   시선 지연 — ItemInHandRenderer.renderHandsWithItems 의 (시선 − xBob/yBob) × 0.1 회전 둘.
 * 코어의 FirstPerson 파츠 · 피벗 손 아이템은 두 흔들림이 다 곱해진 «뒤» 에 그려지므로 저절로 따른다(코어 CLAUDE.md 이력 22 · 27).
 *
 * 값은 호스트 아바타(SillyPlugin.hostInstance)의 것만 읽는다 — 내 1인칭 화면이라서다. 아바타가 내려가면 1(바닐라)이다.
 */
public final class HandBob {
    // GameRenderer.renderLevel 이 카메라(월드 투영)용으로 bobView 를 부르는 동안 true — 그 호출에는 배율을 안 건다.
    public static boolean cameraPass = false;

    private HandBob() {}

    public static float bobScale() {
        SillyAPI silly = SillyPlugin.hostInstance;
        if (silly == null || AvatarManager.panic) return 1f;
        return silly.handBob.get();
    }

    public static float swayScale() {
        SillyAPI silly = SillyPlugin.hostInstance;
        if (silly == null || AvatarManager.panic) return 1f;
        return silly.handSway.get();
    }

    /** 배율 하나 — 지금 보이는 값에서 목표까지 실제 시간으로 smoothstep 전환. */
    public static final class Channel {
        public static final float MIN = 0f;
        public static final float MAX = 10f;

        private float from = 1f;
        private float to = 1f;
        private long startNanos = 0L;
        private long durationNanos = 0L;

        /** 지금 보이는 값 — 프레임마다 부른다. */
        public float get() {
            if (durationNanos <= 0L) return to;
            long elapsed = System.nanoTime() - startNanos;
            if (elapsed >= durationNanos) return to;
            float t = Math.max(0f, (float) elapsed / (float) durationNanos);
            float s = t * t * (3f - 2f * t);
            return from + (to - from) * s;
        }

        public float getTarget() {
            return to;
        }

        /** ticks ≤ 0 이면 바로. 전환 중에 다시 불러도 지금 보이는 값에서 이어 가므로 튀지 않는다. */
        public void set(float target, int ticks) {
            if (Float.isNaN(target)) target = 1f;
            target = Math.max(MIN, Math.min(MAX, target));
            float current = get();
            from = current;
            to = target;
            startNanos = System.nanoTime();
            durationNanos = Math.max(0, ticks) * 50_000_000L;
            if (durationNanos <= 0L) from = target;
        }
    }
}
