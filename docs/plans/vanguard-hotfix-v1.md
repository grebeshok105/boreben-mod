# Vanguard Hotfix v0.1.1 — план для исполнителя

**Версия документа:** 1
**Адресат:** агент / разработчик, который будет править v0.1.0 → v0.1.1.
**Цель релиза:** закрыть пользовательские жалобы по v0.1.0 (см. скриншот в треде) и выпустить v0.1.1 как jar в GitHub Release.

---

## 0. TL;DR

| # | Жалоба пользователя (буквальная) | Что делать |
|---|---|---|
| 1 | «убрать нахуй ману вообще полностью убрать» | Вырезать всё про мана из аддона (только `Hero.getManaMax()` возвращает `0f`, плюс убрать любые упоминания в HUD/доке/lang). Не трогать ману в **базовом моде**. |
| 2 | «изменить худ на красно-алый цвет» | Перекрасить `WishRadialHud` и `ReinhardStatusHud` с gold/ivory → crimson/scarlet. |
| 3 | «на модельке рейнхарда нет текстур» | Применять Reinhard player-skin при трансформе. Сейчас костюм — только item, скин не меняется. |
| 4 | «меч ХУЙНЯ А НЕ ТЕКСТУРКА» | Меч рендерится с **missing-texture** (magenta/black checker). Найти причину (texture-not-found в runtime) и поставить нормальную текстуру вместо placeholder-золотого 32×32. |
| 5 | «ГДЕ БЛЯТЬ НА РЕЙНХАРДА ТЕКСТУРКА? ЧТО ЭТО ОБЫЧНЫЙ СТИВ?» | Дубликат #3 — в трансформе ничто не делает skin-override. |
| 6 | «КАКОГО ХУЯ ОНИ НАКЛАДЫВАЮТСЯ НА ДРУГ ДРУГА?» | На скриншоте видно **меч-в-руке от ванилы И GeckoLib-меч одновременно**. Базовый item-renderer не подавлен; нужно либо `BuiltInModelType.GECKOLIB` в model JSON, либо item-model JSON с `"parent": "minecraft:builtin/entity"`. |
| 7 | «никаких кастомных звуков, никаких эффектов. все убогое ужасное фу мерзость» | Удалить из аддона все кастомные звуки/эффекты, перейти на vanilla `SoundEvents.*` для всего, что играется. (Файлы под `assets/vanguard/sounds/` и `sounds.json` можно физически оставить, но **из кода ни одного `vanguard:` SoundEvent больше**.) |
| 8 | «энергию до тысячи повысь» | `ReinhardHero.getEnergyMax()` 200 → 1000. Регены/cost-ы способностей пересмотреть так, чтобы мах из 1000 имел смысл (см. таблицу в §F2). |

Дополнительный мета-запрос: **«либо ты находишь и ставишь мне норм анимки + текстуры либо нахуй идёшь»** — найти и поставить **реальные** ассеты (skin Рейнхарда, модель/текстура меча, GeckoLib-анимации, player-animator JSON-ы). Никаких procedural placeholders в финальном билде.

---

## 1. Branching / PR / релиз

- Все изменения этого hotfix-а — **в одну ветку** `devin/<ts>-hotfix-v0.1.1`, базируется на `origin/devin/1777714426-bootstrap-addon-skills` (это ветка где сейчас весь код 4a–4f; `main` отстаёт).
- Один PR с заголовком `fix(v0.1.1): hotfix — texture/skin/HUD/mana/energy` и описанием на русском.
- **CI не ждать** (по требованию пользователя). После мерджа PR — собрать jar локально (`./gradlew build --no-daemon -x test`), затэгать `v0.1.1`, выложить релиз через `gh release create v0.1.1 --notes-file <md> build/libs/vanguard-0.1.1.jar build/libs/vanguard-0.1.1-sources.jar`.
- В `gradle.properties` поднять `mod_version=0.1.1`.

---

