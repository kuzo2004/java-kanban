package ru.yandex.practicum.service.validation;

import ru.yandex.practicum.entity.Epic;
import ru.yandex.practicum.entity.Task;
import ru.yandex.practicum.service.TaskManager;

import java.time.Duration;
import java.util.Optional;
import java.util.Scanner;


public final class Validators {
    // Приватный конструктор
    private Validators() {
    }

    // Проверка валидности длительности задачи
    public static final class DurationValidator {
        public static Duration parse(String input) {
            if (input == null || input.trim().isEmpty()) return null;
            try {
                long minutes = Long.parseLong(input.trim());
                if (minutes < 0) {
                    throw new IllegalArgumentException("Длительность не может быть отрицательной");
                }
                if (minutes > Task.MAX_DURATION_MINUTES) {
                    throw new IllegalArgumentException("Максимальная длительность: " +
                            Task.MAX_DURATION_MINUTES + " минут");
                }
                return Duration.ofMinutes(minutes);
            } catch (IllegalArgumentException e) {
                System.out.println("Указан не допустимый формат длительности задачи." + e.getMessage());
            }
            System.out.println("Задача зарегистрирована без указания длительности.");
            return null;
        }
    }

    // Проверка ввода типа задачи (Epic)
    public static Optional<Epic> validateIsEpic(Task task) {
        if (task instanceof Epic epic) {
            return Optional.of(epic);
        }
        System.out.println("Задача " + task.getId() + " не является Epic");
        return Optional.empty();
    }

    // Поверяет ID вводимое пользователем и ищет по нему задачу
    public static Optional<Task> validateTaskId(TaskManager manager, Scanner scanner, String input) {
        int id = validatePositiveIntInput(input,
                "Ожидается положительное число.\nВведите id задачи: ",
                scanner);

        return validateTaskExists(id, manager);
    }

    // Проверка ввода положительного числа
    public static int validatePositiveIntInput(String input, String promptMessage, Scanner scanner) {
        while (!isPositiveInteger(input)) {
            System.out.print(promptMessage);
            input = scanner.nextLine().trim();
        }
        return Integer.parseInt(input);
    }

    // Проверяет существования задачи
    public static Optional<Task> validateTaskExists(int id, TaskManager manager) {
        return manager.getTaskById(id)
                .or(() -> {
                    System.out.println("Задача " + id + " не найдена");
                    return Optional.empty();
                });
    }

    public static boolean isPositiveInteger(String input) {
        return !input.isEmpty() && input.matches("[1-9]\\d*");
    }
}
