# libs/

Локальные jar-зависимости, которые нет в публичных maven-реп.

## superheroes-X.Y.Z.jar

Базовый мод [Superheroes Mod](https://github.com/grebeshok105/grebeshok105). Скачать:

- [Releases](https://github.com/grebeshok105/grebeshok105/releases) — там же `-sources.jar` для IDE-навигации (его в репо не коммитим, ставь руками если нужно)

Версия определяется в `gradle.properties`:

```
superheroes_version=2.3.0
```

Когда обновляешь — положи новый jar сюда и подними `superheroes_version` соответственно. Проверь `docs/plans/reinhard-hero-plan.md` и `docs/api.md` (в репо основного мода) на breaking changes.