## 2. Карта затронутых файлов (быстрый референс)

```
gradle.properties                                        — mod_version
src/main/java/com/vanguard/mod/hero/ReinhardHero.java    — energy max, mana max
src/main/java/com/vanguard/mod/ability/sword/*.java      — пересмотр cost-ов
src/main/java/com/vanguard/mod/effect/*.java             — убрать vanguard:-sounds
src/main/resources/assets/vanguard/lang/*.json           — убрать про ману
src/main/resources/assets/vanguard/sounds.json           — оставить или вычистить
src/main/resources/assets/vanguard/models/item/dragon_sword_reid.json — fix overlap
src/main/resources/assets/vanguard/textures/item/dragon_sword_reid.png — заменить
src/main/resources/assets/vanguard/geo/item/dragon_sword_reid.geo.json — проверить UV
src/main/resources/assets/vanguard/textures/skin/reinhard.png         — НОВЫЙ
src/client/java/com/vanguard/mod/client/render/HeroSkinLayer.java     — НОВЫЙ (или переиспользовать из base)
src/client/java/com/vanguard/mod/client/hud/WishRadialHud.java        — recolor
src/client/java/com/vanguard/mod/client/hud/ReinhardStatusHud.java    — recolor
src/client/java/com/vanguard/mod/client/render/item/DragonSwordReidRenderer.java — fix path/UV
src/client/java/com/vanguard/mod/client/mixin/ (новый mixin config)   — skin override
```

---

## F1. Полное удаление маны (ровно в аддоне)

### Что есть сейчас

```bash
$ rg -n 'mana|Mana|MANA' src/main/java
src/main/java/com/vanguard/mod/hero/ReinhardHero.java:36:public float getManaMax() { return 100f; }
```

(Все остальные совпадения — `HeavensSwordStrikeManager` / `Manager`-суффиксы, не имеют отношения к ресурсу.)

### Шаги

1. В `ReinhardHero.java`: `getManaMax()` → `return 0f;`
   - Если интерфейс `Hero` из base mod не позволяет 0 (валидация `> 0`) — оставить минимально-возможное, но НЕ показывать в HUD.
2. В `assets/vanguard/lang/en_us.json` и `ru_ru.json` найти любые ключи со словом `mana` — удалить.
3. Проверить (`rg`), что **ни один `Ability.tryConsume(...)` в аддоне не использует `ResourceKind.MANA`**. Сейчас — только `ENERGY`. Подтвердить.
4. Если рефакторинг (1) ломает компиляцию (mana max не разрешает 0) — добавить override `Hero.usesMana()` если такой метод есть в API; иначе документировать в README что mana-bar Reinhard'а всегда полный/пустой и HUD-ом не отображается.

### Acceptance

- В `mods/vanguard-0.1.1.jar` нет ни одного **рантайм-обращения** к `ResourceKind.MANA` в коде аддона.
- В трансформированном Reinhard'е base-mod resource bar показывает **только Energy** (или mana-bar пустой/невидимый).
- Поиск `rg -n 'mana' src` возвращает только false-positives на `Manager`.

---

## F2. Energy max → 1000

### Текущие значения (`ReinhardHero.java`)

```java
public float getEnergyMax()       { return 200f; }
public float getEnergyRegenPerTick() { return 1.0f; }   // 20/sec
```

### Новые значения

| Параметр | Было | Стало | Обоснование |
|---|---|---|---|
| `getEnergyMax()` | 200 | **1000** | Запрос пользователя |
| `getEnergyRegenPerTick()` | 1.0 (20/s) | **5.0 (100/s)** | Чтобы 1000-cap не превратился в 50-секундный wait после ульты |
| Ability costs | (см. ниже) | **×5** скейл сохраняет старое процентное соотношение |

### Cost-таблица (умножить старые значения на 5)

