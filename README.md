# MCmod_SillyPlugin_quadaFork

**[한국어](#한국어)** 설명은 아래에 있습니다.

> An **AI-assisted fork** of the Figura add-on [SillyPlugin](https://github.com/Figura-Solstice/SillyPlugin).
> It adds a Minecraft **1.21.8 (Fabric)** target (built against
> [MCmod_Figura_quadaFork](https://github.com/kwangsoo-assemble/MCmod_Figura_quadaFork)), a few new functions
> in the `silly` Lua API, and some `/figura` subcommands and a key binding.

> ⚠ **This is an unofficial fork** and is not affiliated with the original authors. Most changes were written with an
> AI coding assistant (Claude Code) and checked by the maintainer in real use. Before reporting a problem upstream,
> please make sure it is not caused by this fork.

## At a glance

| Item | Value |
|---|---|
| Version | `1.2.0-alpha.but-ai-edited.1` — a pre-release tag added to tell it apart from upstream 1.2.0-alpha (see 5) |
| Added target | Minecraft 1.21.8, Fabric |
| Upstream targets | 1.21.4 and 1.21.1 (Fabric, NeoForge), 1.20.1 (Fabric) — the version folders are still there, but they **do not build right now** (see "Building") |
| Figura for 1.21.8 | MCmod_Figura_quadaFork `0.1.6-but-ai-edited.1+1.21.8` (not on the official Maven) |
| Java | 21 |
| Build setup | Stonecutter (multi-version) + modstitch (loader abstraction), as in upstream |

## Changes from upstream

Compared against upstream branch `master` at [`828e5ed`](https://github.com/Figura-Solstice/SillyPlugin/commit/828e5edfc9cc5a9b84368bb9d2b84f01e1e70f2d).
The **Code** paths under each item are relative to `src/main/java/dev/celestial/silly/`, except build files and
`versions/`, which are relative to the repository root. **(new)** marks files that upstream does not have.

### 1. Minecraft 1.21.8 (Fabric) target

Adapted to the large rendering changes in 1.21.6.

- `GuiGraphics.pose()` is now a 2D matrix stack (`Matrix3x2fStack`) — only the 2D affine part of a 4×4 matrix is
  applied (no 3D skew in `setGuiMatrix` and `pushPoseMatrix`).
- Image drawing uses `RenderPipelines.GUI_TEXTURED`.
- The inventory entity rendering API changed, so `blitEntity` is an approximation.
- The experience bar and jump meter moved to `ContextualBarRenderer` — HUD hiding wraps the call sites inside
  `renderHotbarAndDecorations` and handles each kind.
- Adapted to API changes in NBT, entity constructors, texture atlas GPU readback and more.
- On 1.21.8 the add-on references **the build jar of MCmod_Figura_quadaFork** from `.libs/` instead of the official
  Maven (see "Building").

Code: `settings.gradle.kts` · `stonecutter.gradle.kts` · `build.gradle.kts` · `versions/1.21.8-fabric/` (new) ·
`lua/LuaGraphicsAPI.java` · `mixin/hud/GuiMixin.java` · `SillyUtil.java` · `lua/BackportsAPI.java` · `mixin/LocalPlayerMixin.java`

### 2. New Lua functions (`silly`)

- **`silly:getCameraType()`** → `"FIRST_PERSON"`, `"THIRD_PERSON_BACK"` or `"THIRD_PERSON_FRONT"` — the camera mode of
  the viewing client. Figura's `renderer:isFirstPerson()` is only true for the avatar's owner; this function tells
  **other players' avatar scripts too** whether the screen is in first person.
- **`silly:isFirstPerson()`** → the boolean version of the above.
- **`silly:getThirdPersonRot(part)`** and **`silly:getThirdPersonPos(part)`** → the **third-person** rotation (degrees)
  and position of a vanilla part, even in first person. The signs and units match `vanilla_model`'s `getOriginRot()`
  and `getOriginPos()`, so they can be dropped in place. Avatars that copy your pose into the world (mirror
  reflections, clones and so on) keep moving naturally in first person, arms and crouching included.
  - `part` — `"HEAD"`, `"BODY"`, `"RIGHT_ARM"`, `"LEFT_ARM"`, `"RIGHT_LEG"`, `"LEFT_LEG"`
  - They return values **only when the avatar's owner is the local player**, and `nil` otherwise — other players'
    avatars are rendered in third person, so `vanilla_model` is already correct for them.
- **`silly:getSkinModel(player or name[, callback])`** → `"SLIM"` or `"WIDE"`. Works for players who are not online,
  through the profile cache and the session server. When given a name it is **asynchronous** and answers through
  the callback.
- **`silly:setBoundingBoxScale(...)`** → scales the hitbox width and height of the player or mob wearing the avatar
  (vec2, two values or one value, 0.05–16; call with no arguments to reset).
  - Called from a mob (CEM) avatar, it changes that mob's hitbox. The sleeping pose is left alone.
  - Eye height stays vanilla (so the camera and look direction do not drift from the server).
  - **Client-side display only** — the server's real hitbox does not change.
  - Resets automatically when the avatar is cleaned up.

![setBoundingBoxScale example](docs/images/bounding_box_scale.webp)

*`silly:setBoundingBoxScale` — the hitbox (white box) grows, while the eye height (red line) stays vanilla.*

Code: `lua/SillyAPI.java` · four new files — `helper/ThirdPersonPose.java`, `mixin/PlayerRendererInvoker.java`, `mixin/PlayerDimensionsMixin.java`, `mixin/MobDimensionsMixin.java`

### 3. `/figura` subcommands — `upload` and `reload` scopes

Adds subcommands to the `/figura` command of MCmod_Figura_quadaFork (using only that fork's public API).

- `/figura upload <main|server|both>` — uploads the avatar you are wearing.
  `main` is the official cloud, `server` is the server you are connected to (FSB), `both` is both.
- `/figura reload self|nonhost|all` — adds a scope to `/figura reload`.
  `self` is your own avatar (the same as `/figura reload` with no argument), `nonhost` is every avatar except yours,
  `all` is everything.

Code: `SillyCommands.java` · `mixin/FiguraCommandsMixin.java` (both new)

### 4. Key binding — reload all avatars

Figura's reload key only reloads your own avatar. This makes the "Reload all" button from the wardrobe permissions tab
available as a key. Unbound by default.

Code: `mixin/SillyKeybindMixin.java` (new) · `SillySettings.java`

### 5. Version number

To avoid confusion with upstream 1.2.0-alpha, the version carries an extra [SemVer pre-release](https://semver.org/#spec-item-9) tag —
`1.2.0-alpha.but-ai-edited.1` (jar: `sillyplugin-1.2.0-alpha.but-ai-edited.1.jar`). The last number is this fork's own release number.

Code: `gradle.properties`

The new mixins are registered in `src/main/resources/sillyplugin.mixins.json`, and the setting and doc strings are in
`src/main/resources/assets/sillyplugin/lang/en_us.json`.

> Note — **the cheat gate is relaxed.** Upstream enables cheat-like features such as movement and physics control only with
> **operator permission, creative mode, singleplayer, or a server MOTD opt-in**. This fork drops those opt-in conditions and checks
> only the client setting `CHEATS` (on by default). The server MOTD opt-out marker (`§!§s§i§l§l§y§p§l§u§g§i§n`) still always turns
> them off — a server that wants to block them puts that marker in its MOTD.

## Building

JDK 21 is required. **Only the 1.21.8-fabric target has been built and tested.**

```
# 1) Build MCmod_Figura_quadaFork and put its jar in .libs/
mkdir .libs
cp ../MCmod_Figura_quadaFork/fabric/build/libs/figura-0.1.6-but-ai-edited.1+1.21.8-fabric-mc.jar .libs/

# 2) 1.21.8-fabric (the active target)
./gradlew :1.21.8-fabric:build
```

Output: `versions/1.21.8-fabric/build/libs/sillyplugin-1.2.0-alpha.but-ai-edited.1.jar`

- ⚠ The other targets keep only their upstream version folders and **do not build as things stand.** Code added in
  this fork uses a 1.21.2+ API (`PlayerRenderState`) and an API that exists only in MCmod_Figura_quadaFork
  (`NetworkStuff.Destination`) without version conditions, so `chiseledBuild` does not pass either.

## License

**MIT**, the same as upstream (`LICENSE.txt`). Parts that come from the template are under `TEMPLATE_LICENSE` (CC0),
and the upstream README is kept as `README.upstream.md`.

---

# 한국어

> Figura 애드온 [SillyPlugin](https://github.com/Figura-Solstice/SillyPlugin) 을 **AI 도구로 수정한 포크**다.
> Minecraft **1.21.8 (Fabric)** 타깃을 더하고(대상 Figura 는 [MCmod_Figura_quadaFork](https://github.com/kwangsoo-assemble/MCmod_Figura_quadaFork)),
> `silly` Lua API 에 함수 몇 개와 `/figura` 하위 명령 · 단축키를 더했다.

> ⚠ **비공식 포크다.** 원본 제작자와 관련이 없다. 수정의 대부분은 AI 코딩 도구(Claude Code)로 작성했고
> 유지보수자가 실제 사용으로 확인했다. 원본에 문제를 알리기 전에 이 포크 때문인지 먼저 확인해 달라.

## 한눈에

| 항목 | 값 |
|---|---|
| 버전 | `1.2.0-alpha.but-ai-edited.1` — 업스트림 1.2.0-alpha 와 구분하려고 붙인 프리릴리스 꼬리 (아래 5) |
| 추가한 타깃 | Minecraft 1.21.8 · Fabric |
| 원본 타깃 | 1.21.4 · 1.21.1 (Fabric · NeoForge), 1.20.1 (Fabric) — 버전 폴더는 남아 있지만 **지금은 빌드되지 않는다** (아래 «빌드») |
| 1.21.8 의 대상 Figura | MCmod_Figura_quadaFork `0.1.6-but-ai-edited.1+1.21.8` (공식 Maven 에 없다) |
| Java | 21 |
| 빌드 구조 | Stonecutter(여러 버전) + modstitch(로더 추상화) — 원본 그대로 |

## 원본 대비 변경 사항

비교 기준은 원본 `master` 브랜치의 [`828e5ed`](https://github.com/Figura-Solstice/SillyPlugin/commit/828e5edfc9cc5a9b84368bb9d2b84f01e1e70f2d) 다.
각 항목의 **코드** 경로는 `src/main/java/dev/celestial/silly/` 기준이고, 빌드 파일 · `versions/` 만 저장소 루트 기준이다.
**(새)** 는 원본에 없던 파일이다.

### 1. Minecraft 1.21.8 (Fabric) 타깃

1.21.6 에서 크게 바뀐 렌더링에 맞췄다.

- `GuiGraphics.pose()` 가 2D 행렬 스택(`Matrix3x2fStack`)이 됐다 — 4×4 행렬은 2D 아핀 성분만 적용된다
  (`setGuiMatrix` · `pushPoseMatrix` 에서 3D 기울임은 안 된다).
- 이미지 그리기가 `RenderPipelines.GUI_TEXTURED` 로 바뀌었다.
- 인벤토리 엔티티 렌더 API 가 바뀌어 `blitEntity` 는 근사 렌더다.
- 경험치 바 · 점프 게이지가 `ContextualBarRenderer` 로 옮겨졌다 — HUD 숨김은 `renderHotbarAndDecorations` 안의
  호출 지점을 감싸 종류별로 처리한다.
- NBT · 엔티티 생성자 · 텍스처 아틀라스 GPU 읽기 등 API 변화에 맞췄다.
- 1.21.8 에서는 공식 Maven 대신 **MCmod_Figura_quadaFork 의 빌드 jar** 를 `.libs/` 에서 참조한다 (아래 «빌드»).

코드: `settings.gradle.kts` · `stonecutter.gradle.kts` · `build.gradle.kts` · `versions/1.21.8-fabric/` (새) ·
`lua/LuaGraphicsAPI.java` · `mixin/hud/GuiMixin.java` · `SillyUtil.java` · `lua/BackportsAPI.java` · `mixin/LocalPlayerMixin.java`

### 2. 새 Lua 함수 (`silly`)

- **`silly:getCameraType()`** → `"FIRST_PERSON"` · `"THIRD_PERSON_BACK"` · `"THIRD_PERSON_FRONT"` —
  보고 있는 클라이언트의 시점이다. Figura 의 `renderer:isFirstPerson()` 은 아바타 주인일 때만 참이지만,
  이 함수는 **다른 플레이어의 아바타 스크립트에서도** 지금 화면이 1인칭인지 알 수 있다.
- **`silly:isFirstPerson()`** → 위 함수의 참/거짓판.
- **`silly:getThirdPersonRot(part)`** · **`silly:getThirdPersonPos(part)`** → 1인칭에서도 **3인칭 기준** 바닐라 파츠의
  회전(도) · 위치. `vanilla_model` 의 `getOriginRot()` · `getOriginPos()` 와 부호 · 단위가 같아 그 자리에 바로 쓸 수 있다.
  내 포즈를 월드에 복제하는 아바타(거울상 · 분신 등)가 1인칭에서도 팔 · 웅크리기까지 자연스럽게 움직인다.
  - `part` — `"HEAD"` · `"BODY"` · `"RIGHT_ARM"` · `"LEFT_ARM"` · `"RIGHT_LEG"` · `"LEFT_LEG"`
  - 아바타 주인이 **로컬 플레이어일 때만** 값을 주고, 아니면 `nil` 이다 — 다른 플레이어의 아바타는 3인칭으로 그려져
    `vanilla_model` 이 이미 정확하기 때문이다.
- **`silly:getSkinModel(플레이어 또는 이름[, 콜백])`** → `"SLIM"` · `"WIDE"`. 지금 접속해 있지 않은 플레이어도
  프로필 캐시 · 세션 서버로 알아낸다. 이름을 주면 **비동기**라 콜백으로 받는다.
- **`silly:setBoundingBoxScale(...)`** → 아바타를 입은 플레이어 · 몹의 히트박스 너비 · 높이 스케일
  (vec2 · 두 값 · 한 값, 0.05~16 · 인자 없이 부르면 원래대로).
  - 몹(CEM) 아바타에서 부르면 그 몹의 히트박스가 바뀐다. 잠자는 자세는 건드리지 않는다.
  - 눈높이는 바닐라 그대로다 (카메라 · 시선 판정이 서버와 어긋나지 않게).
  - **클라이언트 표시 전용**이다 — 서버의 실제 히트박스는 바뀌지 않는다.
  - 아바타가 정리될 때 자동으로 원래대로 돌아간다.

![setBoundingBoxScale 예시](docs/images/bounding_box_scale.webp)

*`silly:setBoundingBoxScale` — 히트박스(흰 상자)는 커지고, 눈높이(빨간 선)는 바닐라 그대로다.*

코드: `lua/SillyAPI.java` · 새 파일 넷 — `helper/ThirdPersonPose.java` · `mixin/PlayerRendererInvoker.java` · `mixin/PlayerDimensionsMixin.java` · `mixin/MobDimensionsMixin.java`

### 3. `/figura` 하위 명령 — `upload` · `reload` 범위

MCmod_Figura_quadaFork 의 `/figura` 명령에 하위 명령을 더한다 (그 포크의 공개 API 만 쓴다).

- `/figura upload <main|server|both>` — 지금 입고 있는 아바타를 올린다.
  `main` 은 공식 클라우드, `server` 는 접속한 서버(FSB), `both` 는 둘 다.
- `/figura reload self|nonhost|all` — `/figura reload` 에 범위를 붙인다.
  `self` 는 내 아바타(인자 없는 `/figura reload` 와 같다), `nonhost` 는 나를 뺀 모든 아바타, `all` 은 전부.

코드: `SillyCommands.java` · `mixin/FiguraCommandsMixin.java` (둘 다 새)

### 4. 단축키 — 모든 아바타 다시 불러오기

Figura 의 다시 불러오기 키는 내 아바타만 다시 불러온다. 옷장 권한 탭의 «모두 다시 불러오기» 버튼과 같은 동작을
키로 쓸 수 있게 했다. 기본값은 지정 안 됨이다.

코드: `mixin/SillyKeybindMixin.java` (새) · `SillySettings.java`

### 5. 버전 표기

업스트림 1.2.0-alpha 와 헷갈리지 않도록 [SemVer 프리릴리스](https://semver.org/#spec-item-9) 꼬리를 덧붙였다 — `1.2.0-alpha.but-ai-edited.1`
(jar 는 `sillyplugin-1.2.0-alpha.but-ai-edited.1.jar`). 끝 숫자는 이 포크의 판 번호다.

코드: `gradle.properties`

새 믹스인의 등록은 `src/main/resources/sillyplugin.mixins.json`, 설정 · 문서 문자열은 `src/main/resources/assets/sillyplugin/lang/en_us.json` 에 있다.

> 참고 — **치트 게이트를 완화했다.** 원본은 이동 · 물리 같은 치트성 기능을 **OP 권한 · 크리에이티브 · 싱글플레이 · 서버 MOTD 동의**
> 중 하나가 있어야 켜는데, 이 포크는 그 동의 조건을 빼고 클라 설정 `CHEATS`(기본 켬)만 본다. 서버 MOTD 의 거부 표시
> (`§!§s§i§l§l§y§p§l§u§g§i§n`)는 그대로 언제나 끈다 — 막고 싶은 서버는 그 표시를 MOTD 에 넣는다.

## 빌드

JDK 21 이 필요하다. **빌드하고 확인한 것은 1.21.8-fabric 타깃뿐이다.**

```
# 1) MCmod_Figura_quadaFork 를 빌드하고 그 jar 를 .libs/ 에 둔다
mkdir .libs
cp ../MCmod_Figura_quadaFork/fabric/build/libs/figura-0.1.6-but-ai-edited.1+1.21.8-fabric-mc.jar .libs/

# 2) 1.21.8-fabric (활성 타깃)
./gradlew :1.21.8-fabric:build
```

산출물: `versions/1.21.8-fabric/build/libs/sillyplugin-1.2.0-alpha.but-ai-edited.1.jar`

- ⚠ 다른 타깃은 버전 폴더만 원본대로 남아 있고 **지금 상태로는 빌드되지 않는다.** 이 포크에서 더한 코드가 버전 조건 없이
  1.21.2+ API(`PlayerRenderState`)와 MCmod_Figura_quadaFork 에만 있는 API(`NetworkStuff.Destination`)를 쓰기 때문이다.
  그래서 `chiseledBuild` 도 통과하지 않는다.

## 라이선스

원본과 같은 **MIT** 다 (`LICENSE.txt`). 템플릿에서 온 부분은 `TEMPLATE_LICENSE`(CC0), 원본 README 는 `README.upstream.md` 에 그대로 두었다.
