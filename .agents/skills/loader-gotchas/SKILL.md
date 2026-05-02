---
name: loader-gotchas
description: Use when writing Fabric specific code, mixins, networking, or registration code in MC 1.21+. Non-obvious traps that aren't in standard docs.
triggers: ["model"]
---

# Loader Gotchas (MC 1.21+, Fabric)

Только то, что **не** найти за один взгляд в Mojang docs / Fabric wiki.

## Fabric (этот проект)

- `@WrapOperation` (MixinExtras) предпочитать `@Redirect` — Redirect конфликтует с другими модами
- В миксинах все приватные поля и методы помечать `@Unique` — иначе скрытый конфликт байт-кода
- `net.fabricmc.fabric.impl.*` — internal, не импортировать никогда. Только `net.fabricmc.fabric.api.*`
- Для регистрации сетевых пакетов **не** ставить `@Environment` — Fabric сам разделит client/server
- `ServerTickEvents.END_SERVER_TICK` runs after entity ticks. Если ставишь `setDeltaMovement(0)` — гравитация всё равно успеет добавить -0.08 в следующем тике до твоего хука. Для жёсткого якоря используй `connection.teleport(...)` назад к точке

## Аддон-специфичное

- `ItemGroupEvents.modifyEntriesEvent` тихо ничего не делает если вкладку с таким ID никто не зарегистрировал. Если items не появляются — проверить что основной мод грузится первее аддона (зависимость в `fabric.mod.json` это гарантирует)
- НЕ создавать собственную вкладку с тем же ID что у основного мода — `IllegalStateException: Adding duplicate key`
- НЕ создавать `Identifier`-ы под чужим неймспейсом для регистрации (`ResourceLocation.fromNamespaceAndPath("superheroes", "my_thing")` — это конфликт)
- Mixin в классы другого мода (`com.example.superheroes.*`) — крайне хрупко. При обновлении основного мода всё ломается. Если без него никак — обязательно `@Pseudo` + `@Mixin(targets = "...")` и `min`/`max` версии

## Общее (Mojang mappings, 1.21+)

- `ResourceLocation.fromNamespaceAndPath(...)` или `ResourceLocation.parse(...)` — конструктор `new ResourceLocation(...)` удалён
- `Component` — единый тип `net.minecraft.network.chat.Component` (Yarn-обёртки `Text` больше не используются)
- `Items.ELYTRA` is `ElytraItem` (`extends Item implements Equipable`); `ArmorItem` для брони. Чтобы детектить экипировку — `instanceof ArmorItem` + `instanceof ElytraItem`
- `Inventory.armor` — публичный `NonNullList<ItemStack>` слотов (boots/legs/chest/helmet)
- `LivingEntity` MobEffect API: `addEffect(new MobEffectInstance(MobEffects.X, duration, amplifier, ambient, showParticles, showIcon))`. Бесконечный эффект — `duration = -1`
- DataGen генерируется в `src/main/generated/` (или `src/generated/resources/` в зависимости от настройки) — не править руками, только через DataProvider

## Регистрация под СВОИМ mod id

Стандартный helper для аддона:
```java
public final class VanguardIds {
    public static final String NAMESPACE = "vanguard";
    public static ResourceLocation of(String path) {
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, path);
    }
    private VanguardIds() {}
}
```
И всегда использовать `VanguardIds.of("foo")`, не литералы.