| Ability | Файл | Old `costOnActivate` | New |
|---|---|---|---|
| `air_slash` | `AirSlashAbility.java` | 30 | **150** |
| `reid_draw` | `ReidDrawAbility.java` | 0 toggle | 0 toggle (без изменений) |
| `sky_vault` | `SkyVaultJumpAbility.java` | 0 (только CD) | 0 (без изменений) |
| `heavens_sword_strike` | `HeavensSwordStrikeAbility.java` | 90 | **450** |
| `sword_wave` | `SwordWaveAbility.java` | 60 | **300** |
| `counter_riposte` | `CounterAutoRiposteAbility.java` | 0 (passive) | 0 (без изменений) |

### Acceptance

- `ResourceBarHud` показывает шкалу Energy 0..1000.
- Полная регенерация с 0 → 1000 ≈ 10 сек (1000 / 100 = 10).
- Все cost-ы в реестре `AbilityApi.get(...).costOnActivate()` соответствуют новой таблице.

---

## F3. Меч: missing-texture (magenta/black checker)

### Что наблюдаем

Скриншот показывает в правой руке игрока **magenta-black checker pattern** — это вшитый в Minecraft fallback при отсутствии текстуры. Это значит, что `ResourceManager` не нашёл файл, на который ссылается GeckoLib.

### Что есть в коде/ассетах сейчас

- `DragonSwordReidRenderer.java`:
  ```java
  super(new DefaultedItemGeoModel<>(ResourceLocation.fromNamespaceAndPath(VanguardMod.MOD_ID, "dragon_sword_reid")));
  ```
- `DefaultedItemGeoModel` (GeckoLib 4.5.8) ищет:
  - geo: `assets/vanguard/geo/item/dragon_sword_reid.geo.json` ✅ присутствует
  - animation: `assets/vanguard/animations/item/dragon_sword_reid.animation.json` ✅ присутствует
  - **texture: `assets/vanguard/textures/item/dragon_sword_reid.png`** ✅ присутствует (32×32 RGBA, golden longsword placeholder)
- В `geo/item/dragon_sword_reid.geo.json` `texture_width=32, texture_height=32`. Cubes используют UV: `[0,0]`, `[4,0]`, `[0,18]`, `[0,22]`, `[10,22]`, `[16,18]` — все в пределах 32×32. UV-маппинг корректный.

### Гипотезы причины

1. **GeckoLib runtime не успевает загрузить texture до первого render-pass** — миграция 4.5.8 требует preload через `geckolib.network.GeckoLibNetwork.registerSyncable(...)`. Проверить: `DragonSwordReidItem.createGeoRenderer()` корректно делегирует `DragonSwordReidRenderProvider`, но render provider lazy-инициализирован только client-side. Возможно при первом show-в-руке renderer ещё не инстанцирован и Minecraft fallback-ит на builtin item-renderer.
2. **ItemModel JSON отсутствует или неверный** — для GeckoLib-предмета `assets/vanguard/models/item/dragon_sword_reid.json` должен быть `{"parent": "builtin/entity"}` или **отсутствовать**, чтобы Minecraft не пытался отрисовать 2D-spritesheet поверх 3D-модели. Если он сейчас содержит `{"parent":"item/handheld","textures":{"layer0":"vanguard:item/dragon_sword_reid"}}` — Minecraft рисует 2D-карту (которая магента-чёрная если sprite не загрузился) и оверлеит на GeckoLib-модель → проблема **#6 (наложение)** одновременно с **#4**.
3. **Texture-canvas не покрывает все cubes**. Если хотя бы один cube имеет UV outside 0..32 — GeckoLib рисует магенту. **Проверить пересчётом**: для каждого cube `(uv_x, uv_y)`, нужно что cube's unwrap (4 sides × cube width/height/depth) умещается в 32×32 минус uv-origin. На текущей геометрии — формально умещается, но проверить тщательно через Blockbench `Validate UV`.

### Шаги

