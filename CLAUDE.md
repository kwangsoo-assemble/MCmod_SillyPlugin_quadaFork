# SillyPlugin — Figura 애드온 모드 (자체 포크)

원본: `github.com/Figura-Solstice/SillyPlugin` (GoofyPlugin 후속작). Figura 아바타에
`silly` Lua API·HUD 제어·치트성 연출 기능을 추가하는 애드온.
이 폴더는 **1.21.8 타깃 추가 + 자체 기능 확장을 한 포크**다 (아래 자체 변경 참고).

## 빌드

- **stonecutter(멀티버전) + modstitch(로더 추상화)** 구조. 소스는 `src/` 하나이고
  버전 분기는 `//? if >=1.21.6 { ... //?} else { /*...*/ //?}` 주석으로 처리한다.
- 전 타깃 빌드: `JAVA_HOME="C:\Program Files\Java\jdk-21" ./gradlew chiseledBuild`
- 단일 타깃: `./gradlew :1.21.8-fabric:build` (활성 타깃일 때만 안전)
- ⚠️ **활성 타깃이 아닌 버전을 `:<ver>:build`로 직접 빌드하면 remapJar가 실패**한다
  (`Cannot nest jars into none mod jar` — 활성 아닌 버전은 소스가 주석 상태).
  여러 버전을 검증할 땐 **반드시 `chiseledBuild`** 를 쓴다.
- 활성 타깃 전환: `./gradlew "Set active project to <ver>-<loader>"`
  (현재 활성 = `1.21.8-fabric`, `stonecutter.gradle.kts`의 `stonecutter active` 로도 확인)
- 산출물: `versions/<타깃>/build/libs/sillyplugin-1.2.0-alpha.jar`

### 타깃 구성 (`settings.gradle.kts` + `versions/*/gradle.properties`)

| 타깃 | 로더 | figura 의존 |
|------|------|------------|
| 1.21.8-fabric | loom | **로컬 포크 jar** (`.libs/`) |
| 1.21.4 / 1.21.1 | fabric+neoforge | maven `org.figuramc:figura-*` |
| 1.20.1 | fabric | maven |

- **1.21.8은 공식 maven에 figura 0.1.6+1.21.8이 없어** `.libs/figura-0.1.6+1.21.8-fabric-mc.jar`
  (= `../figura_core_internal` 빌드 산출물)을 직접 참조한다.
  **포크의 공개 API가 바뀌면 이 jar를 재복사**하고 재빌드해야 한다 (UI 상수 등 내부 변경은 불필요).
- 버전별 상수 추가 지점(신규 MC 버전 추가 시 4곳 모두): `build.gradle.kts`의 `javaTarget`,
  `pack_format`, `svc_version`(Modrinth 버전 id), figura 의존 분기.

## ★ GitHub 저장소 — 이 작업본을 그대로 올린다 (2026-10-10 — 🙋 규칙 변경)

이 폴더가 GitHub `kwangsoo-assemble/MCmod_SillyPlugin_quadaFork` 의 **작업본**이다 — 한글 주석 · 이 CLAUDE.md 까지 그대로 올라간다.
옛 «공개판» 사본(`figura_sillyplugin_but_ai_edited` — 같은 코드에 주석만 영어)은 2026-10-10 에 없앴다 — 그 이력은 이 저장소에 합쳐 두었다.

- 매듭마다 로컬 커밋 → `python ../../../mc_content_production/projects/mc_repositories/tools/repos.py publish sillyplugin` 로 푸시한다
  (세션이 자동으로 한다 · `--force` 금지 · 원격이 앞서 있거나 README 동기가 빨가면 멈춘다 — 하네스 `CLAUDE.md` 「외부 저장소 — mc_repositories」).
- README.md 는 영어판이고 **원본은 하네스의 한글판**이다 — `projects/mc_repositories/readme_ko/MCmod_SillyPlugin_quadaFork.md`. 기능을 바꾸면 거기 먼저 적고
  영어 README 에 옮긴 뒤 `repos.py mark-readme sillyplugin`. 원본 README 는 `README.upstream.md`.
