package ru.yandex.practicum;

import ru.yandex.practicum.entity.*;
import ru.yandex.practicum.manager.Managers;
import ru.yandex.practicum.service.TaskManager;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;


public class Main {
    public static Scanner scanner = new Scanner(System.in);
    public static TaskManager manager = Managers.getDefault();

    public static void main(String[] args) {

        // тестовые данные
        Epic tTask0 = new Epic("Переезд", "");
        Subtask tTask1 = new Subtask("Собрать коробки", "Положить все вещи в коробки", tTask0);
        Subtask tTask2 = new Subtask("Упаковать кошку", "Положить кошку в клетку", tTask0);
        Task tTask3 = new Task("Включить чайник", "вскипятить 1.5 литра воды");
        Task tTask4 = new Task("Заварить чай", "зеленый китайский");
        Epic tTask5 = new Epic("Сделать проект", "прогноз рынка гаджетов");
        Subtask tTask6 = new Subtask("Найти информацию", "Продажи гаджетов по годам", tTask5);
        manager.addTask(tTask0);
        manager.addTask(tTask1);
        manager.addTask(tTask2);
        manager.addTask(tTask3);
        manager.addTask(tTask4);
        manager.addTask(tTask5);
        manager.addTask(tTask6);


        // основной диалог с пользователем
        while (true) {
            System.out.println();
            printMenu();
            String command = scanner.nextLine().trim();
            switch (command) {
                case "1": // Получение списка задач по типу
                    Map<Integer, Task> allTasks1 = getTasksByType();
                    printTasks(allTasks1);
                    break;
                case "2": // Удаление задач по типу
                    clearTasksByType();
                    break;
                case "3": // Получение задачи по идентификатору и запись задачи в историю
                    Optional<Task> optionalTask = getTaskIdAndSaveToHistory();
                    if (optionalTask.isPresent()) {
                        Task task3 = optionalTask.get();
                        System.out.println("Найдена задача: " + task3);
                    }
                    break;
                case "4": // Создание новой задачи
                    Task task4 = prepareAndCreateTask();
                    if (task4 != null) {
                        System.out.println("Создана задача: " + task4);
                    } else {
                        System.out.println("Задача не создана.");
                    }
                    break;
                case "5": // Обновление задачи по идентификатору
                    Task task5 = prepareAndUpdateTask();
                    if (task5 != null) {
                        System.out.println("Обновлена задача: " + task5);
                    }
                    break;
                case "6": // Удаление задачи по идентификатору
                    if (deleteTaskById()) {
                        System.out.println("Задача удалена.");
                    }
                    break;
                case "7": // Получение списка всех подзадач определённого эпика
                    Epic parentTask = askParentTaskFromUser();

                    if (parentTask != null) {
                        System.out.println("Выбранная задача \n" + parentTask);
                        Optional<Map<Integer, Task>> subtasksOptional = manager.getSubtasksByEpic(parentTask);

                        if (subtasksOptional.isPresent()) {
                            Map<Integer, Task> allTasks7 = subtasksOptional.get();
                            System.out.println("содержит подзадачи");
                            printTasks(allTasks7);
                        }
                    }
                    break;
                case "8": // Выход из программы
                    System.out.println("История просмотренных задач");
                    List<Task> historyTasks = manager.getHistory();
                    if (historyTasks.isEmpty()) {
                        System.out.println("История просмотренных задач пуста.");
                    } else {
                        for (Task t : historyTasks) {
                            System.out.print(t.getId() + " ");
                        }
                    }
                    break;
                case "9": // Выход из программы
                    System.out.println("Завершение программы. До свиданья!");
                    return;
                default:
                    System.out.println("Нет такой команды. Введите число от 1 до 9");
            }

        }
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
        System.out.println("9 - Выход из программы.");
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
            System.out.println("Задачи с типом EPIC будут удалены вместе с подзадачами. Подтвердить удаление да(1)/нет(0).");
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

        // фабрика создания задачи
        return manager.createTask(taskType, name, description, parentTask);
    }

    public static Task prepareAndUpdateTask() {

        // выбрать задачу для обновления
        Optional<Task> optionalTask = askTaskIdFromUser();
        if (optionalTask.isEmpty()) {
            return null;
        }
        Task oldTask = optionalTask.get();

        // подготовка полей к обновлению, заполним их исходными значениями
        String newName = oldTask.getName();
        String newDescription = oldTask.getDescription();
        Status newStatus = oldTask.getStatus();

        // выбор поля для обновления, так как за один раз обновляем только одно поле в задаче
        System.out.print("Введите поле, которое хотите обновить: name, description, status:");
        String input = scanner.nextLine().trim();


        switch (input) {
            case "name":
                newName = askTaskNameFromUser();
                break;
            case "description":
                newDescription = askTaskDescriptionFromUser();
                break;
            case "status":
                if (oldTask instanceof Epic) {
                    System.out.println("Статус у задачи Epic нельзя поменять вручную, он обновляется автоматически.");
                    return null;
                }
                newStatus = askTaskStatusFromUser();
                break;
            default:
                System.out.println("Поле <" + input + "> нельзя обновить");
        }

        // Проверяем, изменилось ли какое-то из полей
        boolean isSame = newName.equals(oldTask.getName()) &&
                newDescription.equals(oldTask.getDescription()) &&
                newStatus == oldTask.getStatus();

        if (isSame) {
            System.out.println("Вы не изменили ни одно поле.");
            return null;
        }

        return manager.updateTask(oldTask.getTaskType(),
                oldTask.getId(),
                newName,
                newDescription,
                newStatus);
    }


    public static boolean deleteTaskById() {
        // выбрать задачу для удаления
        Optional<Task> optionalTask = askTaskIdFromUser();

        if (optionalTask.isEmpty()) {
            return false;
        }
        Task task = optionalTask.get();
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
        if (optionalTask.isPresent()) {
            Task task = optionalTask.get();
            manager.saveTaskToHistory(task.getId());
        }
        return optionalTask;
    }


    public static Optional<Task> askTaskIdFromUser() {

        System.out.print("Введите id задачи: ");
        String input = scanner.nextLine().trim();

        while (!isPositiveInteger(input)) {
            System.out.print("Ожидается положительное число.\nВведите id задачи: ");
            input = scanner.nextLine().trim();
        }

        int id = Integer.parseInt(input);
        Optional<Task> optionalTask = manager.getTaskById(id);

        if (optionalTask.isEmpty()) {
            System.out.println("Задача " + id + " не найдена. Проверьте список задач.");
        }
        return optionalTask;
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

        while (!isPositiveInteger(input)) {
            System.out.print("Ожидается положительное число: ");
            input = scanner.nextLine().trim();
        }

        int id = Integer.parseInt(input);
        Optional<Task> optionalTask = manager.getTaskById(id);
        if (optionalTask.isEmpty()) {
            System.out.println("Задача " + id + " не найдена. Проверьте список задач.");
            return null;
        }
        Task parentTask = optionalTask.get();
        if (!(parentTask instanceof Epic)) {
            System.out.println("Родительская задача " + id + " не найдена. Проверьте список задач с типом EPIC");
            return null;
        }

        return (Epic) parentTask;
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

    public static boolean isPositiveInteger(String input) {
        return !input.isEmpty() && input.matches("[1-9]\\d*");
    }
}




