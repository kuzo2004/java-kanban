package ru.yandex.practicum.service;

import org.junit.jupiter.api.*;
import ru.yandex.practicum.entity.Epic;
import ru.yandex.practicum.entity.Subtask;
import ru.yandex.practicum.entity.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FileBackedTaskManagerTest {

    private static Path testFile;
    private FileBackedTaskManager manager;


    private Path createTestFile() throws IOException {
        // Создаем временный файл для теста
        testFile = Files.createTempFile("tasks", ".csv");
        return Files.write(testFile, Arrays.asList(
                "id,type,name,status,description,epic",
                "1,Epic,Epic 1,NEW,Description 1,",
                "2,Task,Task 2,NEW,Description 2,",
                "3,Subtask,Subtask 3,NEW,Description 3,1,"
        ));
    }

    private Path createEmptyTestFile() throws IOException {
        return Files.createTempFile("tasks", ".csv");
    }


    @Test
    void shouldLoadEmptyFileAndSaveMultipleTasks() throws IOException {
        // Загрузили пустой файл
        Path tempFile = createEmptyTestFile();
        manager = new FileBackedTaskManager(tempFile);

        int lineCount = Files.readAllLines(tempFile).size();

        assertEquals(0, lineCount, "Файл не должен содержать строки");
        assertTrue(manager.getAllTasks().isEmpty(), "Загруженный из файла менеджер должен быть пустым");

        // Создаем тестовые задачи
        Epic epic1 = new Epic("Epic 1", "Epic Description 1");
        Task task1 = new Task("Task 1", "Description 1");
        Subtask subtask1 = new Subtask("Subtask 1", "Sub Description 1", epic1);

        // Добавляем задачи (это автоматически вызовет save())
        manager.addTask(task1);
        manager.addTask(epic1);
        manager.addTask(subtask1);

        // Проверяем, что файл содержит ожидаемое количество строк (заголовок + 3 задачи = 4 строки)
        int lineCountAfter = Files.readAllLines(tempFile).size();
        assertEquals(4, lineCountAfter, "Файл должен содержать 4 строки (заголовок + 3 задачи)");

        Files.deleteIfExists(tempFile);
    }

    @Test
    public void shouldLoadTasksFromFile() throws IOException {
        // Загружаем  файл c 3-мя задачами
        testFile = createTestFile();
        manager = FileBackedTaskManager.loadFromFile(testFile.toFile());

        assertEquals(3, manager.getAllTasks().size(), "Должны быть загружены 3 задачи");
        assertTrue(manager.getTaskById(1).isPresent(), "Эпик 1 должен существовать");
        assertTrue(manager.getTaskById(2).isPresent(), "Задача 2 должна существовать");
        assertTrue(manager.getTaskById(3).isPresent(), "Подзадача 3 должна существовать");

        // Проверяем связь подзадачи с эпиком
        Optional<Task> subtaskOpt = manager.getTaskById(3);
        assertTrue(subtaskOpt.isPresent() && subtaskOpt.get() instanceof Subtask);
        Subtask subtask = (Subtask) subtaskOpt.get();
        assertEquals(1, subtask.getParentEpic().getId(), "Подзадача должна быть связана с эпиком 1");

        Files.deleteIfExists(testFile);
    }

    @Test
    void shouldLoadMultipleTasksFromFile() throws IOException {
        // Загружаем  файл c 3-мя задачами
        testFile = createTestFile();
        manager = FileBackedTaskManager.loadFromFile(testFile.toFile());

        // Проверяем что количество задач в менеджере соответствует количеству строк в файле без учета заголовка
        int loadedCount = Files.readAllLines(testFile).size() - 1;
        int taskCount = manager.getAllTasks().size();
        assertEquals(loadedCount, taskCount, "Количество задач в менеджере должно соответствовать" +
                " количеству строк в файле без учета заголовка");
        Files.deleteIfExists(testFile);
    }

    @Test
    void shouldLoadMultipleTasksAndDeleteSomeTask() throws IOException {
        // Загружаем  файл c 3-мя задачами
        testFile = createTestFile();
        manager = FileBackedTaskManager.loadFromFile(testFile.toFile());

        assertEquals(3, manager.getAllTasks().size(), "Должны быть загружены 3 задачи");

        Optional<Task> epicOpt = manager.getTaskById(1);
        assertTrue(epicOpt.isPresent() && epicOpt.get() instanceof Epic);
        manager.deleteTask(epicOpt.get());

        // Количество задач в менеджере после удаления (если был epic, то он удалился с подзадачей)
        assertEquals(1, manager.getAllTasks().size(), "Должна остаться 1 задача");

        // Обновленное количество строк в файле за вычетом заголовка
        assertEquals(1, Files.readAllLines(testFile).size() - 1, "Должна остаться 1 задача");
        Files.deleteIfExists(testFile);
    }

    @Test
    void shouldLoadMultipleTasksAndDeleteAllTask() throws IOException {
        // Загружаем  файл c 3-мя задачами
        testFile = createTestFile();
        manager = FileBackedTaskManager.loadFromFile(testFile.toFile());

        assertEquals(3, manager.getAllTasks().size(), "Должны быть загружены 3 задачи");
        manager.clearAllTasks();

        // Количество задач в менеджере после удаления 0
        assertEquals(0, manager.getAllTasks().size(), "Не должно остаться задач");

        // Обновленное количество строк в файле за вычетом заголовка
        assertEquals(0, Files.readAllLines(testFile).size() - 1, "Должно остаться 0 задач");
        Files.deleteIfExists(testFile);
    }

    @Test
    public void testLoadFromFileWithInvalidData() throws IOException {
        Path wrongTestFile = Files.createTempFile("tasks_invalid", ".csv");
        Files.write(wrongTestFile, Arrays.asList(
                "id,type,name,status,description,epic",
                "invalid,TASK,Task 1,NEW,Description 1" // Неверный ID
        ));


        assertThrows(RuntimeException.class,
                () -> FileBackedTaskManager.loadFromFile(wrongTestFile.toFile()),
                "Должно выбрасываться исключение при неверных данных"
        );

        Files.deleteIfExists(wrongTestFile);
    }

    @Test
    public void testCreateSubtaskWithoutEpic() throws IOException {
        Path wrongTestFile = Files.createTempFile("tasks_invalid", ".csv");
        Files.write(wrongTestFile, Arrays.asList(
                "id,type,name,status,description,epic",
                "1,SUBTASK,Subtask 1,NEW,Description 1" // нет родительской задачи
        ));
        assertThrows(RuntimeException.class,
                () -> FileBackedTaskManager.loadFromFile(wrongTestFile.toFile()),
                "Должно выбрасываться исключение при отсутствии эпика"
        );
        Files.deleteIfExists(wrongTestFile);
    }
}