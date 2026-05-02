---
name: add-block
description: Use when adding a new block to the addon. Full Fabric 1.21 pipeline including registration, model, blockstate, texture, loot table, and creative-tab hookup into the base mod's tab.
---

# Add Block — Vanguard Addon

Pipeline под неймспейс `vanguard`. Свою вкладку creative не создавать — всё идёт в основную `superheroes:superheroes` через `ItemGroupEvents`.

1. **Класс блока** в `src/main/java/com/vanguard/mod/block/<Name>Block.java`
   - extends `Block` (или `BaseEntityBlock` если есть BE)

2. **Регистрация в `VanguardBlocks`**
   ```java
   public static final Block FOO = register("foo",
       new FooBlock(BlockBehaviour.Properties.of()...));

   private static Block register(String name, Block block) {
       return Registry.register(BuiltInRegistries.BLOCK,
           ResourceLocation.fromNamespaceAndPath("vanguard", name), block);
   }
   ```
   Если нужен `BlockItem` — также регистрировать в `VanguardItems` (или в `VanguardBlocks` отдельным методом).

3. **Модель блока**: `assets/vanguard/models/block/<name>.json`

4. **Модель предмета** (если есть BlockItem): `assets/vanguard/models/item/<name>.json`
   обычно `{"parent": "vanguard:block/<name>"}`

5. **Blockstate**: `assets/vanguard/blockstates/<name>.json`

6. **Текстура**: `assets/vanguard/textures/block/<name>.png` (16x16, sides если multi-face)

7. **Локализация**: `assets/vanguard/lang/en_us.json` + `lang/ru_ru.json`
   ```json
   "block.vanguard.<name>": "Foo"
   ```

8. **Loot table** (если drops != self): `data/vanguard/loot_table/blocks/<name>.json`

9. **Recipe** (если crafting): `data/vanguard/recipes/<name>.json`

10. **Добавить BlockItem в creative-tab основного мода** в `VanguardItemGroupHook`:
    ```java
    entries.accept(VanguardBlocks.FOO);
    ```

11. **Build & verify**: skill `build-mod`

## Чек-лист
- [ ] Все ID в неймспейсе `vanguard`
- [ ] Все ассеты под `assets/vanguard/...` и `data/vanguard/...`
- [ ] Блок и его BlockItem зарегистрированы
- [ ] Loot table если нужен
- [ ] Локализация в обоих языках
- [ ] Добавлен в creative-tab через `ItemGroupEvents.modifyEntriesEvent`
