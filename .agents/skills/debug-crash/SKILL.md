---
name: debug-crash
description: Use when user shares a crash report, stacktrace, or asks to debug a runtime crash of the addon.
---

# Debug Crash — Boreben Addon

## 1. Найти источник
```bash
ls -la /home/ubuntu/repos/boreben-mod/run/crash-reports/
ls -la /home/ubuntu/repos/boreben-mod/run/logs/latest.log
```

Если краш в файле — `cat` или `read`. Если пользователь дал текст — работать с ним напрямую.

## 2. Найти первопричину
Искать в порядке:
- `Caused by:` (в самом конце stacktrace) — настоящая причина
- `Exception in thread` — точка падения
- `at com.boreben.mod.*` — наш код в стеке
- `at com.example.superheroes.*` — код основного мода. **Если краш тут, а не у нас — это не баг аддона**, либо аддон зовёт internal API основного мода (нельзя), либо несовместимая версия

## 3. Классифицировать
- **Наш код** — открыть указанную строку, исправить
- **Основной мод (`com.example.superheroes.*`)** — проверить:
  - правильная ли версия `superheroes` jar в `libs/` / classpath
  - не зовём ли мы internal API
  - если именно баг основного мода — заводить issue в `grebeshok105/grebeshok105`, не патчить в аддоне
- **Чужой мод** — проверить version compatibility, mixins
- **Vanilla / Mappings** — проверить что используем правильное API для 1.21 (skill `loader-gotchas`)
- **Mixin conflict** — `org.spongepowered.asm.mixin.transformer.throwables.MixinTransformerError`

## 4. Типовые краши в аддоне

- **`IllegalStateException: Adding duplicate key 'superheroes:superheroes'`** — пытался зарегистрировать creative-tab под id основного мода. Не делать так. Использовать `ItemGroupEvents.modifyEntriesEvent` (см. skill `addon-integration`)
- **`java.lang.NoClassDefFoundError: com/example/superheroes/...`** — основной мод не загрузился (проверить что jar в `mods/` / classpath; проверить логи на ошибки загрузки superheroes)
- **`Mod 'boreben' requires version * of 'superheroes' which is missing`** — игрок пытается запустить аддон без основного мода. Это правильное поведение, не лечить
- **`IllegalStateException: Receiving network packet on wrong side`** — забыли разделить client/server. Networking регистрировать без `@Environment`
- **`NoSuchMethodError`** — Mojang mappings drift между версиями MC. См. `loader-gotchas`

## 5. Fix-flow
- Точечный фикс, не рефакторить
- Если фикс затрагивает много мест — задать вопрос пользователю
- Если фикс должен быть в основном моде — НЕ дублировать в аддон, идти в основной репо
- После фикса: `./gradlew build` (skill `build-mod`)
