# Samurai_way

Приложение для удобного планирования задач на Android.

## Описание

Samurai_way позволяет пользователям создавать, редактировать и сортировать свои задачи, отмечать их как выполненные, а также удалять задачи с возможностью восстановления из корзины. Для работы с приложением необходимо зарегистрироваться по номеру телефона с подтверждением через SMS.

## Возможности

- **Добавление задач**  
  Создание новых задач с указанием названия, описания и даты/времени.

- **Редактирование задач**  
  Изменение любых параметров задачи после её создания.

- **Сортировка списка задач**  
  Возможность упорядочить задачи по дате создания, дате выполнения и приоритету.

- **Пометка как выполненные**  
  Отмеченные задачи автоматически перемещаются в отдельный список «Выполненные».

- **Удаление задач**  
  Удалённые задачи помещаются в «Корзину» с возможностью восстановления или окончательного удаления.

- **Регистрация и вход**  
  Регистрация и аутентификация через номер телефона с отправкой SMS-кода.
  
- **Напоминания**  
  Возможность поставить напоминание о важных задах, за сутки до их окончания их дедлайна.
  
- **Дедлайны**  
  Акцентный цвет на тех задачах, срок которых уже прошел.
   

## Технологии

- **Язык:** Java 8+  
- **IDE:** Android Studio  
- **Архитектура:** MVVM с одним Activity и навигацией на Fragments  
- **Хранение данных:**  
  - Room (SQLite) — основные CRUD-операции с задачами  
  - DataStore (SharedPreferences) — хранение настроек и токена пользователя  
- **Аутентификация:** Firebase Authentication (телефон + SMS)  
- **UI:** Jetpack Navigation, Material Components

## Установка и запуск

1. Клонируйте репозиторий:  
   ```bash
   git clone [https://github.com/Cas-byte-per/Samurai_way]
2. Откройте проект в Android Studio.
3. Добавьте файл ```google-services.json``` (Firebase) в каталог ```app/```.
4. В Android Studio синхронизируйте Gradle и запустите приложение на устройстве или эмуляторе.

