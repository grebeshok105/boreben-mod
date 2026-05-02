# Art Source — Vanguard Addon

Эта папка — **склад сырых ассетов** перенесённый из основного мода `superheroes` для использования в аддоне `vanguard`. Содержит текстуры, модели, звуки, FX, готовые партикл-спрайты, ресурсы с других модов для референса.

## Что тут лежит

- `rezero-fx-textures.zip` — большой архив (52 MB) с FX и текстурами Re:Zero-тематики:
  - Reinhard skin: `FX + TEXTURES REZERO/rezeromc/textures/entities/reinhard-van-astrea-re-zero-on-planetminecraft-com.png`
  - Reid sword textures: `dragonsword.png`, `dragonswordreidnew1.png`, `reidstick.png`, `reidastreatexture.png`
  - VFX: `swordexplosion.png`, `sword_explosion_1..6.png`
  - GeckoLib geo (для адаптации/референса): `soulsweapons/geo/entity/freyr_sword.geo.json`, `soulsweapons/animations/entity/freyr_sword.animation.json`
  - 3D-style item models (vanilla multi-element): `soulsweapons/models/item/dragonbane.json`, `dragonslayer_swordspear.json`, `dragon_staff.json` — кандидаты как база для модели меча Reid
  - Mjolnir / Thor / Shield страпы и анимации (powerborne) — для референса формата
- `slenderman-mod-rip/` — ассеты Slenderman, использовались в основном моде (для референса формата addon-mob-rip)
- `sounds/homelander/` — звуки Homelander (референс)
- `sung_jinwoo_v2/` — текстуры Sung Jinwoo (референс)
- `textures/` — разрозненные текстуры из основного мода (homelander wounded, goku, naruto, entity)

## Как пользоваться архивом

```bash
# Посмотреть содержимое не распаковывая:
unzip -l art-source/rezero-fx-textures.zip | grep -i <keyword>

# Извлечь конкретный файл:
unzip -p art-source/rezero-fx-textures.zip "FX + TEXTURES REZERO/.../some.png" > /tmp/some.png

# Полный просмотр — распаковать во временную папку:
unzip -q art-source/rezero-fx-textures.zip -d /tmp/art-source/
```

## Правила

1. **Сюда кидаем всё сырое** — текстуры от пользователя, звуки до обработки, исходные blockbench-проекты, референсы.
2. **В `src/main/resources/assets/vanguard/` копируем только то, что реально используется**, под правильными именами и путями. **НЕ под `assets/superheroes/`** — это неймспейс основного мода, в аддоне затирать его нельзя.
3. **Формат звуков**: OGG Vorbis. Конвертация: `ffmpeg -i in.mp3 -c:a libvorbis -qscale:a 5 out.ogg`.
4. **Формат текстур**: PNG, power-of-two размеры. Прозрачность — альфа-канал.
5. **Крупные файлы ок** (до 100 МБ — лимит GitHub), но если суммарно > 500 МБ — git-lfs или внешнее хранилище.
6. **Лицензии**: если ассет откуда-то скачан, положить рядом `LICENSE.md` или `source.txt` с ссылкой.

## Источник

Большая часть ассетов перенесена из репо основного мода (`grebeshok105/grebeshok105:art-source/`). Часть взята из публичных Re:Zero / soulsweapons / powerborne ресурс-паков; используется как сырьё под адаптацию в аддоне.
