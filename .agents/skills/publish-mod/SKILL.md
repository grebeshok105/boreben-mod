---
name: publish-mod
description: Use when user asks to publish the addon to CurseForge / Modrinth (external platforms, not GitHub releases).
---

# Publish to CurseForge / Modrinth — Vanguard Addon

GitHub releases — см. skill `release-mod`. Этот skill — про внешние платформы.

## Пререквизиты
- `MODRINTH_TOKEN` (для Modrinth) — env var или GitHub secret
- `CURSEFORGE_API_KEY` (для CurseForge) — env var
- Plugin в `build.gradle`: `me.modmuss50.mod-publish-plugin` (рекомендую) или `com.modrinth.minotaur` + `com.matthewprenger.cursegradle`

## Если plugin настроен

```bash
cd /home/ubuntu/repos/vanguard-mod
export JAVA_HOME=/home/ubuntu/jdk-21.0.2 && export PATH=$JAVA_HOME/bin:$PATH
./gradlew build --no-daemon -x test
./gradlew publishMods --no-daemon  # mod-publish-plugin
# или
./gradlew modrinth curseforge --no-daemon  # отдельные плагины
```

## Если plugin НЕ настроен
1. `./gradlew build` → jar в `build/libs/`
2. Modrinth: https://modrinth.com/mod/<slug>/versions/create — drag-and-drop jar
3. CurseForge: https://www.curseforge.com/minecraft/mc-mods/<slug>/upload — drag-and-drop jar

## Зависимости в листинге
**Обязательно** указать `superheroes` как required dependency:
- Modrinth: добавить как Required dependency через UI или `dependencies { required(project: "superheroes-mod-slug") }` в plugin
- CurseForge: тоже Required dependency

Без этого юзеры будут устанавливать аддон без основного мода и получать NoClassDefFoundError.

## Версионирование
- Та же версия что и в `gradle.properties` (см. skill `release-mod`)
- Сначала GitHub release → потом publish — чтобы версии совпадали и был источник правды

## Changelog
- Брать из release notes последнего GitHub-релиза
- Указывать MC version (`1.21`) и loader (`Fabric`) при загрузке
- Указывать совместимую версию `superheroes`-мода
