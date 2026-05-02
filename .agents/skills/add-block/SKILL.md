---
name: add-block
description: Use when adding a new block to the addon. Full Fabric 1.21 pipeline including registration, model, blockstate, texture, loot table, and creative-tab hookup into the base mod's tab.
---

# Add Block — Boreben Addon

Pipeline под неймспейс `boreben`. Свою вкладку creative не создавать — всё идёт в основную `superheroes:superheroes` через `ItemGroupEvents`.

1. **Класс блока** в `src/main/java/com/boreben/mod/block/<Name>Block.java`
   - extends `Block` (или `BaseEntityBlock` если есть BE)

2. **Регистрация в `BorebenBlocks`**
   ```java
   public static final Block FOO = register("foo",
       new FooBlock(BlockBehaviour.Properties.of()...));

   private static Block register(String name, Block block) {
       return Registry.register(BuiltInRegistries.BLOCK,
           ResourceLocation.fromNamespaceAndPath("boreben", name), block);
   }
   ```
   Если нужен `BlockItem` — также регистрировать в `BorebenItems` (или в `BorebenBlocks` отдельным методом).

3. **Модель блока**: `assets/boreben/models/block/<name>.json`

4. **Модель предмета** (если есть BlockItem): `assets/boreben/models/item/<name>.json`
   обычно `{"parent": "boreben:block/<name>"}`

5. **Blockstate**: `assets/boreben/blockstates/<name>.json`

6. **Текстура**: `assets/boreben/textures/block/<name>.png` (16x16, sides если multi-face)

7. **Локализация**: `assets/boreben/lang/en_us.json` + `lang/ru_ru.json`
   ```json
   "block.boreben.<name>": "Foo"
   ```

8. **Loot table** (если drops != self): `data/boreben/loot_table/blocks/<name>.json`

9. **Recipe** (если crafting): `data/boreben/recipes/<name>.json`

10. **Добавить BlockItem в creative-tab основного мода** в `BorebenItemGroupHook`:
    ```java
    entries.accept(BorebenBlocks.FOO);
    ```

11. **Build & verify**: skill `build-mod`

## Чек-лист
- [ ] Все ID в неймспейсе `boreben`
- [ ] Все ассеты под `assets/boreben/...` и `data/boreben/...`
- [ ] Блок и его BlockItem зарегистрированы
- [ ] Loot table если нужен
- [ ] Локализация в обоих языках
- [ ] Добавлен в creative-tab через `ItemGroupEvents.modifyEntriesEvent`
