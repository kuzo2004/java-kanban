package ru.yandex.practicum.service;

import ru.yandex.practicum.entity.*;
import ru.yandex.practicum.exceptions.ManagerSaveException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {
    private Path path;

    public FileBackedTaskManager(Path path) {
        super();
        this.path = path;
        loadFromFile(path);
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void clearAllTasks() {
        super.clearAllTasks();
        save();
    }

    @Override
    public boolean deleteTask(Task task) {
        boolean isDeleted = super.deleteTask(task);
        save();
        return isDeleted;
    }

    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("id,type,name,status,description,epic" + System.lineSeparator());
            for (Task task : tasks.values()) {
                String processedLine = task.writeToString() + System.lineSeparator();
                writer.write(processedLine);
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить в файл " + path.toString());
        }
    }

    public void loadFromFile(Path path) {
        int sizeBeforeLoad = tasks.size();
        try (BufferedReader br = Files.newBufferedReader(path)) {
            br.readLine(); // пропуск 1 строки
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                createTaskFromCsvLine(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать файл " + path, e);
        }
        int numberLoaded = tasks.size() - sizeBeforeLoad;
        System.out.println("Загружено " + numberLoaded + " задач.");
    }

    public void createTaskFromCsvLine(String value) {
        String[] fields = value.split(",");
        if (fields.length < 5) {
            throw new RuntimeException("Недостаточно данных в строке: " + value);
        }

        try {
            int id = Integer.parseInt(fields[0]);
            TaskType type = TaskType.valueOf(fields[1].toUpperCase());
            String name = fields[2];
            Status status = Status.valueOf(fields[3]);
            String description = fields[4];

            switch (type) {
                case TASK, EPIC -> createExistingTask(id, type, name, description, status, null);
                case SUBTASK -> {
                    if (fields.length < 6) {
                        throw new RuntimeException("Для подзадачи не указан эпик");
                    }
                    Epic parentEpic = (Epic) tasks.get(Integer.parseInt(fields[5]));
                    createExistingTask(id, type, name, description, status, parentEpic);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Неверный формат данных для создания задачи:" + value, e);
        }
    }

    public void createExistingTask(int id, TaskType taskType, String name,
                                   String description, Status status, Epic parentEpic) {
        Task task = switch (taskType) {
            case TASK -> new Task(id, name, description, status);
            case EPIC -> new Epic(id, name, description);
            case SUBTASK -> new Subtask(id, name, description, status, parentEpic);
        };
        super.addTask(task);
    }
}
