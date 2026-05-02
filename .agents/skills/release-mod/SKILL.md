---
name: release-mod
description: Use when user asks to release the addon, create a GitHub release, tag a version, or publish a build. Triggered by "релиз", "release", "тег".
---

# Release Mod — Vanguard Addon

Создание GitHub-релиза с прикреплённым jar.

## Шаги

1. **Перейти на baseline и подтянуть свежий**
```bash
cd /home/ubuntu/repos/vanguard-mod
git checkout main && git pull origin main   # или baseline если ветка по-другому называется
```

2. **Поднять версию локально (НЕ коммитить в обычных PR)**
```bash
# редактируем gradle.properties: mod_version=<X>
# например 0.1.0, 0.2.0, 1.0.0
```

3. **Собрать** (см. skill `build-mod`)
```bash
export JAVA_HOME=/home/ubuntu/jdk-21.0.2 && export PATH=$JAVA_HOME/bin:$PATH
./gradlew build --no-daemon -x test
ls build/libs/vanguard-<X>.jar  # проверить что собрался
```

4. **Создать release**
```bash
gh release create v<X> build/libs/vanguard-<X>.jar \
  --title "v<X> — <короткое описание>" \
  --notes "<markdown notes>"
```

В notes писать на русском, перечислять что вошло. **Обязательно указать совместимую версию основного мода** (`superheroes vX.X.X`).

5. **Откатить версию локально**
```bash
git checkout -- gradle.properties
```

`mod_version` в репо желательно держать на стабильной версии — версии существуют только как релиз-теги.

## Naming
- `v0.1.X` пока pre-release
- `v1.0.X` после первой стабильной публикации
- `v1.0.X-<suffix>` для тематических (`-fix`, `-content`, etc.)

## Совместимость
- В описании релиза всегда указывать: `Требует Superheroes Mod vX.Y.Z+ и Minecraft 1.21`
- Если что-то ломается из-за апдейта основного мода — выпускать новую версию аддона со ссылкой на причину