1. **Снять `DragonSwordReidItem`-вью** в development-клиенте, прицельно проверить F3 perspective: если меч **в инвентаре** также magenta — значит item-model JSON broken (это case #2). Если только в **руке** — это case #1 (renderer not bound) или case #3 (UV/texture loader).
2. Открыть `src/main/resources/assets/vanguard/models/item/dragon_sword_reid.json`. Если он содержит `"parent": "item/handheld"` — заменить на:
   ```json
   { "parent": "builtin/entity",
     "gui_light": "front",
     "display": {
       "thirdperson_righthand": { "rotation":[-90,55,-35], "translation":[0,4,0.5], "scale":[0.85,0.85,0.85] },
       "firstperson_righthand": { "rotation":[0,-55,25], "translation":[0,4,2], "scale":[0.7,0.7,0.7] },
       "ground": { "rotation":[0,0,0], "translation":[0,2,0], "scale":[0.5,0.5,0.5] },
       "gui": { "rotation":[0,-30,0], "translation":[0,0,0], "scale":[1,1,1] },
       "fixed": { "rotation":[0,-90,0], "translation":[0,0,0], "scale":[1,1,1] }
     }
   }
   ```
   Это **обязательно** для GeckoLib-предметов (см. их docs `https://github.com/bernie-g/geckolib/wiki/Item-Animations`).
3. Заменить **placeholder-текстуру** `textures/item/dragon_sword_reid.png` на нормальную (см. §F7 для источника).
4. Если после фиксов 1-3 меч всё ещё магента — копать в `DragonSwordReidRenderProvider` lazy-binding в `DragonSwordReidItem.createRenderer(...)` (`Supplier<GeoRenderProvider>`). Должен возвращать **новый** инстанс при каждом вызове или кэшировать корректно. См. v0.1.0 коммит `feat(stage-4d)` где была первая попытка обхода classloader-issue.

### Acceptance

- Меч в инвентаре и в руке отображается с реальной текстурой, без magenta-черного.
- В first-person и third-person модель не клипает в землю / не парит над рукой (`display` transforms адекватные).
- При метании (drop) меч лежит горизонтально с разумным размером.

---

## F4. Skin Reinhard'а (player-skin override на трансформ)

### Контекст

Базовый мод `superheroes` в `src/client/java/.../mixin/AbstractClientPlayerSkinMixin.java` форсит **Classic (Steve / WIDE) геометрию** и накладывает hero-skin как **layer** (`HeroSkinLayer extends LivingEntityFeatureRenderer`). Этот feature-renderer регистрируется в `SuperheroesClient.onInitializeClient()` для всех hero-id.

Аддон `vanguard` ничего такого не делает: `ReinhardHero` зарегистрирован через `HeroApi.register(...)`, но **визуально** игрок остаётся ванильным Steve.

### Шаги

1. В `src/client/java/com/vanguard/mod/client/render/` создать `ReinhardSkinLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>`:
   ```java
   public class ReinhardSkinLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
       private static final ResourceLocation SKIN =
           ResourceLocation.fromNamespaceAndPath("vanguard", "textures/skin/reinhard.png");
       public ReinhardSkinLayer(RenderLayerParent<...> parent) { super(parent); }
       @Override public void render(PoseStack pose, MultiBufferSource buf, int light,
                                     AbstractClientPlayer player, ...) {
           if (!isReinhardTransformed(player)) return;
           // render parent model with our texture
           VertexConsumer vc = buf.getBuffer(RenderType.entityTranslucent(SKIN));
           getParentModel().renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
       }
   }
   ```
   `isReinhardTransformed(player)` — проверка через `RemoteHeroSkins.get(player.getUUID()).equals(ReinhardHero.ID)`.
2. Зарегистрировать в `VanguardClient.onInitializeClient()`:
   ```java
   LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, ctx) -> {
       if (entityRenderer instanceof PlayerRenderer pr) {
           registrationHelper.register(new ReinhardSkinLayer(pr));
       }
   });
   ```
3. **Sync remote players**: на стороне сервера в `VanguardNetworking.init()` шлём `S2C` с `Map<UUID, HeroId>` всем клиентам, чтобы `RemoteHeroSkins` знала про чужого Reinhard'а. (Проверить, есть ли это в base-mod's API; если есть — переиспользовать через `HeroApi.getCurrentHeroId(otherPlayer)`.)
4. Положить **реальный** skin-PNG в `src/main/resources/assets/vanguard/textures/skin/reinhard.png` (формат **64×64 Classic Steve geometry**, без slim).

