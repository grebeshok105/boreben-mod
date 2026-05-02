# AGENTS.md — Boreben Addon

Гайд для Devin / других AI-агентов. Читается **в начале каждой сессии автоматически**. Назначение — за 1-2 минуты дать карту проекта: что это, как устроено, что НЕ трогать.

---

## TL;DR

- **Что это**: **аддон** к моду `superheroes` (репо `grebeshok105/grebeshok105`). Не самостоятельный мод. Контент попадает в ту же creative-вкладку, что и основной мод.
- **Стек**: Fabric 1.21, Java 21, Mojang mappings (как у основного мода)
- **Mod ID**: `boreben` (пока tentative, подтвердить при первой реальной задаче)
- **Java package**: `com.boreben.mod`
- **Hard dep**: `superheroes` (без него аддон не загрузится — это правильно)
- **Build**: `./gradlew build --no-daemon -x test` (см. skill `build-mod`)
- **Branching**: `devin/$(date +%s)-<short-name>`
- **PR title**: `feat(scope): ...` / `fix(scope): ...`. Description **на русском**.
- **Перед любым кодом**: прочитать `.agents/skills/base-rules/SKILL.md`.

---

## Главные принципы аддона

1. **Не дублировать механики основного мода**. Если нужна механика основного мода — потреблять через публичное API. Если публичного API нет — попросить пользователя/автора основного мода вынести его, не копировать internal в аддон.
2. **Свой неймспейс**. Все ID, ассеты, локализация — под `boreben`, никогда под `superheroes`.
3. **Свою creative-вкладку не создавать**. Использовать `ItemGroupEvents.modifyEntriesEvent(superheroes:superheroes)`. См. skill `addon-integration`.
4. **Версии и стек как у основного мода**. Любой апдейт MC/Fabric делается сначала там, потом синхронно тут.

---

## Quick map: куда идти за чем

| Хочешь… | Файл/папка | Skill |
|---|---|---|
| Понять как встроить контент в creative-tab основного мода | — | `addon-integration` |
| Добавить предмет | `src/main/java/com/boreben/mod/item/` + регистрация | `add-item` |
| Добавить блок | `src/main/java/com/boreben/mod/block/` + регистрация | `add-block` |
| Собрать jar | `./gradlew build --no-daemon -x test` | `build-mod` |
| Сгенерировать модели/loot/recipes | `./gradlew runDatagen` | `datagen` |
| Разобрать краш | `run/crash-reports/` или `run/logs/latest.log` | `debug-crash` |
| Не наступить на грабли Fabric 1.21 | — | `loader-gotchas` |
| Подобрать инструмент для ресёрча | — | `research-tools` |
| Найти готовые ассеты | `art-source/` (если есть) | `art-source` |
| Сделать GitHub-релиз | `gh release create ...` | `release-mod` |
| Опубликовать в CurseForge / Modrinth | — | `publish-mod` |
| Базовые правила что НЕ делать | — | `base-rules` |
| Идентификаторы и версии проекта | — | `project-profile` |

---

## Идентичность проекта

| Параметр | Значение |
|---|---|
| Mod ID | `boreben` (TODO: подтвердить) |
| Display Name | `Boreben Mod` |
| Java package | `com.boreben.mod` (TODO: подтвердить) |
| Main entrypoint | `com.boreben.mod.BorebenMod` |
| Client entrypoint | `com.boreben.mod.client.BorebenClient` |
| Datagen entrypoint | `com.boreben.mod.datagen.BorebenDataGenerator` |
| Minecraft | `1.21` |
| Fabric Loader | `>=0.19.2` |
| Fabric API | `0.102.0+1.21` |
| Java | 21 (на VM `/home/ubuntu/jdk-21.0.2`) |
| Mappings | Mojang (Yarn НЕ используется) |
| Loom | `1.16-SNAPSHOT` |
| Hard dep | `superheroes` |
| License | CC0-1.0 |

> При первой реальной задаче: подтвердить у пользователя финальные `mod_id`, `package`, `Display Name` и зафиксировать в `project-profile/SKILL.md`.

---

## Архитектура аддона

```
BorebenMod (entrypoint)
├── BorebenItems.init()           # регистрация предметов под боребен-неймспейсом
├── BorebenBlocks.init()          # регистрация блоков
├── BorebenItemGroupHook.init()   # ItemGroupEvents → добавляет всё в superheroes:superheroes tab
├── BorebenSounds.init()          # звуки если есть
└── ...
```

Никаких собственных систем уровня Hero/Ability/Resource — это всё в основном моде. Аддон только **содержательный**: предметы, блоки, ассеты, лор-расширения.

---

## Структура исходников

```
boreben-mod/
├── AGENTS.md                              # этот файл
├── README.md                              # описание для людей
├── build.gradle                           # Fabric Loom build script
├── gradle.properties                      # версии MC, loader, мода
├── settings.gradle
├── .agents/
│   └── skills/                            # skills для AI-агентов
│       ├── base-rules/SKILL.md
│       ├── project-profile/SKILL.md
│       ├── addon-integration/SKILL.md
│       ├── add-item/SKILL.md
│       ├── add-block/SKILL.md
│       ├── build-mod/SKILL.md
│       ├── datagen/SKILL.md
│       ├── debug-crash/SKILL.md
│       ├── loader-gotchas/SKILL.md
│       ├── minecraft-mod-dev/SKILL.md
│       ├── research-tools/SKILL.md
│       ├── art-source/SKILL.md
│       ├── release-mod/SKILL.md
│       └── publish-mod/SKILL.md
├── src/
│   └── main/
│       ├── java/com/boreben/mod/...        # код аддона
│       └── resources/
│           ├── fabric.mod.json
│           ├── boreben.mixins.json (если будут)
│           └── assets/boreben/             # ассеты под СВОЙ неймспейс
│               ├── lang/{en_us,ru_ru}.json
│               ├── models/...
│               └── textures/...
├── art-source/                            # сырые ассеты от автора (если есть)
└── libs/                                  # superheroes-X.Y.Z.jar для compile-time
```

---

## Что аддон НЕ делает

- НЕ переопределяет heroes / abilities основного мода
- НЕ изменяет балансировку основного мода (`ResourceController`, attribute modifiers и т.д.)
- НЕ трогает HUD основного мода
- НЕ ставит mixin-ы в `com.example.superheroes.*` (хрупко при апдейтах)
- НЕ дублирует ассеты в `assets/superheroes/...`

Если для задачи требуется одно из этого — это сигнал что задача не для аддона, а для основного мода. Пользователю об этом говорить.

---

## Workflow для типовых задач

### Добавить предмет
1. `add-item` skill пошагово
2. `addon-integration` — добавить в creative-tab через `ItemGroupEvents`
3. `build-mod` — `./gradlew build`
4. PR

### Добавить блок
1. `add-block` skill
2. `addon-integration` — BlockItem в creative-tab
3. `datagen` если нужны blockstates/loot/recipes автогенерацией
4. `build-mod` — `./gradlew build`
5. PR

### Релиз
1. `release-mod` skill — все шаги
2. В release notes указать совместимую версию `superheroes`

---

## Стиль коммуникации

- Пользователь пишет на русском → отвечаем на русском, тех.термины — английскими
- Минимум токенов, по делу
- Ссылка на PR / релиз в финальном сообщении
- Промежуточные апдейты только если есть что показать

См. `base-rules/SKILL.md` для полного списка правил.
