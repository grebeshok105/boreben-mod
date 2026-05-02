# Vanguard Asset Research — Reinhard van Astrea

Этап ассет-сбора для героя `vanguard:reinhard`. Распаковка `art-source/rezero-fx-textures.zip` + ресёрч внешних мод-репозиториев на предмет кандидатов для phoenix-fall / knight-death / golden-ascension анимаций.

> Распакованный zip лежал в `/tmp/rezero/FX + TEXTURES REZERO/` (NOT committed — путь в `.gitignore` через шаблон `art-source/rezero_fx_extracted/`, временная папка вне репо).

## 1. Извлечённое из `art-source/rezero-fx-textures.zip`

### 1.1. Spirit-effect текстуры → `assets/vanguard/textures/effect/spirit_*.png`

Для дальних spirit-strikes Reinhard'а (партиклы, ауры, sword-waves):

| Куда положил | Из чего | Размер | Описание |
|---|---|---|---|
| `effect/spirit_dona.png` | `rezeromc/textures/donaspirit.png` | 16x16 | Малый dark-spirit партикл |
| `effect/spirit_elemental.png` | `rezeromc/textures/elementalspirit.png` | 32x32 | Generic elemental spirit aura |
| `effect/spirit_yang.png` | `rezeromc/textures/entities/yangspirit.png` | 16x16 | Light-spirit (золотисто-белый) — кандидат для divine-protection FX |
| `effect/spirit_yin.png` | `rezeromc/textures/entities/yinspirit.png` | 16x16 | Dark-spirit (тёмно-фиолетовый) контраст к yang |
| `effect/spirit_earth.png` | `rezeromc/textures/entities/playerearthspirit.png` | 16x16 | Earth elemental |
| `effect/spirit_fire.png` | `rezeromc/textures/entities/playerfirespirit.png` | 16x16 | Fire elemental |
| `effect/spirit_water.png` | `rezeromc/textures/entities/playerwaterspirit.png` | 16x16 | Water elemental |
| `effect/spirit_wind.png` | `rezeromc/textures/entities/playerwindspirit.png` | 16x16 | Wind elemental |
| `effect/spirit_great_zarestia.png` | `rezeromc/textures/entities/greatspiritzarestiatexture.png` | 64x64 | Great spirit Zarestia (high-tier divine) |
| `effect/spirit_glow.png` | `rezeromc/textures/entities/guiltylowenewglow.png` | 128x128 | Generic glow-mask (для overlay-rendering) |
| `effect/spirit_wave.png` | `soulsweapons/textures/entity/moonveil_wave.png` | 64x64 | **Sword-wave проекция — основной кандидат для distant slash FX** |
| `effect/spirit_aura_levi.png` | `soulsweapons/textures/entity/leviathan_axe_glowmask.png` | 128x128 | Glow-mask вариант 2 |
| `effect/spirit_aura_mjolnir.png` | `soulsweapons/textures/entity/mjolnir_glowmask.png` | 64x64 | Glow-mask вариант 3 |
| `effect/spirit_slash_1..6.png` | `rezeromc/textures/particle/sword_explosion_{1..6}.png` | 64x64 | 6-frame strip для анимированного sword-slash explosion |

### 1.2. Phoenix / феникс / divine / golden → `assets/vanguard/textures/effect/phoenix_*.png`

