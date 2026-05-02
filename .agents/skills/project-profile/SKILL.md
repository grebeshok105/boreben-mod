---
name: project-profile
description: Use when working in this repo — provides core mod identifiers, versions, and the addon relationship for the Boreben addon.
triggers: ["model"]
---

# Project Profile — Boreben Addon

> Это **аддон** к моду `superheroes` (`grebeshok105/grebeshok105`). Он добавляет контент в ту же creative-вкладку и работает поверх механик основного мода. Ничего из основного мода не дублируем.

## Идентификаторы
- Mod ID: `boreben` (TODO: подтвердить пользователю при первой реальной задаче)
- Display Name: `Boreben Mod`
- Java package: `com.boreben.mod` (TODO: подтвердить)
- Main entrypoint: `com.boreben.mod.BorebenMod`
- Client entrypoint: `com.boreben.mod.client.BorebenClient`

## Версии (синхронизированы с основным модом)
- Minecraft: `1.21`
- Loader: Fabric (`fabric-loader >=0.19.2`)
- Java: 21 (`/home/ubuntu/jdk-21.0.2`)
- Fabric API: `0.102.0+1.21`
- Mappings: Mojang (Yarn НЕ используется)
- Loom: `1.16-SNAPSHOT`
- License: CC0-1.0

## Зависимости
- `fabricloader >=0.19.2`
- `minecraft ~1.21`
- `java >=21`
- `fabric-api *`
- **`superheroes *`** — основной мод (hard dep, без него аддон не работает)

## Цель аддона
Расширение основного `superheroes`-мода новым контентом в едином оформлении: предметы, блоки, ассеты, лор. Игрок должен видеть всё в одной и той же creative-вкладке, как будто это один мод. См. skill `addon-integration` для технических деталей интеграции.

## Что аддон ДОБАВЛЯЕТ (контент-ориентация)
TODO заполнить когда пользователь определится с конкретным контентом. Кандидаты:
- Дополнительные предметы под существующих героев
- Декоративные блоки в стилистике мода
- Расширение лора через advancements / structures

## Чего аддон НЕ ДЕЛАЕТ
- НЕ переопределяет heroes / abilities основного мода (только потребляет через API)
- НЕ изменяет балансировку основного мода (нет правок `ResourceController`, attributes, atc)
- НЕ затрагивает HUD основного мода (если нужен свой HUD — отдельный канал)
- НЕ дублирует ассеты (`assets/superheroes/...` не трогать, использовать только свой неймспейс)

## Точки расширения, которые гарантированно стабильны
- `ItemGroupEvents.modifyEntriesEvent(...)` для основной вкладки `superheroes:superheroes` — стандартный Fabric API, не зависит от внутренностей основного мода
- Стандартные регистры предметов/блоков/звуков под собственным неймспейсом
- Локализация в собственном lang-файле

## Что трогать ОПАСНО (требует согласования с автором)
- Любая попытка обратиться к `com.example.superheroes.*` напрямую — это internal API основного мода. Сначала уточнить у пользователя есть ли публичная альтернатива
- Mixin-ы в классы основного мода — почти всегда плохая идея, конфликтует при обновлении

## Документация проекта
- Дизайн/планы: `docs/design/<topic>.md` (если будут)
- Интеграция: `.agents/skills/addon-integration/SKILL.md`
- Базовые правила: `.agents/skills/base-rules/SKILL.md`