- 레포 이름 규칙 — `MCmod_` / `MCplugin_` + 프로젝트 이름 (+ 포크는 `_quadaFork`). `but-ai-edited` 는 **버전명에만** 쓴다.
- ⚠ **치트 게이트 완화판이 그대로 공개돼 있다**(아래 «자체 변경» 2) — 옛 공개판만 원본 게이트였다(2026-09-25 «공개판은 원본대로»).
  🙋 2026-10-10 «내부판 그대로 올리기» — README 의 «참고» 도 완화판 설명으로 고쳤다.

## 자체 변경 (원본 대비)

1. **1.21.8-fabric 타깃 추가** — 1.21.6+ 렌더링 개편 대응이 핵심:
   - `GuiGraphics.pose()`가 `Matrix3x2fStack`(2D) → `pushMatrix/popMatrix/mul`.
     **4x4 행렬은 2D 아핀 성분만 적용 가능**(3D 기울임 불가) — `setGuiMatrix`, `pushPoseMatrix` 영향
   - `blit`: `RenderType::guiTextured` → `RenderPipelines.GUI_TEXTURED`
   - `InventoryScreen.renderEntityInInventory`: 좌표 박스 시그니처로 변경 → `blitEntity`는 근사 렌더
   - **경험치바/점프미터가 Gui에서 `ContextualBarRenderer`로 이동** → Gui의 개별 메서드 inject 대신
     `renderHotbarAndDecorations`의 호출 지점 3곳을 `@WrapOperation`으로 감싸 종류별 분기
   - NBT `getCompoundOrEmpty`, `TagValueInput.create(ProblemReporter.DISCARDING, ...)`,
     `Player(Level, GameProfile)` 생성자, 아틀라스 GPU readback(포크 TextureAPI 패턴,
     `TextureAtlasAccessor.figura$getWidth`)
2. **치트 게이트 완화** (`SillyAPI.cheatExecutor`): 원본의 옵트인 조건(OP권한2 / 크리에이티브 /
   싱글플레이 / MOTD `§s§i§l§l§y§p§l§u§g§i§n`)을 **제거**. 이제 클라 설정 `CHEATS`(기본 on)만
   검사하고, MOTD 옵트아웃(`§!§s§i§l§l§y§p§l§u§g§i§n`)만 강제 차단으로 남겼다.
3. **`silly:getThirdPersonRot(part)` · `getThirdPersonPos(part)` 추가** (2026-09-20) — **1인칭에서도 3인칭 기준 바닐라 파츠 회전**을 준다.
   · 왜 — `vanilla_model.X:getOriginRot()` 이 1인칭에서 3인칭 포즈가 **아니다.** 코어 두 자리 때문이다:
     ① `LivingEntityRendererMixin.preRender` 가 `Avatar.firstPerson` 일 때 `part.save(model)` **앞에서** `ci.cancel()`
     ② `PlayerRendererMixin.onRenderHand` 가 1인칭 손 렌더 중 `part.save(model)` 호출(**팔 하나만 setup 된 모델**)
     ⇒ «내 포즈를 월드에 복제하는» 아바타(거울상·분신)는 1인칭에서 팔이 굳는다. 실제 사례: 미술감정편 거울 반사상.
   · 어떻게 — `helper/ThirdPersonPose`: **자기 몫의 `PlayerModel`** 을 구워 두고(`PlayerModel(root, slim)`),
     `PlayerRendererInvoker`(신규 Invoker 믹스인)로 `createRenderState`/`extractRenderState` 를 열어
     매 프레임 최대 1회 `setupAnim` 을 돌려 각도를 읽는다. **렌더러 모델은 안 건드린다**(건드리면 1인칭 손 렌더가 깨진다).
   · 부호·단위는 `VanillaModelPart.save` 와 **글자 그대로 같은 식**이다 —
     회전 `(-xRot, -yRot, +zRot) * 180/π` · 위치 `(parentType.offset - part.pos) * (1, -1, -1)`.
     Lua 에서 `getOriginRot()`/`getOriginPos()` 자리에 그대로 끼운다.
   · ⚠ **위치 짝이 꼭 필요하다** — 회전만으로는 **웅크리기가 재현되지 않는다**(바닐라가 피벗 y 를 내린다).
     실측: 회전만 넣었더니 🙋 *"웅크리기 할때 거울에 보이는 자신이 어색하게 보이더라고"*.
   · ⚠⚠ **둘 다 「아바타 주인이 로컬 플레이어일 때만」 값을 준다**(`isLocalAvatar` 게이트). 남이면 `null`.
     아바타 스크립트는 **보는 클라마다** 도는데 계산은 `Minecraft.getInstance().player` 기준이라,
     게이트가 없으면 **남의 아바타가 내 모션을 따라 한다**(2026-09-20 2인 동시 접속 실측).
     남은 3인칭 렌더라 `vanilla_model` 이 이미 정확하므로 **폴백이 정답**이다.
   · ⚠ **1.21.6+ 렌더 상태 API 전용**이다. 구버전 타깃에 이식하려면 `//? if >=1.21.6` 분기가 필요하다
     (지금은 활성 타깃 `1.21.8-fabric` 에서만 빌드·검증했다. `chiseledBuild` 는 아직 안 돌렸다).
