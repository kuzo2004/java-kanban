package ru.yandex.practicum;

import ru.yandex.practicum.entity.*;
import ru.yandex.practicum.exceptions.TimeConflictException;
import ru.yandex.practicum.manager.Managers;
import ru.yandex.practicum.service.TaskManager;
import ru.yandex.practicum.service.validation.Validators;
import ru.yandex.practicum.service.validation.Validators.DurationValidator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Stream;


public class Main {
    public static Scanner scanner = new Scanner(System.in);
    public static TaskManager manager = Managers.getDefault();

    public static void main(String[] args) {

        if (manager.getClass().getSimpleName().equals("InMemoryTaskManager")) {
            addTestTasks(); // заполним HashMap тестовыми задачами
        }
        // основной диалог с пользователем
        while (true) {
            System.out.println();
            printMenu();
            String command = scanner.nextLine().trim();
            switch (command) {
                case "1": // Получение списка задач по типу
                    printTasks(getTasksByType());
                    break;
                case "2": // Удаление задач по типу
                    clearTasksByType();
                    break;
                case "3": // Получение задачи по идентификатору и запись задачи в историю
                    getTaskIdAndSaveToHistory()
                            .ifPresentOrElse(
                                    task -> System.out.println("Найдена задача: " + task),
                                    () -> System.out.println("Задача не найдена")
                            );
                    break;
                case "4": // Создание новой задачи
                    try {
                        Optional.ofNullable(prepareAndCreateTask())
                                .ifPresentOrElse(
                                        task -> System.out.println("Создана задача: " + task),
                                        () -> System.out.println("Задача не создана.")
                                );
                    } catch (TimeConflictException e) {
                        System.out.println("Ошибка создания задачи: " + e.getMessage());
                    }
                    break;
                case "5": // Обновление задачи по идентификатору
                    try {
                        Optional.ofNullable(prepareAndUpdateTask())
                                .ifPresent(t -> System.out.println("Обновлена задача: " + t));
                    } catch (TimeConflictException e) {
                        System.out.println("Ошибка обновления задачи: " + e.getMessage());
                    }
                    break;
                case "6": // Удаление задачи по идентификатору
                    if (deleteTaskById()) {
                        System.out.println("Задача удалена.");
                    }
                    break;
                case "7": // Получение списка всех подзадач определённого эпика
                    Epic parentTask = askParentTaskFromUser();
                    if (parentTask == null) break;

                    System.out.println("Выбранная задача:\n" + parentTask);
                    manager.getSubtasksByEpic(parentTask)
                            .ifPresentOrElse(
                                    subtasks -> {
                                        System.out.println("Содержит подзадачи:");
                                        printTasks(subtasks);
                                    },
                                    () -> System.out.println("Не содержит подзадач")
                            );
                    break;
                case "8": // История просмотренных задач
                    List<Task> history = manager.getHistory();
                    System.out.println(history.isEmpty()
                            ? "История просмотренных задач пуста."
                            : "История просмотренных задач:");
                    history.forEach(task -> System.out.print(task.getId() + " "));
                    System.out.println();
                    break;
                case "9":  //Получение отсортированного списка задач.
                    Set<Task> prioritizedTasks = manager.getPrioritizedTasks();
                    System.out.println(prioritizedTasks.isEmpty()
                            ? "Отсортированный список задач пуст"
                            : "Отсортированный список задач:");
                    prioritizedTasks.forEach(System.out::println);
                    break;
                case "10": // Выход из программы
                    System.out.println("Завершение программы. До свиданья!");
                    return;
                default:
                    System.out.println("Нет такой команды. Введите число от 1 до 9");
            }
        }
    }

    private static void addTestTasks() {
        // тестовые задачи
        Epic tTask0 = new Epic("Переезд", "");
        Subtask tTask1 = new Subtask("Собрать коробки", "Положить все вещи в коробки", tTask0,
                LocalDateTime.of(2025, 5, 14, 9, 30), Duration.ofMinutes(60));
        Subtask tTask2 = new Subtask("Упаковать кошку", "Положить кошку в клетку", tTask0,
                LocalDateTime.of(2025, 5, 14, 10, 30), Duration.ofMinutes(30)); // +1 час после tTask1
        Task tTask3 = new Task("Включить чайник", "вскипятить 1.5 литра воды",
                LocalDateTime.of(2025, 5, 14, 11, 0), Duration.ofMinutes(10));
        Task tTask4 = new Task("Заварить чай", "зеленый китайский",
                LocalDateTime.of(2025, 5, 14, 11, 10), Duration.ofMinutes(5)); // +10 минут после tTask3
        Epic tTask5 = new Epic("Сделать проект", "прогноз рынка гаджетов");
        Subtask tTask6 = new Subtask("Найти информацию", "Продажи гаджетов по годам", tTask5,
                LocalDateTime.of(2025, 5, 14, 12, 0), Duration.ofMinutes(120));

        Stream.of(tTask0, tTask1, tTask2, tTask3, tTask4, tTask5, tTask6)
                .forEach(manager::addTask);
    }