### Источник скина (см. §F7)

### Acceptance

- После ПКМ по `vanguard:reinhard_suit` игрок виден с моделью Reinhard'а от первого И третьего лица (плащ, седые волосы, белая рубашка с золотыми элементами).
- Другие игроки на сервере тоже видят Reinhard'а, не Стива.
- При untransform — возвращается ванильный skin игрока.

---

## F5. HUD recolor: gold/ivory → crimson/scarlet

### Затронутые файлы

- `src/client/java/com/vanguard/mod/client/hud/WishRadialHud.java`
- `src/client/java/com/vanguard/mod/client/hud/ReinhardStatusHud.java`

### Палитра (новая)

| Назначение | Hex | Замена для |
|---|---|---|
| `COLOR_TEXT_IDLE` | `0xFFFFCBC8` (бледно-розовый) | старое gold-ivory |
| `COLOR_TEXT_ACTIVE` | `0xFFFFFFFF` (белый) | без изменений |
| `COLOR_KEY_IDLE` | `0xFF8A1A1A` (тёмно-алый) | старое золото |
| `COLOR_KEY_ACTIVE` | `0xFFFF3030` (яркий красный) | старое яркое золото |
| `COLOR_BORDER_IDLE` | `0xFF4A0808` (тёмно-багровый) | старая бронза |
| `COLOR_BORDER_ACTIVE` | `0xFFFF1414` (алый) | старое яркое золото |
| `COLOR_BORDER_ADAPTED` | `0xFFFFB000` (янтарь — adapted остаётся выделенным, но другим цветом) | старый синий |
| `COLOR_GLOW` | `0x66FF1414` | старое золотое-glow |
| `COLOR_PANEL_TOP` | `0xCC1A0808` | старое тёмно-золотое |
| `COLOR_PANEL_BOTTOM` | `0xCC0A0404` | старое тёмное |
| `COLOR_GOLD_FILL` (rename → `COLOR_BAR_FILL`) | `0xFFFF1414` | алый |
| `COLOR_GOLD_DIM` (rename → `COLOR_BAR_DIM`) | `0x88FF1414` | алый dim |
| Title accent (P4+) | `0xFFFF4040` | старое яркое золото |

### Шаги

