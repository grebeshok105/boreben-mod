---
name: datagen
description: Use when running data generation for blockstates, models, recipes, loot tables, or tags via DataProviders.
---

# Data Generation — Boreben Addon

Используется Fabric DataGen API. Если в `build.gradle` ещё нет `runs { datagen { ... } }` — добавить как в основном моде:

```gradle
loom {
    runs {
        datagen {
            inherit server
            name "Data Generation"
            vmArg "-Dfabric-api.datagen"
            vmArg "-Dfabric-api.datagen.output-dir=${file("src/main/generated")}"
            vmArg "-Dfabric-api.datagen.modid=boreben"
            runDir "build/datagen"
        }
    }
}

sourceSets {
    main {
        resources {
            srcDirs += ['src/main/generated']
        }
    }
}
```

И в `fabric.mod.json`:
```json
"entrypoints": {
    "fabric-datagen": ["com.boreben.mod.datagen.BorebenDataGenerator"]
}
```

## Команда

```bash
cd /home/ubuntu/repos/boreben-mod
export JAVA_HOME=/home/ubuntu/jdk-21.0.2 && export PATH=$JAVA_HOME/bin:$PATH
./gradlew runDatagen --no-daemon
```

## Куда попадают результаты
- `src/main/generated/` — auto-generated. Не править руками.
- При `build` они автоматически попадут в jar (через source set + `srcDirs`).

## Когда нужен datagen
- Добавил новый блок/предмет → сгенерировать blockstate / model / loot table через `FabricBlockLootTableProvider` / `FabricModelProvider`
- Добавил тег → `FabricTagProvider`
- Рецепты — через `FabricRecipeProvider`

## Workflow
1. Добавить/изменить DataProvider в `src/main/java/com/boreben/mod/datagen/`
2. Запустить `./gradlew runDatagen`
3. Проверить diff в `src/main/generated/`
4. Закоммитить и сгенерированное (Fabric ожидает их в репо)
