# Reinhard van Astrea — план первого героя в Vanguard аддоне

> **Адаптация для аддона.** Изначально план был написан в основном моде `superheroes` (см. оригинал в `grebeshok105/grebeshok105` — PR #51, файл удалён с baseline после имплементации). В Vanguard-аддоне мы реализуем его поверх **публичного API** основного мода и под **собственным неймспейсом `vanguard`**.

## Ключевые отличия от оригинального плана

| Параметр | Оригинал (в основном моде) | В Vanguard-аддоне |
|---|---|---|
| Hero ID | `superheroes:reinhard` | `vanguard:reinhard` |
| Java package классов | `com.example.superheroes.hero.*` | `com.vanguard.mod.hero.*` |
| Ассеты | `assets/superheroes/...` | `assets/vanguard/...` |
| Регистрация героя | `Heroes.register(REINHARD)` (internal) | через **публичное API** основного мода (`HeroRegistry.register(...)`) — этот API нужно сначала вынести в основной мод (Этап 2) |
| Атрибуты | `HeroAttributes.java` (internal) | через публичный API `HeroAttributesBuilder` или модификации создаются прямо в аддоне |
| Item group | `ModItemGroups.SUPERHEROES_TAB` (internal) | `ItemGroupEvents.modifyEntriesEvent(superheroes:superheroes)` (Fabric API) |
| Меч 3D-модель | vanilla item model | **GeckoLib** (`com.vanguard.mod.client.render.item.DragonSwordReidRenderer` extends GeoItemRenderer) |

## Этапность

1. **Этап 2 (PR в основной мод)** — вынести в публичное API: `Hero` interface, `HeroRegistry.register(...)`, `Ability` interface, `AbilityRegistry.register(...)`, `HeroTransformService.transform/untransform`, `HeroData` getters, `ResourceController` consume API, base `HeroAttributesBuilder`. Не меняет существующее поведение, только переэкспозит уже работающие классы под пакетом `com.example.superheroes.api`.
2. **Этап 3** — собрать jar основного мода с этим API, выложить в GitHub release `grebeshok105/grebeshok105/releases`.
3. **Этап 4** — реализовать Reinhard в аддоне поверх этого API.

---

## 0. Подтверждённые решения пользователя

1. **Трансформация через меч**: отдельного pendant/insignia не нужно.
2. **Меч — отдельный item**: вне формы Reinhard он годится только для превращения в героя; пользоваться им как оружием/ability-key можно только в форме Reinhard.
3. **Баланс — лорно OP**: Reinhard должен быть намеренно сильнее текущих героев, с ощущением Sword Saint / Divine Protections.
4. **3D-модель меча**: через GeckoLib (required dep аддона). Анимации — кастомные (idle / draw / sheathe / Reid release charge).

## 1. Концепт героя

### ID / название

- Hero ID: `vanguard:reinhard`
- Display EN: `Reinhard van Astrea`
- Display RU: `Рейнхард ван Астрея`
- Основной ресурс: `Energy` как stamina/Divine Protection reserve.
- Mana: `0`.

### Фантазия геймплея

Reinhard — лорно-OP swordmaster с высоким burst-уроном, защитными Divine Protection proc-ами и ультимативным ударом мечом Reid. Он не летает, но очень быстро двигается, игнорирует падение и должен ощущаться как почти непобедимый дуэлянт.

## 2. Ассеты из `art-source/rezero-fx-textures.zip`

В архиве уже есть подходящие Re:Zero ассеты:

- Skin/texture Reinhard:
  - `FX + TEXTURES REZERO/rezeromc/textures/entities/reinhard-van-astrea-re-zero-on-planetminecraft-com.png`
- Sword / Reid 2D-textures:
  - `FX + TEXTURES REZERO/rezeromc/textures/item/dragonsword.png`
  - `FX + TEXTURES REZERO/rezeromc/textures/item/dragonswordreidnew1.png`
  - `FX + TEXTURES REZERO/rezeromc/textures/item/reidstick.png`
  - `FX + TEXTURES REZERO/rezeromc/textures/entities/reidastreatexture.png` — для GeckoLib-меча в "Reid"-форме
- VFX particles:
  - `FX + TEXTURES REZERO/rezeromc/textures/particle/swordexplosion.png`
  - `FX + TEXTURES REZERO/rezeromc/textures/particle/sword_explosion_1.png` … `sword_explosion_6.png`
- UI refs:
  - `FX + TEXTURES REZERO/rezeromc/textures/screens/rezeromcbuttonswordsmanship.png`
  - `FX + TEXTURES REZERO/rezeromc/textures/screens/rezeromcbuttondivineprotection.png`
- 3D-модель меча — **готовой `.geo.json` для Reid в архиве нет**. Кандидаты на адаптацию:
  - `FX + TEXTURES REZERO/soulsweapons/geo/entity/freyr_sword.geo.json` + соответствующая animation.json — общий шаблон сводного меча (большой, длинный клинок). Можно сделать форк под Reid.
  - Либо собрать собственный `.bbmodel` в Blockbench по эталонной форме (длинный двуручник с фиолетово-золотой рукоятью).

Runtime copy targets (под `vanguard`-неймспейсом):

- `src/main/resources/assets/vanguard/textures/entity/hero/reinhard.png`
- `src/main/resources/assets/vanguard/textures/item/dragon_sword_reid.png` (2D fallback / inventory icon)
- `src/main/resources/assets/vanguard/geo/item/dragon_sword_reid.geo.json` (GeckoLib model)
- `src/main/resources/assets/vanguard/animations/item/dragon_sword_reid.animation.json`
- `src/main/resources/assets/vanguard/textures/item/dragon_sword_reid_geo.png` (текстура для GeckoLib-модели)
- `src/main/resources/assets/vanguard/textures/particle/reinhard_sword_explosion_*.png`

## 3. Hero stats/passives

### `ReinhardHero`

Файл: `src/main/java/com/vanguard/mod/hero/ReinhardHero.java` (implements публичный `Hero` интерфейс из API основного мода)

Поля:

- `ID = VanguardIds.of("reinhard")` (`vanguard:reinhard`)
- `SKIN = VanguardIds.of("textures/entity/hero/reinhard.png")`
- `THEME` — собственная цветовая тема, локально в классе (не трогаем `HeroTheme` основного мода)

Базовые значения:

- Energy max: `500`
- Energy regen: `3.0/tick`
- Mana max: `0`
- Dimensions: vanilla player `0.6 x 1.8`

Атрибуты (через публичный `HeroAttributesBuilder` API):

- Armor: `28`
- Armor toughness: `14`
- Attack damage: `14`
- Attack speed: `2.0`
- Movement speed: `+60% base`
- Max health: `+40`
- Knockback resistance: `0.9`
- Step height: `+1.0`

Пассивные эффекты при трансформации:

- `DAMAGE_RESISTANCE II`
- `REGENERATION I`
- Fall damage cancelled.

## 4. Меч Reid / Dragon Sword

### Предмет

Файл: `src/main/java/com/vanguard/mod/item/DragonSwordReidItem.java`

Регистрация:

- `VanguardItems.DRAGON_SWORD_REID` (id `vanguard:dragon_sword_reid`)
- inventory texture: `assets/vanguard/textures/item/dragon_sword_reid.png`
- in-world model: GeckoLib через `GeoItemRenderer<DragonSwordReidItem>` (см. секцию 4.1)
- lang:
  - EN: `Dragon Sword Reid`
  - RU: `Драконий меч Рейд`

Поведение:

- `stacksTo(1)`, `fireResistant()`, `rarity(EPIC)`, durability `2500`.
- `use()` вне формы Reinhard вызывает публичный `HeroTransformService.transform(player, vanguard:reinhard)`.
- Вне формы Reinhard меч **не должен работать как оружие**: melee damage минимальный/нулевой, durability не тратится, abilities не активируются.
- В форме Reinhard меч раскрывается как OP-оружие: высокий melee damage, sweep/crit VFX, доступ к sword abilities.
- Shift-use в форме Reinhard — `HeroTransformService.untransform(player)`.
- Не должен теряться при смерти: использовать `HeroEquipmentLock`-аналог если он публичен в API основного мода, иначе хранить в персистентном attachment самого аддона.

### 4.1 GeckoLib рендер меча

Класс: `src/client/java/com/vanguard/mod/client/render/item/DragonSwordReidRenderer.java`
- extends `GeoItemRenderer<DragonSwordReidItem>`
- `geo`: `vanguard:geo/item/dragon_sword_reid.geo.json`
- `animation`: `vanguard:animations/item/dragon_sword_reid.animation.json`

Анимации (минимум):
- `idle` — slow rotation/glow
- `draw` — при `use()` снаружи формы (трансформация)
- `reid_release` — charge для ультимата (Reid Draw)
- `swing` — обычный взмах в форме Reinhard (можно через vanilla animation если сложно)

`DragonSwordReidItem implements GeoItem` — реализовать через `AnimatableInstanceCache`, обработчик controller-ов в `registerControllers(...)`.

Регистрация renderer-а в `VanguardClient.onInitializeClient`:
```java
GeoItemRenderer.register(VanguardItems.DRAGON_SWORD_REID, new DragonSwordReidRenderer());
```

## 5. Трансформация через меч

`DragonSwordReidItem` совмещает:

- transformation behavior при `use()` вне формы Reinhard (через публичный `HeroTransformService`);
- untransform behavior при shift-use в форме Reinhard;
- locked combat behavior: не-Reinhard не может пользоваться мечом как оружием (`getAttackDamageBonus = 0`);
- full combat behavior: Reinhard получает весь урон/ability synergy меча.

Реализация — `extends Item implements GeoItem`, ручная логика трансформации внутри `use()`.

## 6. Abilities

Все abilities регистрируются через публичный `AbilityRegistry.register(id, ability)` основного мода под `vanguard:*` IDs.

### 6.1 Sword Saint Dash

ID: `vanguard:reinhard_sword_saint_dash`
Файл: `ability/ReinhardSwordSaintDashAbility.java`
- Cost: `45 energy`. Cooldown: `4s`.
- Требует `DRAGON_SWORD_REID` в main/offhand и форму Reinhard.
- Игрок рывком на 12–16 блоков.
- LivingEntity в линии получают `28–36` damage. Сильный knockback по направлению.
- Particles `SWEEP_ATTACK`, `CRIT`, custom `vanguard:reinhard_sword_explosion_*`.

### 6.2 Divine Protection

ID: `vanguard:reinhard_divine_protection`
Файл: `effect/ReinhardDivineProtectionController.java`
- Passive controller.
- Когда Reinhard получает урон, раз в `8–12s` срабатывает защита:
  - снижение damage на 80–95% (через публичный damage hook основного мода или Fabric `ServerLivingEntityEvents.ALLOW_DAMAGE`);
  - knockback атакующему;
  - короткий `ABSORPTION` + `REGENERATION`;
  - тушит огонь и снимает негативные эффекты, кроме void/kill.

### 6.3 Reid Draw / Dragon Sword Release

ID: `vanguard:reinhard_reid_draw` (ultimate)
Файл: `ability/ReinhardReidDrawAbility.java`
- Требует `DRAGON_SWORD_REID`. Cost: `180 energy`. Cooldown: `30s`.
- Charge `20–30 ticks` с GeckoLib-анимацией `reid_release` и vfx.
- Конус: range `18`, angle `70°`. Damage: `70–100`.
- Сильный knockback + flash/sound. Не разрушает блоки.

### 6.4 Astrea Counter

ID: `vanguard:reinhard_astrea_counter`
Файл: `ability/ReinhardAstreaCounterAbility.java`
- Toggle/charge `2s`.
- В окне parry — cancel/reduce damage и ответный slash.
- Cooldown: `8s`.

## 7. Damage types / datagen

Новые damage types (под неймспейсом `vanguard`):

- `vanguard:reinhard_sword_dash`
- `vanguard:reinhard_reid_draw`
- `vanguard:reinhard_counter`

Файлы/правки:

- `damage/VanguardDamageTypes.java`
- `datagen/VanguardDamageTypeProvider.java`
- generated JSON в `src/main/generated/data/vanguard/damage_type/`
- lang death messages EN/RU.

## 8. Registration checklist (адаптировано под аддон)

Код:

- `hero/ReinhardHero.java`
- `hero/VanguardHeroes.init()` — регистрация через публичный `HeroRegistry.register(REINHARD)` из API основного мода (вызывать в `VanguardMod.onInitialize()`)
- `ability/VanguardAbilityIds.java`
- `ability/VanguardAbilityRegistry.init()` — регистрация через публичный `AbilityRegistry.register(...)`
- `effect/ReinhardDivineProtectionController.java` — `init()` зов в `VanguardMod.onInitialize()`
- `item/DragonSwordReidItem.java` (implements `GeoItem`)
- `item/VanguardItems.java`
- `item/VanguardItemGroupHook.java` — добавить меч в `superheroes:superheroes` tab через `ItemGroupEvents.modifyEntriesEvent`
- `damage/VanguardDamageTypes.java`
- `client/render/item/DragonSwordReidRenderer.java`
- `client/VanguardClient.java` — регистрация GeoItemRenderer

Assets/resources (всё под `vanguard`-неймспейсом!):

- `assets/vanguard/textures/entity/hero/reinhard.png`
- `assets/vanguard/textures/item/dragon_sword_reid.png` (inventory icon)
- `assets/vanguard/textures/item/dragon_sword_reid_geo.png` (для GeckoLib-модели)
- `assets/vanguard/models/item/dragon_sword_reid.json` (parent: builtin/entity для геколиб)
- `assets/vanguard/geo/item/dragon_sword_reid.geo.json`
- `assets/vanguard/animations/item/dragon_sword_reid.animation.json`
- `assets/vanguard/textures/particle/reinhard_sword_explosion_*.png`
- `assets/vanguard/lang/en_us.json`
- `assets/vanguard/lang/ru_ru.json`

Datagen:

- если item models генерируются через `VanguardItemModelProvider`, добавить туда `dragon_sword_reid` (с parent на `builtin/entity` для GeckoLib).
- выполнить `./gradlew runDatagen --no-daemon`.

## 9. Баланс v1

Рекомендуемый старт как лорно-OP герой:

- Reinhard без активных abilities уже сильнее большинства героев по melee/defense.
- Reinhard с Reid: OP burst, короткие cooldowns, высокий урон.
- Нет постоянного creative-flight.
- Практически неубиваем в обычном бою, но без полного бессмертия против void/kill commands.
- Divine Protection имеет короткий cooldown и сильно режет обычный урон.
- Ultimate не ломает блоки; по обычным мобам может ваншотить, по major bosses — damage cap по необходимости.

## 10. Риски

- **Публичный API основного мода** обязателен. Без него аддон вынужден импортить `com.example.superheroes.*` internal классы — хрупко при апдейтах. Этап 2 (вынести API) — блокер для Этапа 4.
- Damage reduction/counter требует hook-а в damage pipeline. Если в API основного мода нет такой точки — добавить её в Этапе 2 либо использовать общий `ServerLivingEntityEvents.ALLOW_DAMAGE`.
- Меч — предмет трансформации, не дублировать его при transform/untransform и не терять при смерти.
- Сторонние Re:Zero assets из `art-source/` — добавить `source.txt` рядом с runtime-копиями.
- GeckoLib-модель меча: если в архиве не найдём готовую, делаем `.bbmodel` сами либо v1 идёт без 3D-меча (только 2D inventory texture), а GeckoLib подключаем во v2.

## 11. Минимальный первый PR реализации

Чтобы быстро получить playable героя:

1. Reinhard hero + attributes + skin (`vanguard:reinhard`).
2. Dragon Sword Reid item (без GeckoLib пока — vanilla 2D model) как transformation item + locked weapon behavior.
3. 2 abilities: `Sword Saint Dash`, `Reid Draw`.
4. Lang EN/RU, models, добавление в creative tab основного мода.
5. Datagen + build.
6. Hook в `superheroes:superheroes` creative tab.

`Divine Protection`, `Astrea Counter`, и **GeckoLib 3D-меч** — отдельным PR, если первый будет слишком большим.

## 12. Проверка

Команды:

```bash
export JAVA_HOME=/home/ubuntu/jdk-21.0.2 && export PATH=$JAVA_HOME/bin:$PATH

# собрать сам аддон
./gradlew runDatagen --no-daemon
./gradlew build --no-daemon -x test
```

Manual smoke-test (после установки jar основного мода + аддона в `mods/`):

- Меч Reid появляется в creative-вкладке `Superheroes Mod`.
- ПКМ мечом вне формы Reinhard — трансформирует.
- Скин Reinhard отображается, атрибуты применены.
- ПКМ мечом в форме Reinhard работает как оружие; shift-ПКМ — выходит из формы.
- Меч НЕ работает как оружие вне формы Reinhard.
- Способности срабатывают, energy тратится, cooldown отрабатывает.
- Без основного мода `superheroes` Fabric корректно ругается и не запускает аддон.

## 13. Зависимости в build.gradle / fabric.mod.json

Полный набор требуемых зависимостей (на основе картинки от пользователя):

```gradle
dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings loom.officialMojangMappings()
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"

    // Required deps аддона
    modImplementation "software.bernie.geckolib:geckolib-fabric-1.21:4.5.8"            // 3D + анимации меча
    modImplementation "dev.kosmx.player-anim:player-animation-lib-fabric:2.0.0+1.21"   // playerAnimator — анимации Reinhard
    // Photon (KilaBash) и LDLib (KilaBash) — TODO: уточнить maven coords (обычно через
    // https://maven.firstdarkdev.xyz/ или https://maven.kilabash.com/)
    // Kleiders Custom Renderer API — TODO: maven coords (modrinth slug "kleiders-custom-renderer-api")

    // Основной мод
    modImplementation files("libs/superheroes-2.2.2.jar")  // или из релиза

    // JEI optional
    // modCompileOnly "mezz.jei:jei-1.21-fabric-api:..."
}
```

```json
{
  "depends": {
    "fabricloader": ">=0.19.2",
    "minecraft": "~1.21",
    "java": ">=21",
    "fabric-api": "*",
    "geckolib": ">=4.5",
    "player-animator": "*",
    "superheroes": "*"
  },
  "suggests": {
    "jei": "*"
  }
}
```

> Финальные maven coords для **Photon**, **LDLib**, **Kleiders Custom Renderer API** — уточнить при стадии Этап 4 через Modrinth API / поиск по их репозиториям. Если они нужны прямо сейчас — добавить, иначе можно отложить до использования.