4. **`silly:setBoundingBoxScale(vec2|w,h|단일값|없음)` 추가** — 아바타 착용 엔티티의 히트박스
   너비/높이 스케일(0.05~16 클램프, 없이 호출 = 리셋). `custom_hitbox` 모드 패턴 이식:
   `PlayerDimensionsMixin`(신규)이 `Player.getDefaultDimensions` RETURN을 스케일하되
   **눈높이는 바닐라 유지**(카메라/시선 레이 서버 desync 방지). 아바타 cleanup 시 자동 리셋.
   클라 표시 전용 — 서버 실제 히트박스는 그대로다.
   - ★ **몹(CEM) 아바타에도 먹는다** (2026-09-28, 하네스 `projects/entity_avatar_template` — 베리티 더미 히트박스 너비 3배).
     전에는 `PlayerDimensionsMixin`(플레이어만 · `getLoadedAvatar` 도 플레이어 목록)이라 몹 아바타가 불러도 아무 일이 없었다.
     `MobDimensionsMixin`(신규 · client)이 `LivingEntity.getDimensions`(**final** — 모든 몹이 지난다 · 좀비처럼 `getDefaultDimensions` 를 덮어쓴 몹도)
     RETURN 을 스케일한다. 스케일은 `SillyAPI.MOB_BOX_SCALES`(엔티티 UUID → 스케일 · `setBoundingBoxScale` 이 몹이면 올리고 · cleanup 이 지운다)에서
     읽는다 — 크기 계산(핫패스)에서 아바타를 찾거나 **만들지** 않는다(코어 `AvatarManager.getAvatar` 는 CEM 아바타를 새로 만든다).
     플레이어 · 잠자는 자세는 안 건드린다 · 눈높이 바닐라 · `isClientSide` 가드(통합 서버 스레드). 빌드 jar `javap -v`: 대상 `method_18377`.
     🎬 2026-09-28 게임이 뜨고(믹스인 대상 찾음) 베리티 더미 폭 3배가 F3+B 로 보였다.

