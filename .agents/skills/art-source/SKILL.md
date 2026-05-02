---
name: art-source
description: Use when adding textures, sounds, particles, or any visual/audio assets to the addon — check art-source/ first for existing material before creating new.
---

# Art Source Library — Boreben Addon

Если в репо есть папка `art-source/` на корне — это **склад сырых ассетов**, куда пользователь кидает всё что может пригодиться (текстуры, звуки, модели, FX-спрайты, референсы).

## Когда использовать
Перед тем как рисовать новую текстуру или искать звук в интернете — **сначала смотреть в `art-source/`**. Возможно нужное уже там.

## Как использовать
```bash
# Посмотреть содержимое архива не распаковывая:
unzip -l art-source/<archive>.zip | grep -i <keyword>

# Извлечь конкретный файл:
unzip -p art-source/<archive>.zip "path/inside/archive.png" > /tmp/some.png

# Полный просмотр — распаковать во временную папку:
unzip -q art-source/<archive>.zip -d /tmp/art-source/
```

После выбора ассета — **скопировать и переименовать** в `src/main/resources/assets/boreben/textures/...` (или `sounds/`, и т.д.). Не ссылаться на файл прямо из `art-source/` — рантайм-ресурсы должны быть в `src/main/resources/`.

## Форматы

- **Текстуры**: PNG, power-of-two (16, 32, 64, 128, 256). Альфа-канал для прозрачности.
- **Звуки**: OGG Vorbis. Если пришёл MP3 — конвертировать:
  ```bash
  ffmpeg -i input.mp3 -c:a libvorbis -qscale:a 5 output.ogg
  ```
- **Модели**: JSON-based (Blockbench export) или `.bbmodel` (для редактирования, конвертируем в JSON перед use).

## Когда пользователь кидает новый ассет
1. Скачать / принять файл.
2. Положить в `art-source/` (или в подпапку `art-source/inbox/` если много).
3. Если нужно в runtime — скопировать в `src/main/resources/assets/boreben/...` (НЕ в `assets/superheroes/...`!)
4. Закоммитить и то, и другое (art-source — как source-of-truth, runtime — как реально используемое).

## Правила

- `art-source/` **коммитится в git**. Не игнорить.
- Суммарный размер под контролем — если > 500 МБ, переходить на git-lfs или внешнее хранилище.
- Если ассет пришёл из чужого мода/пака — рядом класть `source.txt` или `LICENSE.md` со ссылкой.
- НИКОГДА не класть рантайм-ассеты в `assets/superheroes/...` — это неймспейс основного мода, конфликтует.