    public static void printMenu() {
        System.out.println("Введите команду, которую требуется выполнить:");
        System.out.println("1 - Получение списка задач.");
        System.out.println("2 - Удаление задач.");
        System.out.println("3 - Получение задачи по идентификатору.");
        System.out.println("4 - Создание задачи.");
        System.out.println("5 - Обновление задачи.");
        System.out.println("6 - Удаление задачи по идентификатору.");
        System.out.println("7 - Получение списка всех подзадач определённого эпика.");
        System.out.println("8 - История просмотра задач.");
        System.out.println("9 - Получение отсортированного списка задач.");
        System.out.println("10 - Выход из программы.");
    }


    public static Map<Integer, Task> getTasksByType() {
        System.out.println("Для получение полного списка задач нажмите - ENTER,  для выбора фильтра - ПРОБЕЛ.");
        String command = scanner.nextLine();
        if ("".equals(command)) {
            return manager.getAllTasks();
        }
        // тип задачи
        TaskType taskType = askTaskTypeFromUser();
        return manager.getAllTasksByType(taskType);
    }

    private static void clearTasksByType() {
        System.out.println("Для удаления полного списка задач нажмите - ENTER,  для выбора фильтра - ПРОБЕЛ.");
        String command = scanner.nextLine();
        if ("".equals(command)) {
            manager.clearAllTasks();
            System.out.println("Список задач удален.");
            return;
        }
        // тип задачи
        TaskType taskType = askTaskTypeFromUser();
        if (taskType == TaskType.EPIC) {
            System.out.println("Задачи с типом EPIC будут удалены вместе с подзадачами. " +
                    "Подтвердить удаление да(1)/нет(0).");
            String input = scanner.nextLine().trim();
            if (input.equals("0")) {
                System.out.println("Отмена операции удаления.");
                return;
            }
        }
        manager.clearTasksByType(taskType);
    }


    public static void printTasks(Map<Integer, Task> tasks) {
        System.out.println("_".repeat(20));

        if (tasks.isEmpty()) {
            System.out.println("Список задач пуст");
            return;
        }

        System.out.println("Список задач");
        for (Task task : tasks.values()) {
            System.out.println(task);
        }
    }


    public static Task prepareAndCreateTask() {
        // подготовка параметров
        // тип задачи
        TaskType taskType = askTaskTypeFromUser();

        // имя задачи
        String name = askTaskNameFromUser();

        // описание задачи
        String description = askTaskDescriptionFromUser();

        // родительская задача для подзадачи
        Epic parentTask = null;
        if (taskType == TaskType.SUBTASK) {
            parentTask = askParentTaskFromUser();
            if (parentTask == null) {
                return null;
            }
        }
        // дата старта задачи
        LocalDateTime startTime = null;
        if (taskType != TaskType.EPIC) {
            startTime = askStartTimeFromUser();
        }
        // продолжительность задачи
        Duration duration = null;
        if (taskType != TaskType.EPIC) {
            duration = askDurationFromUser();
        }

        // фабрика создания задачи
        return manager.createTask(taskType, name, description, parentTask, startTime, duration);
    }


