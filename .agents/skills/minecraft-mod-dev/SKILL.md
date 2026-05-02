---
name: minecraft-mod-dev
description: Use when creating Minecraft mod content, integrating with mod APIs, setting up modding environments, or migrating to newer versions. Loader = Fabric, MC = 1.21, Java = 21.
---

# Minecraft Mod Development — Vanguard Addon

Правила работы по mod-дев в этом репо. Стек жёстко зафиксирован: **Fabric / MC 1.21 / Java 21 / Mojang mappings**.

## Core Protocols

### 1. Dynamic Documentation Fetching
Когда что-то непонятно по версии 1.21 / Fabric — НЕ полагаться на память:
- `web` action=fetch для `https://fabricmc.net/wiki/`
- `web` action=search с `domain=docs.fabricmc.net`
- Для конкретной либы — глянуть её README на GitHub

### 2. Inter-mod Integration (Ecosystem First)
Этот аддон **по дизайну** интегрируется с другим модом (`superheroes`). Перед изобретением своего:
- Проверить нет ли в основном моде публичного API под нужное (см. `com.example.superheroes.*` API surface)
- Если есть JEI/EMI integration в основном моде — использовать тот же подход
- **Convention Tags** (`#c:ingots`, `#c:dusts`) для cross-mod совместимости

### 3. Standards & Best Practices
- **Registration**: для Fabric — стандартный `Registry.register(BuiltInRegistries.ITEM, id, item)` или хелперы Fabric API
- **Logic Separation**: client-only код в `src/client/java/...` если используется `splitEnvironmentSourceSets()` в loom; иначе строго `@Environment(EnvType.CLIENT)` на классах рендера
- **Data Generation**: предпочитать `DataProvider`-ы вместо ручного JSON
- **Data Components** (1.20.5+): использовать `DataComponentType` вместо raw NBT для item data

## Tooling
- `./gradlew runClient` — запустить клиент (с jar основного мода в classpath/mods)
- `./gradlew runServer` — сервер
- `./gradlew runDatagen` — datagen (см. skill `datagen`)
- `./gradlew build --no-daemon -x test` — релизный jar (см. skill `build-mod`)

## Когда нужен Fabric API event vs Mixin
- Если есть Fabric API event под задачу — использовать его (`UseItemCallback`, `ServerTickEvents`, `ItemGroupEvents`, и т.д.)
- Mixin — только если события не покрывают
- В аддоне **избегать** mixin-ов в код основного мода; лучше попросить вынести API в основной мод

## Migration Protocol (если когда-нибудь будет апдейт MC)
1. Сначала проапгрейдится **основной мод** — у аддона нет смысла существовать впереди
2. Поднять `minecraft_version`, `fabric_api_version`, `loader_version` синхронно с основным
3. Прогнать `./gradlew compileJava` — починить mappings drift
4. Запустить `./gradlew runClient` — проверить creative-tab integration не сломалась