5. **`/figura nonhost_load` 를 포크 코어로 이관 — 여기서 제거** (2026-09-22). `SillyCommands.nonhostLoad` 와
   접근자 믹스인 `AvatarManagerAccessor`·`LocalAvatarLoaderAccessor` 를 지웠다 (`sillyplugin.mixins.json` 도).
   · 왜 — 여기 구현은 `clearAvatars()` 뒤에 포크의 비공개 `FETCHED_USERS` 표식을 못 되돌려 **다음 프레임에 클라우드 아바타가
     덮어썼다.** 고치려면 표식 + 사용자별 pin 을 모든 클라우드/FSB 진입점이 봐야 해서 포크 안에서만 가능하다
     (`../figura_core_internal/CLAUDE.md` 10). 지금은 `/figura nonhost_load` · `nonhost_unload` · `netlock` 이 코어 명령이다.
   · ⚠⚠ **여기에 다시 넣지 마라** — Brigadier 는 같은 이름 리터럴을 합치고 **나중 등록(이 애드온)이 이긴다.** 버그가 부활한다.
   · `/figura reload nonhost` 는 그대로 여기 있다. pin 된 아바타는 포크가 pin 경로에서 되살리고, `netlock` 중에는 지운 네트워크
     아바타가 되살아나지 않는다(잠금의 뜻).
   · `.libs` 포크 jar 재복사 + 재빌드했다 (포크 공개 API 추가).

## ⚠️ 미신 작성 주의 (실제로 크래시 났던 것들)

- **`Player`/`Entity` 등 공용 클래스 믹스인은 통합 서버(싱글플레이) 스레드의 서버측 엔티티에도 적용된다.**
  거기서 `AvatarManager`·`Minecraft` 등 **클라 전용 API를 부르면 NPE로 서버가 크래시**한다
  (실제 사례: 싱글 월드 열 때 `Ticking player` 크래시).
  → `level().isClientSide()` 가드 필수.
- `AvatarManager.getAvatarForPlayer()`는 **백엔드 fetch 부작용**이 있다. 매틱 도는 핫패스나
  디멘션 계산에서는 `getLoadedAvatar()`(부작용 없음)를 쓴다.
- **Figura 내부 타깃 미신은 포크 버전 올릴 때마다 검증**할 것. 특히 합성 람다 이름
  (`FiguraLuaPrinter.lambda$static$0~2`, `LuaSound.lambda$play$4/$6`)은 상류 코드가 조금만 바뀌어도
  번호가 밀린다 — 포크 jar에 `javap -p`로 실존 확인.
- Loom `remapJar`는 **`remap = false` 미신의 어노테이션 안 MC 타입 디스크립터까지 intermediary로
  재작성**한다 (refmap 없이 동작). 빌드 jar에 `javap -v`로 `method_xxxxx` 리맵 여부 검증 가능.

## Lua API 표면 (전역)

- `silly` — 메인. HUD 숨김(`setHudElementVisible`, 17종), 아바타 조회/권한, 이동·물리 치트
  (`setPos/setRot/setVelocity/setGravity/setFly/setNoclip`), 히트박스(`setBoundingBoxScale`),
  가짜 블록, `silly.vehicle`(탈것), `silly.profiler`, `silly.collection`(테이블 유틸),
  `silly.compats.svc`(Simple Voice Chat — `isTalking`/`getVolume` 립싱크용)
- `silly_backports` (= `silly.backports`, deprecated) — `getCaller`, 런타임 스크립트 조작
  (`addScript`/`getScript(s)`), `PRE_RENDER` 이벤트
- `silly_dev` — 개발 모드 빌드 + 호스트 전용 디버그
- 추가 이벤트: `events.GUI_RENDER(graphics, delta)`(HUD에 직접 그리기 — `LuaGraphicsAPI`는
  **해당 프레임만 유효**), `events.ERROR(message)`
- **API 등록 위치**: `mixin/FiguraAPIManagerMixin`(WHITELISTED_CLASSES + API_GETTERS),
  이벤트 추가는 `mixin/EventsAPIMixin`

## 관련 문서

- Lua 자동완성 docs(한국어 전체 API): 아바타 레포
  `RaD_FiguraAvatars/.vscode/docs/addon/sillyplugin.lua`
- 대상 클라이언트 포크: `../figura_core_internal/CLAUDE.md`
- 히트박스 원본 아이디어: `C:\Users\aaaa\OneDrive\Desktop\폴더\마크 기타 개발\custom_hitbox` (모드 설정 기반 전체 플레이어 스케일 — 2026-09-25 이전 대상이 아니라 옛 자리에 있다)
