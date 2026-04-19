# Ручной HTTP-сервер на Java

Минималистичный многопоточный HTTP-сервер, написанный на Java без фреймворков.

Проект показывает, как устроены HTTP-запросы и работа веб-сервера на низком уровне.

---

## Возможности

- HTTP/1.1 сервер
- GET-запросы с query-параметрами
- Раздача статических файлов из папки `public`
- Многопоточность (thread pool)

---

## Поддерживаемые пути

Доступно по адресу: http://localhost:9999

- /index.html
- /spring.svg
- /spring.png
- /resources.html
- /styles.css
- /app.js
- /links.html
- /forms.html
- /classic.html
- /events.html
- /events.js

## Принцип работы

1. Сервер слушает порт 9999
2. Принимает TCP-соединение
3. Читает и парсит HTTP request вручную
4. Загружает файл из директории public и отправляет HTTP response



