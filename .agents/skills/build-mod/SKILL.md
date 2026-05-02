---
name: build-mod
description: Use when user asks to build the addon, compile, check for errors, or produce a .jar. Run before opening a PR.
---

# Build Mod — Boreben Addon

## Пререквизит — JDK 21
Loom требует Java 21. На VM лежит готовая дистрибуция в `/home/ubuntu/jdk-21.0.2`.

```bash
export JAVA_HOME=/home/ubuntu/jdk-21.0.2
export PATH=$JAVA_HOME/bin:$PATH
java -version  # должно быть 21.x
```

Если `/home/ubuntu/jdk-21.0.2` не существует:
```bash
mkdir -p /home/ubuntu/jdk-21.0.2
cd /tmp && wget -q https://download.oracle.com/java/21/archive/jdk-21.0.2_linux-x64_bin.tar.gz \
  && tar -xzf jdk-21.0.2_linux-x64_bin.tar.gz --strip-components=1 -C /home/ubuntu/jdk-21.0.2
```

## Команды

```bash
cd /home/ubuntu/repos/boreben-mod
export JAVA_HOME=/home/ubuntu/jdk-21.0.2 && export PATH=$JAVA_HOME/bin:$PATH

./gradlew compileJava --no-daemon          # быстрая проверка ошибок компиляции
./gradlew build --no-daemon -x test        # полная сборка без тестов (CI обычно так)
./gradlew build --no-daemon                # с тестами (если они появятся)
```

`--no-daemon` — Gradle daemon съедает RAM на VM и иногда даёт race conditions.

## Артефакты
После успешного `build`:
- `build/libs/boreben-<version>.jar` — release jar
- `build/libs/boreben-<version>-sources.jar` — sources

Версия из `gradle.properties: mod_version=...`.

## Зависимость от superheroes mod
Аддон зависит от основного мода как `modImplementation`. Если в `libs/` нет jar основного мода (или maven недоступен) — компиляция упадёт со ссылками на отсутствующие классы.

Варианты:
1. Положить `superheroes-X.X.X.jar` в `libs/` и подключить через `files("libs/...")`
2. Использовать ту версию которую опубликовал автор в GitHub Packages / Modrinth (если включено)
3. Для локальной разработки — собрать основной мод из `grebeshok105/grebeshok105` и взять jar из `build/libs/`

## Типовые ошибки
- `Unsupported Java version` → не настроил JAVA_HOME
- `Could not resolve net.fabricmc.fabric-api:fabric-api` → нет интернета. Запустить с `--refresh-dependencies` после сети
- `error: cannot find symbol` от `com.example.superheroes.*` → нет jar основного мода в classpath. Положить в `libs/` или сменить версию зависимости
- `error: cannot find symbol` от Mojang mappings → метод/класс переименовался в 1.21+, см. skill `loader-gotchas`
