---
name: add-item
description: Use when adding a new item to the addon. Generic Fabric 1.21 pipeline; remember the item must be registered under the addon namespace and added to the base mod's creative tab via ItemGroupEvents.
---

# Add Item — Vanguard Addon

Базовый pipeline для нового предмета в аддоне. **Все ID — под неймспейсом `vanguard`**, не `superheroes`.

1. **Класс предмета** в `src/main/java/com/vanguard/mod/item/<Name>Item.java`
   - extends `Item` (или специальная база если нужно)

2. **Регистрация в `VanguardItems`**
   ```java
   public static final Item FOO = register("foo", new FooItem(new Item.Properties()...));

   private static Item register(String name, Item item) {
       return Registry.register(BuiltInRegistries.ITEM,
           ResourceLocation.fromNamespaceAndPath("vanguard", name), item);
   }
   ```

3. **Текстура**: `assets/vanguard/textures/item/<name>.png` (16x16 обычно)

4. **Модель**: `assets/vanguard/models/item/<name>.json`
   ```json
   {"parent": "minecraft:item/generated", "textures": {"layer0": "vanguard:item/<name>"}}
   ```

5. **Локализация**: `assets/vanguard/lang/en_us.json` + `lang/ru_ru.json`
   ```json
   "item.vanguard.<name>": "Foo"
   ```

6. **Recipe** (опционально): `data/vanguard/recipes/<name>.json`

7. **Добавить в creative-вкладку основного мода** через `VanguardItemGroupHook`:
   ```java
   ItemGroupEvents.modifyEntriesEvent(SUPERHEROES_TAB).register(entries -> {
       entries.accept(VanguardItems.FOO);
   });
   ```
   См. полную схему в skill `addon-integration`. Свою вкладку не создавать.

8. **Build & verify**: skill `build-mod`

## Если основной мод вынес публичный API для расширения предметов

Например, есть `TransformationItem` или базовый `HeroItem` — попроси автора подтвердить что это публичный API (не `com.example.superheroes.internal.*`). Если да — можно extends. Если нет — копировать паттерн в свой класс, не импортить internal.

## Чек-лист
- [ ] ID в неймспейсе `vanguard`, не `superheroes`
- [ ] Текстура и модель под `assets/vanguard/...`
- [ ] Локализация в обоих языках
- [ ] Добавлен в creative-tab через `ItemGroupEvents.modifyEntriesEvent`
- [ ] `./gradlew compileJava` зелёный