    private static LocalDateTime askStartTimeFromUser() {
        // дать 3 попытки пользователю ввести дату в правильном формате
        for (int attempts = 0; attempts < 3; attempts++) {
            try {
                System.out.print("Введите дату начала (dd.MM.yyyy HH:mm) -> ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) return null;
                return LocalDateTime.parse(input, Task.DATE_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println(attempts < 2
                        ? "Ошибка формата, попробуйте еще (" + (2 - attempts) + " попытки)"
                        : "Дата не установлена");
            }
        }
        return null;
    }

    private static Duration askDurationFromUser() {
        System.out.print("Укажите количество минут, которое требуется для выполнения задачи:");
        String input = scanner.nextLine().trim();
        return DurationValidator.parse(input);
    }

    public static Task prepareAndUpdateTask() {
        // выбрать задачу для обновления
        Task oldTask = askTaskIdFromUser().orElse(null);
        if (oldTask == null) return null;

        // подготовка полей к обновлению, заполним их исходными значениями
        String newName = oldTask.getName();
        String newDescription = oldTask.getDescription();
        Status newStatus = oldTask.getStatus();
        LocalDateTime newStartTime = oldTask.getStartTime();
        Duration newDuration = oldTask.getDuration();

        // выбор поля для обновления, так как за один раз обновляем только одно поле в задаче
        System.out.print("Введите поле, которое хотите обновить: name, description, status, starttime, duration:");
        String field = scanner.nextLine().trim().toLowerCase();
        switch (field) {
            case "name" -> newName = askTaskNameFromUser();
            case "description" -> newDescription = askTaskDescriptionFromUser();

            case "status", "starttime", "duration" -> {
                if (oldTask instanceof Epic) {
                    System.out.printf("Поле '%s' для Epic обновляется автоматически%n", field);
                    return null;
                }
                switch (field) { // Вложенный switch для полей с особой логикой
                    case "status" -> newStatus = askTaskStatusFromUser();
                    case "starttime" -> newStartTime = askStartTimeFromUser();
                    case "duration" -> newDuration = askDurationFromUser();
                }
            }
            default -> {
                System.out.println("Указанное поле недоступно для обновления");
                return null;
            }
        }

        // Проверяем, изменилось ли какое-то из полей
        if (newName.equals(oldTask.getName()) &&
                newDescription.equals(oldTask.getDescription()) &&
                newStatus == oldTask.getStatus() &&
                Objects.equals(newStartTime, oldTask.getStartTime()) &&
                Objects.equals(newDuration, oldTask.getDuration())) {
            System.out.println("Изменений не обнаружено");
            return null;
        }

        return manager.updateTask(
                oldTask.getTaskType(),
                oldTask.getId(),
                newName,
                newDescription,
                newStatus,
                newStartTime,
                newDuration);
    }


    public static boolean deleteTaskById() {
        // выбрать задачу для удаления
        Task task = askTaskIdFromUser().orElse(null);
        if (task == null) return false;

        if (task instanceof Epic) {
            System.out.println("Задача будет удалена вместе в подзадачами. Подтвердить да(1)/нет(0).");
            String input = scanner.nextLine().trim();
            if (input.equals("0")) {
                System.out.println("Отмена операции удаления.");
                return false;
            }
        }
        return manager.deleteTask(task);
    }

    public static Optional<Task> getTaskIdAndSaveToHistory() {
        Optional<Task> optionalTask = askTaskIdFromUser();
        optionalTask.ifPresent(task -> manager.saveTaskToHistory(task.getId()));
        return optionalTask;
    }


    public static Optional<Task> askTaskIdFromUser() {
        System.out.print("Введите id задачи: ");
        String input = scanner.nextLine().trim();

        return Validators.validateTaskId(manager, scanner, input);
    }

    public static String askTaskNameFromUser() {
        String name = null;

        while (name == null || name.isBlank()) {
            System.out.print("Введите имя задачи: ");
            name = scanner.nextLine().trim();
        }
        return name;
    }

    public static String askTaskDescriptionFromUser() {
        System.out.print("Введите описание задачи, если оно есть: ");
        return scanner.nextLine().trim();
    }

    public static Epic askParentTaskFromUser() {
        System.out.print("Введите id родительской задачи: ");
        String input = scanner.nextLine().trim();

        try {
            // 1. Валидация числового ввода
            int id = Validators.validatePositiveIntInput(input,
                    "Ожидается положительное число: ",
                    scanner);

            // 2. Проверка существования задачи
            Optional<Task> taskOpt = Validators.validateTaskExists(id, manager);
            if (taskOpt.isEmpty()) {
                return null;
            }
            // 3. Проверка типа задачи Epic
            Optional<Epic> epicOpt = Validators.validateIsEpic(taskOpt.get());
            return epicOpt.orElse(null);

        } catch (Exception e) {
            System.out.println("Ошибка при обработке ввода: " + e.getMessage());
            return null;
        }
    }

    public static TaskType askTaskTypeFromUser() {
        TaskType taskType = null;

        while (taskType == null) {
            try {
                System.out.print("Введите тип задачи (EPIC, SUBTASK, TASK): ");
                String input = scanner.nextLine().trim().toUpperCase();
                taskType = TaskType.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка! Введите корректный тип задачи (EPIC, SUBTASK, TASK).");
            }
        }
        return taskType;
    }

    public static Status askTaskStatusFromUser() {
        Status status = null;

        while (status == null) {
            try {
                System.out.print("Введите статус задачи (NEW, IN_PROGRESS, DONE): ");
                String input = scanner.nextLine().trim().toUpperCase();
                status = Status.valueOf(input);
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка! Введите корректный статус задачи (NEW, IN_PROGRESS, DONE).");
            }
        }
        return status;
    }
}




