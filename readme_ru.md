<div align="center">

# 🎬 SubCreator

![Java](https://img.shields.io/badge/Java-24-orange?logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-17.0.8-%23ED8B00?logo=javafx)
![License](https://img.shields.io/badge/license-GNUv3-yellow)

**Инструмент для создания и перевода субтитров**

</div>

## ✨ Уникальные преимущества

### 🆚 По сравнению с аналогами:
- **🎯 Полностью бесплатно*** - один API ключ = 150+ часов обработки
- **🌍 Встроенный перевод** - создавайте субтитры на множестве языке
- **📦 Гибкая настройка** - благодаря интеграции с FFmpeg

### ⚡ Основные возможности:
- 🎧 **Извлечение аудио** из видео
- 🗣️ **Транскрибация** через AssemblyAI с высокой точностью
- 🌐 **Автоматический перевод** субтитров
- 🎨 **Настройка стилей** шрифтов и оформления

> *Примечание: Каждому аккаунту AssemblyAI предоставляется 150+ часов обработки видео. При исчерпании лимита можно создать новый аккаунт и продолжить бесплатное использование.*

## 💾 Установка

### Windows
[![NSIS Installer](https://img.shields.io/badge/Download-Windows_Installer-blue?logo=windows&style=for-the-badge)](installer/SubCreatorSetup.exe)

1. **Скачайте** `SubCreatorSetup.exe`
2. **Запустите установщик** и следуйте инструкциям
3. **Введите API ключ** AssemblyAI при установке
4. **Готово!** Приложение полностью настроено

### Получение API ключа
1. Перейдите на [AssemblyAI](https://www.assemblyai.com/dashboard/login)
2. Зарегистрируйте бесплатный аккаунт
3. Скопируйте API ключ из личного кабинета
4. Используйте его при установке SubCreator

> Детальная [инструкция](docs/installation_guide_ru.md)

## 🛠 Технологический стек

[![Java 24](https://img.shields.io/badge/Java-24-%23ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![JavaFX 17](https://img.shields.io/badge/JavaFX-17.0.8-%230175C2?logo=javafx)](https://openjfx.io/)
[![AssemblyAI](https://img.shields.io/badge/AssemblyAI-Speech_to_Text-%2300599C?logo=assemblyai)](https://www.assemblyai.com/)
[![FFmpeg](https://img.shields.io/badge/FFmpeg-Multimedia-%23007800?logo=ffmpeg)](https://ffmpeg.org/)

<details>
<summary>📦 Полный список зависимостей</summary>

**Ядро приложения:**
- Java 24
- JavaFX 17

**Обработка медиа:**
- Интеграция с FFmpeg для обработки видео
- AssemblyAI API для распознавания речи

**Дополнительные технологии:**
- Jackson
- Lombok
- NSIS
</details>

## 💬 Поддержка и сообщество

### Нашли проблему?
- **Откройте Issue** на [GitHub](https://github.com/vovabsuir/subcreator/issues)
- **Напишите на почту**: vovabsuir@gmail.com
- **Telegram чат**: [![Telegram](https://img.shields.io/badge/Chat-Telegram-blue?logo=telegram)](https://t.me/+76LBDzoK2xlmNzUy)

### Частые вопросы:
**Q: Сколько стоит использование?**  
A: Полностью бесплатно, 150+ часов видео на один аккаунт AssemblyAI.

**Q: Нужно ли устанавливать Java?**  
A: Нет, в установщик встроено JRE.

**Q: Какие языки речи поддерживаются?**  
A: На данный момент - EN, FR, DE, ES, RU.

**Q: Какие форматы видео поддерживаются?**
A: webm, .mts, .m2ts, .ts, .mov, .mp2, .mp4, .m4p, .m4v, .mxf

**Q: Можно ли обрабатывать большие файлы?**  
A: Ограничение со стороны AssemblyAI - 2.2GB на файл.

## 🤝 Участие в разработке

Проект открыт для contribution! Особенно нужна помощь в следующем:

- 🌐 Локализация интерфейса
- 🎨 Улучшение дизайна и UX
- 🐛 Поиск и исправление багов
- 📚 Добавление функционала (сохранение промежуточных файлов, предварительный просмотр видео, batch-обработка)

**Процесс разработки:**
1. Форкните репозиторий
2. Создайте feature ветку: `git checkout -b feature/improvement`
3. Закоммитьте изменения: `git commit -m 'Add amazing feature'`
4. Запушьте ветку: `git push origin feature/improvement`
5. Откройте Pull Request

## 📈 Дорожная карта

### Версия 1.1
- [ ] Пакетная обработка нескольких файлов
- [ ] История проектов
- [ ] Шаблоны стилей субтитров

### Версия 1.2
- [ ] Добавление нового переводчика

### Версия 1.3
- [ ] Веб-версия приложения
- [ ] Мобильное приложение
- [ ] Редактирование видео с помощью FFmpeg

---

<div align="center">

**Создавайте субтитры как профессионал - бесплатно и без ограничений**

[![GitHub stars](https://img.shields.io/github/stars/vovabsuir/SubCreator?style=social)](https://github.com/vovabsuir/SubCreator/stargazers)
[![GitHub forks](https://img.shields.io/badge/Forks-Welcome-success?style=social)](https://github.com/vovabsuir/SubCreator/network/members)

</div>
