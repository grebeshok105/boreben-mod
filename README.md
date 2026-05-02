# Boreben Mod

Аддон к моду [Superheroes Mod](https://github.com/grebeshok105/grebeshok105). Расширяет основной мод дополнительным контентом, который попадает в ту же creative-вкладку и держится в едином стиле.

## Зависимости

| Что | Версия |
|---|---|
| Minecraft | 1.21 |
| Fabric Loader | >= 0.19.2 |
| Fabric API | 0.102.0+1.21 |
| Java | 21 |
| **Superheroes Mod** | (hard dep, точная версия в `gradle.properties` / fabric.mod.json) |

Без основного `superheroes` мода аддон не запустится — это by design.

## Сборка

```bash
export JAVA_HOME=/path/to/jdk-21
./gradlew build --no-daemon -x test
```

Готовый jar: `build/libs/boreben-<version>.jar`.

Подробнее — `.agents/skills/build-mod/SKILL.md`.

## Структура

```
.agents/skills/        — skills для Devin / AI-агентов (правила, гайды по типовым задачам)
AGENTS.md              — entry-point для AI: карта проекта, на что смотреть в первую очередь
src/main/java/...      — код аддона (под пакетом com.boreben.mod)
src/main/resources/    — fabric.mod.json, ассеты под неймспейсом `boreben`
```

## Для AI-агентов / Devin

Перед любой задачей читать в порядке:

1. `AGENTS.md` — общая карта
2. `.agents/skills/base-rules/SKILL.md` — что НЕ делать
3. `.agents/skills/addon-integration/SKILL.md` — как именно встраивать контент в основной мод
4. Конкретный skill под задачу (`add-item`, `add-block`, `build-mod`, ...)

## Как это аддон, а не отдельный мод

- **Свой mod id (`boreben`)**, свой неймспейс ассетов
- **Не создаёт собственную creative-вкладку** — добавляет предметы в существующую `superheroes:superheroes` через `ItemGroupEvents.modifyEntriesEvent` (Fabric API)
- **Не трогает internal API основного мода** — только публичные API и стандартный Fabric API
- В `fabric.mod.json` явная зависимость `"superheroes": "*"`

## Лицензия

CC0-1.0 (как у основного мода).