1. Заменить константы в обоих файлах по таблице (имена констант лучше тоже переименовать `*_GOLD_*` → `*_RED_*` для читаемости).
2. `ReinhardStatusHud.drawTitle` — текст `"ASTREA · P{n}"` оставить, но при `phase >= 4` теперь `0xFFFF4040` вместо `0xFFFFE08A`.
3. `GoldenAuraController` (ВНИМАНИЕ — это сервер-сайд particle emitter, не HUD): пользователь сказал «худ красно-алым», но **золотая аура — отдельная фича** (P4+ канон Reinhard'а). Уточнить с пользователем перед изменением, **по умолчанию НЕ трогать** золотой particle-aura. Если пользователь скажет «и ауру тоже» — поменять `DUST_GOLD` на `new DustParticleOptions(new Vector3f(1.0f, 0.1f, 0.1f), 1.4f)` и `DUST_IVORY` на `new DustParticleOptions(new Vector3f(1.0f, 0.85f, 0.85f), 1.0f)`, заменить `ParticleTypes.GLOW` на `ParticleTypes.LAVA` или `ParticleTypes.FLAME`.

### Acceptance

- Открытие radial-N показывает красно-алую радиалку.
- Top-left status panel — красно-алая.
- Wish-pips — алые/тёмные вместо золотых.

---

## F6. Custom sounds → vanilla SoundEvents

### Что есть сейчас

```
assets/vanguard/sounds.json
assets/vanguard/sounds/phoenix/{blinding_light,dawnbreaker,knight_fall,restore}.ogg
assets/vanguard/sounds/spirit/{blast_big,blast_small,slash_horizontal,slash_vertical}.ogg
assets/vanguard/sounds/sword/{charge,crit,hit_shield,smash,swipe,thrust}.ogg
```

### Шаги

1. **Найти все обращения к `vanguard:`-sound** в коде:
   ```bash
   rg -n '"vanguard"' src --type java | rg -i 'sound|playSound'
   rg -n '\bvanguard:\w+\b' src/main --type java
   ```
2. Заменить на vanilla `SoundEvents.*`:

| Был | Стало |
|---|---|
| `vanguard:phoenix.blinding_light` | `SoundEvents.BEACON_ACTIVATE` |
| `vanguard:phoenix.dawnbreaker` | `SoundEvents.RESPAWN_ANCHOR_DEPLETE.value()` |
| `vanguard:phoenix.knight_fall` | `SoundEvents.PLAYER_BIG_FALL` |
| `vanguard:phoenix.restore` | `SoundEvents.PLAYER_LEVELUP` |
| `vanguard:spirit.blast_big` | `SoundEvents.GENERIC_EXPLODE.value()` |
| `vanguard:spirit.blast_small` | `SoundEvents.FIREWORK_ROCKET_BLAST` |
| `vanguard:spirit.slash_horizontal` | `SoundEvents.PLAYER_ATTACK_SWEEP` |
| `vanguard:spirit.slash_vertical` | `SoundEvents.PLAYER_ATTACK_STRONG` |
| `vanguard:sword.charge` | `SoundEvents.TRIDENT_RIPTIDE_1` |
| `vanguard:sword.crit` | `SoundEvents.PLAYER_ATTACK_CRIT` |
| `vanguard:sword.hit_shield` | `SoundEvents.SHIELD_BLOCK` |
| `vanguard:sword.smash` | `SoundEvents.ANVIL_LAND` |
| `vanguard:sword.swipe` | `SoundEvents.PLAYER_ATTACK_SWEEP` |
| `vanguard:sword.thrust` | `SoundEvents.PLAYER_ATTACK_STRONG` |

3. Удалить `assets/vanguard/sounds.json` и папку `assets/vanguard/sounds/` целиком (raw-файлы остаются в `art-source/` для будущего возврата).
4. Аналогично — пройтись по vanguard-particle типам (`vanguard:sword_explosion` etc). Если они **зарегистрированы как кастомные ParticleType** — оставить, потому что без них некоторые VFX (sword wave trail) перестанут рисоваться. Но **простые «декоративные»** particles (golden aura, hit sparks) можно заменить на vanilla (`ParticleTypes.SWEEP_ATTACK`, `ParticleTypes.CRIT`, `ParticleTypes.GLOW`, `ParticleTypes.END_ROD`, `ParticleTypes.FLAME`, `ParticleTypes.LAVA`).

### Acceptance

- В `mods/vanguard-0.1.1.jar` нет ни одного `.ogg` файла под `vanguard/sounds/`.
- Поиск `rg -n 'vanguard:[a-z_]+\.[a-z_]+' src/main --type java` (sound-style id-ов) даёт пустой результат.
- Все события (heaven's sword strike, sword wave, phoenix burst, sky vault landing) играют **vanilla** звуки.

---

## F7. Source open-source ассеты

Без этого пункт #4 и #5 пользователя не закрываются. Это **исследовательская работа**, не код-работа.

### Что искать

| Ассет | Где смотреть |
|---|---|
| **Reinhard van Astrea Minecraft skin** (Classic 64×64, не slim) | NameMC: `https://namemc.com/?q=Reinhard+Van+Astrea`, MinecraftSkins.com, Re:Zero-сообщество на planetminecraft |
| **3D-модель Dragon Sword Reid** (`.bbmodel` для Blockbench) | Не существует canonically. Альтернатива — взять generic «golden longsword + red gem»-меч из Soulsweapons, Spartan Weaponry, Tetra: `github:soulsweapons/Soulsweapons-Mod` ищем `.geo.json` golden-sword-like, либо `github:fuzs_/spartan_weaponry`. Готовый GeckoLib `.geo.json` под лицензией CC-BY/MIT. |
| **GeckoLib animation JSONs** для меча (`idle`, `swing`, `block`) | Сами создаём в Blockbench (Animations panel → export `.animation.json`). 3 анимации: `animation.dragon_sword_reid.idle` (1.0 sec, мелкое колебание), `animation.dragon_sword_reid.swing` (0.4s, ребяческий взмах), `animation.dragon_sword_reid.block` (0.2s loop, поднятие на блок). |
| **Player Animator JSONs** (для трансформа, фолла, воскрешения) | KosmX's Player-Animator — формат `.json` с JsonGZ; примеры в репо: `github:KosmX/EmotesMod-Animations`. Либо CurseForge → "player animation" → search «knight», «sword draw», «fall to knee», «resurrect». Лицензии обязательно проверить (предпочесть CC-BY/MIT). |

### Лицензионные правила (см. `.agents/skills/art-source/SKILL.md`)

- **Только** материалы под открытыми лицензиями (CC0, CC-BY, MIT, Apache-2.0).
- Если у пользователя есть друзья-мододелы которые разрешат — попросить **письменное** подтверждение, положить рядом с ассетом `.LICENSE.txt` с автором и разрешением.
- В PR-описании перечислить все добавленные ассеты + их источник + лицензию.

### Шаги

1. Создать `art-source/reinhard-skin/` — собрать туда 3-5 кандидатов skin-PNG. Best one → `assets/vanguard/textures/skin/reinhard.png`.
2. Создать `art-source/dragon-sword-reid/` — `.bbmodel` источник + экспортированные `.geo.json` + `.animation.json` + `.png` texture.
3. Создать `art-source/player-animations/` — JSON-ы под KosmX Player Animator + soft-dep описание в `fabric.mod.json`.
4. Каждый положенный ассет — записать в `docs/asset-research.md` (источник, автор, лицензия, дата).

### Acceptance

- Все три категории (skin / sword / animations) — **не** procedurally-generated placeholders, а реальные ассеты с провенансом.
- `docs/asset-research.md` заполнен и валиден.

---

## F8. Наложение моделей (item overlap)

### Гипотеза

GeckoLib `GeoItemRenderer.renderByItem(...)` рисует 3D-модель в hand. Параллельно ванильная item-rendering pipeline видит обычный `models/item/dragon_sword_reid.json` (если он `parent: item/handheld`) и рисует **2D sprite в той же руке**. Результат — два меча в руке одновременно (один 3D, один 2D-плоский с magenta).

### Шаги

1. Заменить `assets/vanguard/models/item/dragon_sword_reid.json` на:
   ```json
   { "parent": "builtin/entity",
     "gui_light": "front",
     "display": {
       "gui": { "scale":[1.0,1.0,1.0] },
       "ground": { "scale":[0.5,0.5,0.5] },
       "fixed": { "scale":[1.0,1.0,1.0] },
       "thirdperson_righthand": { "rotation":[0,90,-55], "translation":[0,4,0.5], "scale":[1,1,1] },
       "firstperson_righthand": { "rotation":[0,-90,25], "translation":[1.13,3.2,1.13], "scale":[0.68,0.68,0.68] }
     }
   }
   ```
2. В GUI inventory меч **не** покажется через GeckoLib — нужен дополнительно `BuiltInRegistries.ITEM` + `ItemModelGenerators` либо отдельный 2D-fallback (через `geckolib`'s `createGeoRenderer` для GUI). См. `geckolib`-wiki `Item Animations § GUI Render`.
3. Убедиться что **в скрытой второй руке (offhand)** меч не рендерится дважды — если рендерится, проверить `DragonSwordReidRenderer` `renderByItem` пропускает `displayContext == ItemDisplayContext.NONE` или подобное.

### Acceptance

- В руке (third-person) — **один** меч, 3D, с реальной текстурой.
- В инвентаре — единая иконка (2D или GUI-rendered 3D).
- В offhand — никаких дубликатов.

---

## 3. Acceptance criteria всего hotfix-релиза

Для пометки v0.1.1 как «готово»:

- [ ] Build clean: `./gradlew build --no-daemon -x test` → `BUILD SUCCESSFUL`.
- [ ] Запуск dev-клиента (`./gradlew runClient`) → трансформ в Reinhard работает, скин видно, меч с текстурой, HUD красно-алый, ни одного magenta-checker.
- [ ] `rg -n 'mana' src/main --type java` — только false-positives (`Manager`).
- [ ] `rg -n 'vanguard:[a-z_]+\.[a-z_]+' src/main --type java` — пусто.
- [ ] `ReinhardHero.getEnergyMax() == 1000f`, `getEnergyRegenPerTick() == 5.0f`.
- [ ] Все файлы в `art-source/` для добавленных ассетов имеют `.LICENSE.txt`.
- [ ] `docs/asset-research.md` обновлён.
- [ ] PR-description перечисляет каждое исправление по пунктам 1-8 из жалоб.
- [ ] Релиз `v0.1.1` создан через `gh release create` с jar-ом и sources-jar-ом.

---

## 4. Что **НЕ** входит в этот hotfix

- Мерж в `main` основного мода/аддона. Релиз с тэга на feature-ветке.
- Балансировка cooldown-ов / damage-чисел способностей сверх энерго-скейла.
- Phoenix-анимация падения (ждём ассеты от друзей-мододелов пользователя).
- Datagen рецептов / loot tables.
- Новые способности.
- Изменения в base mod (`grebeshok105/grebeshok105`).

---

## 5. Open questions для пользователя

Если исполнитель упрётся — **не** догадываться, а спросить:

1. **Аура P4+** — оставить золотой (канон Reinhard'а) или тоже красно-алым?
2. **Skin Reinhard'а** — slim (Alex 64×64) или classic (Steve 64×64)? Сейчас по умолчанию classic, как форсит base mod.
3. **Если 0f mana max нарушает API base mod** — какой минимальный non-zero допустим? (Если `Hero` interface валидирует `> 0`, выбрать `1f` и скрыть из HUD.)
4. **Кастомные particle types** для Sword Wave / Heaven's Sword — оставить (vanguard:sword_explosion) или тоже на vanilla?
5. **Sword Aegis (V)** — раньше пользователь его убрал, потом добавили Sword Wave. Подтвердить что V → Sword Wave (текущее поведение) сохраняется в hotfix.

---

## 6. Финальный шаг — релиз

```bash
# на ветке hotfix-v0.1.1, после merge PR в bootstrap-addon-skills
git fetch origin
git checkout origin/devin/1777714426-bootstrap-addon-skills
./gradlew clean build --no-daemon -x test
ls build/libs/  # должно быть vanguard-0.1.1.jar и vanguard-0.1.1-sources.jar
git tag -a v0.1.1 -m "Vanguard v0.1.1 — hotfix (texture/skin/HUD/mana/energy)"
git push origin v0.1.1
gh release create v0.1.1 \
  --repo grebeshok105/boreben-mod \
  --title "Vanguard v0.1.1 — hotfix" \
  --notes-file docs/release-notes/v0.1.1.md \
  build/libs/vanguard-0.1.1.jar \
  build/libs/vanguard-0.1.1-sources.jar
```

---

**Конец плана.**
