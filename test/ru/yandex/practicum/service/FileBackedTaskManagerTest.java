package ru.yandex.practicum.service;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.entity.Epic;
import ru.yandex.practicum.entity.Subtask;
import ru.yandex.practicum.entity.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 1. Общие тесты (из TaskManagerTest):
 * Используют taskManager, созданный в beforeEach()
 * Работают с дефолтным временным файлом
 * <p>
 * 2. Специфичные тесты (в FileBackedTaskManagerTest):
 * Создают свои экземпляры FileBackedTaskManager.
 * Используют свои временные файлы.
 * Могут игнорировать taskManager из родительского класса
 * <p>
 * 3. Да, вы не можете избавиться от создания taskManager в абстрактном классе, но:
 * Для общих тестов это нужно и правильно
 * Для специфичных тестов можно создавать дополнительные менеджеры
 * Создание "лишнего" менеджера в специфичных тестах - это нормальная плата за унифицированную архитектуру тестов
 */


class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private Path testFile;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            testFile = Files.createTempFile("tasks", ".csv");
            return new FileBackedTaskManager(testFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp file", e);
        }
    }

    private Path createTestFile() throws IOException {
        testFile = Files.createTempFile("tasks", ".csv");
        return Files.write(testFile, Arrays.asList(
                "id,type,name,status,description,epic,startTime,duration",
                "1,Epic,Epic 1,NEW,Description, , , ,",
                "2,Task,Task 2,NEW,Description, , , ,",
                "3,Subtask,Subtask 3,NEW,Description 3,1, , ,"
        ));
    }

    private Path createEmptyTestFile() throws IOException {
        return Files.createTempFile("tasks", ".csv");
    }


    @Test
    void shouldLoadEmptyFileAndSaveMultipleTasks() throws IOException {
        Path tempFile = createEmptyTestFile();
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        int lineCount = Files.readAllLines(tempFile).size();

        assertEquals(0, lineCount, "Файл не должен содержать строки");
        assertTrue(manager.getAllTasks().isEmpty(), "Загруженный из файла менеджер должен быть пустым");

        Epic epic1 = new Epic("Epic 1", "Epic Description 1");
        Task task1 = new Task("Task 1", "Description");
        Subtask subtask1 = new Subtask("Subtask 1", "Sub Description 1", epic1, null, null);

        manager.addTask(task1);
        manager.addTask(epic1);
        manager.addTask(subtask1);

        int lineCountAfter = Files.readAllLines(tempFile).size();
        assertEquals(4, lineCountAfter, "Файл должен содержать 4 строки (заголовок + 3 задачи)");

        Files.deleteIfExists(tempFile);
    }

    @Test
    public void shouldLoadTasksFromFile() throws IOException {
        testFile = createTestFile();
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(testFile.toFile());

        assertEquals(3, manager.getAllTasks().size(), "Должны быть загружены 3 задачи");
        assertTrue(manager.getTaskById(1).isPresent(), "Эпик 1 должен существовать");
        assertTrue(manager.getTaskById(2).isPresent(), "Задача 2 должна существовать");
        assertTrue(manager.getTaskById(3).isPresent(), "Подзадача 3 должна существовать");

        Optional<Task> subtaskOpt = manager.getTaskById(3);
        assertTrue(subtaskOpt.isPresent() && subtaskOpt.get() instanceof Subtask);
        Subtask subtask = (Subtask) subtaskOpt.get();
        assertEquals(1, subtask.getParentEpic().getId(), "Подзадача должна быть связана с эпиком 1");

        Files.deleteIfExists(testFile);
    }

    @Test
    void shouldLoadMultipleTasksFromFile() throws IOException {
        testFile = createTestFile();
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(testFile.toFile());

        int loadedCount = Files.readAllLines(testFile).size() - 1;
        int taskCount = manager.getAllTasks().size();
        assertEquals(loadedCount, taskCount, "Количество задач в менеджере должно соответствовать" +
                " количеству строк в файле без учета заголовка");
        Files.deleteIfExists(testFile);
    }

    @Test
    void shouldLoadMultipleTasksAndDeleteSomeTask() throws IOException {
        testFile = createTestFile();
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(testFile.toFile());

        assertEquals(3, manager.getAllTasks().size(), "Должны быть загружены 3 задачи");

        Optional<Task> epicOpt = manager.getTaskById(1);
        assertTrue(epicOpt.isPresent() && epicOpt.get() instanceof Epic);
        manager.deleteTask(epicOpt.get());

        assertEquals(1, manager.getAllTasks().size(), "Должна остаться 1 задача");
        assertEquals(1, Files.readAllLines(testFile).size() - 1, "Должна остаться 1 задача");
        Files.deleteIfExists(testFile);
    }

    @Test
    void shouldLoadMultipleTasksAndDeleteAllTask() throws IOException {
        testFile = createTestFile();
        FileBackedTaskManager manager = FileBackedTaskManager.loadFromFile(testFile.toFile());

        assertEquals(3, manager.getAllTasks().size(), "Должны быть загружены 3 задачи");
        manager.clearAllTasks();

        assertEquals(0, manager.getAllTasks().size(), "Не должно остаться задач");
        assertEquals(0, Files.readAllLines(testFile).size() - 1, "Должно остаться 0 задач");
        Files.deleteIfExists(testFile);
    }

    @Test
    public void testLoadFromFileWithInvalidData() throws IOException {
        Path wrongTestFile = Files.createTempFile("tasks_invalid", ".csv");
        Files.write(wrongTestFile, Arrays.asList(
                "id,type,name,status,description,epic",
                "invalid,TASK,Task 1,NEW,Description 1"
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
                "1,SUBTASK,Subtask 1,NEW,Description 1"
        ));
        assertThrows(RuntimeException.class,
                () -> FileBackedTaskManager.loadFromFile(wrongTestFile.toFile()),
                "Должно выбрасываться исключение при отсутствии эпика"
        );
        Files.deleteIfExists(wrongTestFile);
    }
}