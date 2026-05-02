---
name: addon-integration
description: Use when adding new items/blocks/sounds to the addon and you need them to integrate with the base superheroes mod (same creative tab, sounds.json, etc.).
---

# Addon Integration — как встроиться в `superheroes` мод

Этот мод — **аддон**. Цель — чтобы для игрока контент аддона выглядел как часть основного мода (одна вкладка в creative, единый стиль), но кодом он лежал в отдельном репо и не зависел от internal-API основного мода.

## 1. Зависимость в `fabric.mod.json`

```json
{
  "depends": {
    "fabricloader": ">=0.19.2",
    "minecraft": "~1.21",
    "java": ">=21",
    "fabric-api": "*",
    "superheroes": "*"
  }
}
```

`superheroes` — `hard dep`. Если основного мода нет, Fabric откажется грузить аддон с понятной ошибкой — это правильно.

## 2. Зависимость в `build.gradle`

Основной мод не публикуется в публичный maven, поэтому проще подключить его jar локально:

```gradle
dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings loom.officialMojangMappings()
    modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"

    // Superheroes mod — kладётся локально в libs/ либо тянется из релиза:
    modImplementation files("libs/superheroes-2.2.2.jar")
}
```

В CI это может не работать без файла → можно сделать `modImplementation` опциональным через таск-проверку наличия файла. На первое время проще держать jar в `libs/` и закоммитить (он не большой).

Альтернатива: `maven { url "https://maven.pkg.github.com/grebeshok105/grebeshok105" }` если включить GitHub Packages publish в основном моде.

## 3. Добавление в creative-вкладку основного мода

ID вкладки основного мода: `superheroes:superheroes` (см. `ModItemGroups.java` в основном моде).

В аддоне не создавать собственную вкладку. Вместо этого:

```java
package com.vanguard.mod.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public final class VanguardItemGroupHook {
    private static final ResourceKey<CreativeModeTab> SUPERHEROES_TAB =
        ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath("superheroes", "superheroes"));

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(SUPERHEROES_TAB).register(entries -> {
            entries.accept(VanguardItems.MY_NEW_ITEM);
            // entries.accept(VanguardItems.OTHER_ITEM);
        });
    }

    private VanguardItemGroupHook() {}
}
```

Зов из `VanguardMod.onInitialize()`:
```java
@Override
public void onInitialize() {
    VanguardItems.init();
    VanguardItemGroupHook.init();
}
```

**Важно**:
- Если вкладку не нашли (мод не загрузился) — `modifyEntriesEvent` тихо ничего не сделает, не упадёт. Это ок.
- Не вызывать `Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ...)` для существующей вкладки — будет `IllegalStateException: Adding duplicate key`.

## 4. Ассеты под СВОЙ неймспейс

```
src/main/resources/
├── assets/vanguard/
│   ├── lang/en_us.json
│   ├── lang/ru_ru.json
│   ├── models/item/...
│   ├── textures/item/...
│   └── sounds.json (опционально)
└── data/vanguard/
    ├── recipes/...
    └── loot_table/blocks/...
```

Никогда не клади файлы в `assets/superheroes/...` — Fabric ругаться не будет, но любой пак-загрузчик будет видеть конфликт ресурсов и поведение зависит от порядка загрузки модов.

## 5. Локализация

Ключи всегда префиксованы своим mod id:
```json
{
  "item.vanguard.cool_artifact": "Cool Artifact",
  "block.vanguard.weird_block": "Weird Block"
}
```

Если хочешь добавить перевод к чему-то из основного мода (например, новый язык) — это уже не задача аддона, это PR в основной мод.

## 6. Звуки

`assets/vanguard/sounds.json` + регистрация в `ModSounds`-аналоге аддона:
```java
public final class VanguardSounds {
    public static final SoundEvent FOO = register("foo");

    private static SoundEvent register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("vanguard", name);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {} // загрузка статики

    private VanguardSounds() {}
}
```

## 7. Что НЕ делать

- Не использовать классы из `com.example.superheroes.*` в коде аддона — это internal API основного мода. Если нужно — просить пользователя/автора основного мода вынести в публичный API
- Не делать mixin-ы в классы основного мода (`@Mixin(SuperheroesMod.class)` и т.п.) — они ломаются при любом обновлении
- Не публиковать аддон с одинаковым `mod id` или тем же `maven_group` — это конфликт регистрации
- Не переопределять регистры основного мода (item id `superheroes:foo` нельзя пересоздать в аддоне)

## 8. Тестирование интеграции

1. Положить jar основного мода в `run/mods/` (или `libs/` если через `modImplementation`).
2. `./gradlew runClient --no-daemon`.
3. Открыть creative → вкладка `Superheroes Mod` должна содержать предметы и из основного мода, и из аддона.
4. Если вкладка пустая — основной мод не загрузился, смотреть `run/logs/latest.log`.