Для воскрешения и золотой ауры (Reinhard's Divine Protection / phoenix-revive):

| Куда положил | Из чего | Размер | Описание |
|---|---|---|---|
| `effect/phoenix_feather.png` | `rezeromc/textures/particle/phoenixfeather.png` | 16x16 | **Главный feather-particle** — золотое перо |
| `effect/phoenix_golden_heart.png` | `powerborne/textures/gui/abilities/sentry/golden_heart.png` | 16x16 | Golden heart — divine-revive icon |
| `effect/phoenix_ascension.png` | `powerborne/textures/mob_effect/solar_ascension.png` (resize 18→16) | 16x16 | Solar ascension symbol — для воскрешения |
| `effect/phoenix_golden_drape.png` | `powerborne/textures/models/suits/sentry/black_and_gold_cape.png` | 32x32 | Чёрно-золотая текстура для overlay/трансформационного капюшона |

### 1.3. Sword (Dragon Sword Reid)

#### Item-icon (2D inventory)

| Куда положил | Из чего | Размер | Описание |
|---|---|---|---|
| `textures/item/dragon_sword_reid.png` | `rezeromc/textures/item/dragonswordreidnew1.png` | 16x16 | Основной item-icon (Reid форма) |
| `textures/item/dragon_sword_reid_dormant.png` | `rezeromc/textures/item/dragonsword.png` | 16x16 | Dormant-форма (без активации Reid) |
| `textures/item/dragon_sword_reid_stick.png` | `rezeromc/textures/item/reidstick.png` | 16x16 | Кандидат на «прутик»-вариант (можно проигнорировать) |

#### Entity-texture (для GeckoLib рендера)

| Куда положил | Из чего | Размер | Описание |
|---|---|---|---|
| `textures/entity/dragon_sword_reid.png` | `soulsweapons/textures/entity/freyr_sword.png` | 64x64 | **Совпадает с UV-маппингом** `freyr_sword.geo.json` (texture_width/height = 32) |
| `textures/entity/dragon_sword_reid_skin.png` | `rezeromc/textures/entities/reidastreatexture.png` | 128x128 | Reid-themed skin вариант (требует UV-remap, не drop-in) |

#### GeckoLib model + animations

| Куда положил | Из чего | Изменения |
|---|---|---|
| `geo/dragon_sword_reid.geo.json` | `soulsweapons/geo/entity/freyr_sword.geo.json` | `geometry.unknown` → `geometry.dragon_sword_reid`. (Префикса `geckolib3:` в исходнике не было; формат — Bedrock 1.12.0, валиден в GeckoLib 4.5+.) |
| `animations/dragon_sword_reid.animation.json` | `soulsweapons/animations/entity/freyr_sword.animation.json` | без изменений; содержит `idle`, `attack_north`, `attack_east` |

> Для полноценного flow `idle / draw / sheathe / Reid release charge` нужно дописать `draw`, `sheathe`, `charge` секции — текущий шаблон даёт **только idle + базовые swing'и**. См. раздел 3.

### 1.4. Sword particle FX → `assets/vanguard/textures/particle/`

| Куда положил | Из чего | Размер |
|---|---|---|
| `particle/sword_explosion.png` (+ `.mcmeta`) | `rezeromc/textures/particle/swordexplosion.png` (+ mcmeta) | 64x384 (animated 6-frame, mcmeta задаёт animation) |
| `particle/sword_explosion_{1..6}.png` | `rezeromc/textures/particle/sword_explosion_{1..6}.png` | 64x64 (отдельные кадры, дублируют strip) |

### 1.5. Sounds → `assets/vanguard/sounds/`

Все источники уже в OGG, конвертация не нужна. Регистрация в `assets/vanguard/sounds.json`:

| Куда положил | Из чего | Sound event |
|---|---|---|
| `sounds/spirit/slash_horizontal.ogg` | `soulsweapons/sounds/moonveil_horizontal.ogg` | `vanguard:spirit_slash_horizontal` |
| `sounds/spirit/slash_vertical.ogg` | `soulsweapons/sounds/moonveil_vertical.ogg` | `vanguard:spirit_slash_vertical` |
| `sounds/spirit/blast_big.ogg` | `soulsweapons/sounds/moonlight_big.ogg` | `vanguard:spirit_blast_big` |
| `sounds/spirit/blast_small.ogg` | `soulsweapons/sounds/moonlight_small.ogg` | `vanguard:spirit_blast_small` |
| `sounds/sword/charge.ogg` | `soulsweapons/sounds/knight_charge_sword.ogg` | `vanguard:sword_charge` |
| `sounds/sword/thrust.ogg` | `soulsweapons/sounds/knight_thrust_sword.ogg` | `vanguard:sword_thrust` |
| `sounds/sword/smash.ogg` | `soulsweapons/sounds/knight_sword_smash.ogg` | `vanguard:sword_smash` |
| `sounds/sword/swipe.ogg` | `soulsweapons/sounds/knight_swipe.ogg` | `vanguard:sword_swipe` |
| `sounds/sword/crit.ogg` | `soulsweapons/sounds/crit_hit.ogg` | `vanguard:sword_crit` |
| `sounds/sword/hit_shield.ogg` | `soulsweapons/sounds/sword_hit_shield.ogg` | `vanguard:sword_hit_shield` |
| `sounds/phoenix/restore.ogg` | `soulsweapons/sounds/restore.ogg` | `vanguard:phoenix_restore` |
| `sounds/phoenix/knight_fall.ogg` | `soulsweapons/sounds/knight_death.ogg` | `vanguard:phoenix_knight_fall` |
| `sounds/phoenix/blinding_light.ogg` | `soulsweapons/sounds/blinding_light_explosion.ogg` | `vanguard:phoenix_blinding_light` |
| `sounds/phoenix/dawnbreaker.ogg` | `soulsweapons/sounds/dawnbreaker_sound.ogg` | `vanguard:phoenix_dawnbreaker` |

## 2. Кандидаты на phoenix-fall / knight-death анимации (внешние моды)

### 2.1. Под разрешающей лицензией (можно использовать сразу)

| Мод | URL | Лицензия | Файлы / анимации | Заметки |
|---|---|---|---|---|
| **Marium's Soulslike Weaponry** (источник)| https://github.com/mariumbacchus/Soulslike-Weaponry (branch `1.21.1`) | **CC0-1.0** | `assets/soulsweapons/animations/entity/`: `returning_knight.animation.json` (`death`, `obliterate`, `unbreakable`, `spawn`, `blinding_reflection`, `summon_warriors`, `rupture`, `mace_of_spades`); `moonknight.animation.json` (`death_phase_2`, `phase_1`, `phase_2`, `blinding_light`, `obliterate_phase_1/2`, `core_beam_phase_2`, `unbreakable`); `chaos_monarch.animation.json` (`death`, `lightning_call`, `barrage`, `teleport`); `accursed_lord.animation.json` (`animation.model.death`, `animation.model.spawn`); `withered_demon.animation.json` (`death`); `frost_giant.animation.json` (`death`); `night_prowler.animation.json` (`death_1`, `death_2`, `engulf_*`, `darkness_rise_*`); `holy_moonlight_pillar.animation.json` (`emerge`, `idle` — **золотая колонна возносится — кандидат для phoenix-ascension FX**) | **Это родительский мод нашего zip.** В `art-source/rezero-fx-textures.zip` уже лежат `freyr_sword.geo.json/animation.json` + `holy_moonlight_pillar.geo.json/animation.json` — но `returning_knight.geo.json/animation.json`, `moonknight.geo.json/animation.json`, `chaos_monarch.geo.json/animation.json` **в zip НЕ присутствуют**. Их можно вытащить напрямую из репо при следующем этапе. CC0 → можно копировать как есть, без attribution. |
| **GeckoLib examples** | https://github.com/bernie-g/geckolib-examples | **MIT** | example entity / item animations (Bat, Parasite, Replaced Creeper) | Знание формата, но «рыцарь падает» нет — только демо-сущности. Использовать как **референс структуры animation.json**. |
| **GeckoLib core** | https://github.com/bernie-g/geckolib | **MIT** | API + кейframe-easings, sound-keyframes, particle-keyframes | Не источник готовых анимаций, а движок. Уже зависимость аддона. |
| **KosmX PlayerAnimator** | https://github.com/KosmX/minecraftPlayerAnimator | **MIT** | Library only | DEPRECATED upstream → автор рекомендует [PAL by ZigyTheBird](https://docs.zigythebird.com/pal/how_to_port_from_player_animator/). Нет готовых dying/phoenix-fall анимаций в самом репо. |
| **fabricPlayerAnimatorExample** | https://github.com/KosmX/fabricPlayerAnimatorExample | **CC0-1.0** | `assets/modid/player_animation/waving.json` (Emotecraft-format) | Только waving. Полезен как **референс формата `.json` для KosmX player-animation** (поля: `version`, `uuid`, `name`, `emote.moves[].rightLeg/leftArm/...`). |

### 2.2. С viral / copyleft лицензией (использовать осторожно)

| Мод | URL | Лицензия | Заметки |
|---|---|---|---|
| **Epic Fight** | https://github.com/Epic-Fight/epicfight | **GPL-3.0** | Огромный набор боевых анимаций (knight stances, sword swings, kneel-style attacks). Лицензия viral copyleft — **использование `.animation.json` как «data assets» обычно не запускает GPL**, но если копировать классы рендера — мод придётся делать GPL-3.0. Сейчас Vanguard под `CC0-1.0`. **Рекомендация**: только как референс движений; не копировать файлы напрямую без согласования. |

### 2.3. Несовместимые лицензии (НЕ использовать)

| Мод | URL | Лицензия | Заметки |
|---|---|---|---|
| **Born in Chaos** | https://modrinth.com/mod/borninchaos / https://www.curseforge.com/minecraft/mc-mods/born-in-chaos | **All Rights Reserved (ARR)** | Закрытый репо, лицензия запрещает копирование. Skip. |
| **L_Ender's Cataclysm** | https://modrinth.com/mod/l_enders-cataclysm | Public source закрыт; исходники только в форках типа `lender544/new1.20.1` без явной лицензии → де-факто ARR | Skip. |
| **Marium's Soulslike Weaponry на CurseForge** | https://www.curseforge.com/minecraft/mc-mods/mariums-soulslike-weaponry | На CurseForge помечено `All Rights Reserved` | НО исходники на GitHub — **CC0-1.0**. Использовать GitHub-версию (см. 2.1). |

## 3. Что НЕ нашлось

Для следующих этапов — TODO список ассетов, которые потребуют либо ручной отрисовки, либо доп. ресёрча:

1. **GeckoLib `draw`/`sheathe` анимации меча** — у `freyr_sword` есть только `idle`, `attack_north`, `attack_east`. `draw` (выхватывание из ножен) и `sheathe` (убирание) надо нарисовать в Blockbench или собрать из тех же бон-параметров вручную.
2. **`Reid release charge` анимация** — отдельная charge-up анимация для ультимейта. Нет ни в zip, ни в external CC0-моде. Нужна ручная сборка.
3. **Player-animation для падения на колено** (knight-fall pose, KosmX `.json`-формат) — в KosmX example только `waving.json`. Нужно либо нарисовать в Blockbench с экспортом в KosmX-format, либо использовать существующий `dying.json` если в основном моде есть.
4. **Player-animation «golden ascension» / phoenix-revive** — отсутствует во всех проверенных CC0/MIT источниках. Нужна ручная сборка либо генерация в Blockbench.
5. **Returning Knight / Moonknight `.geo.json` файлы** — анимации в Soulslike-Weaponry CC0 есть, но сами geo-модели в `art-source/rezero-fx-textures.zip` НЕ скопированы (только freyr_sword + holy_moonlight_pillar). Можно вытащить напрямую из https://github.com/mariumbacchus/Soulslike-Weaponry/tree/1.21.1/src/main/resources/assets/soulsweapons/geo/entity при потребности.
6. **`Reinhard skin`** — `rezeromc/textures/entities/reinhard-van-astrea-re-zero-on-planetminecraft-com.png` упомянут в плане героя, но это **player-skin** и его обработка относится к этапу реализации героя, а не к particle/sword-FX этой задачи. Не извлечён.
7. **Звук «феникс-крик» / «golden bell»** — для воскрешения. В `soulsweapons/sounds/` нет однозначного кандидата. Текущий fallback — `restore.ogg` + `dawnbreaker_sound.ogg` под алиасом `vanguard:phoenix_restore` / `vanguard:phoenix_dawnbreaker`.
8. **«Spirit-strike» проекция как отдельная entity-модель** — текстуры есть (`spirit_wave.png`, `spirit_slash_*.png`), но `.geo.json` для летящей spirit-projectile нет. Можно либо использовать vanilla particle-API, либо сделать в Blockbench.

## 4. Что НЕ закоммичено

- **Распакованный** `art-source/rezero-fx-textures.zip` (60 МБ распакованных) — лежал в `/tmp/rezero/`, **не в репо**. Сам zip остаётся как source-of-truth в `art-source/rezero-fx-textures.zip`.
- Файлы со спорной лицензией (Born in Chaos, Cataclysm, Epic Fight) — даже на референс-уровне.
- Любые Java-классы, build.gradle, fabric.mod.json, gradle.properties — это область parent-сессии.

## 5. Источники / лицензии

- Файлы из `rezeromc/...` и `soulsweapons/...` внутри `art-source/rezero-fx-textures.zip` — соответствующая часть совпадает с публикацией [mariumbacchus/Soulslike-Weaponry](https://github.com/mariumbacchus/Soulslike-Weaponry) под **CC0-1.0** ([LICENSE](https://github.com/mariumbacchus/Soulslike-Weaponry/blob/1.21.1/LICENSE)). Re:Zero specific фрагменты (`rezeromc/`) — публичные resource-pack ассеты Re:Zero MC; используются как сырьё под адаптацию (см. `art-source/README.md` правило 6 «Лицензии»).
- Файлы из `powerborne/...` — условия использования в README zip-а не указаны. Использованы только golden/heart/cape текстуры (≈3 файла); если автор основного мода Vanguard потребует — заменить ручной отрисовкой.
- Vanguard mod itself — CC0-1.0 (см. `fabric.mod.json`).
