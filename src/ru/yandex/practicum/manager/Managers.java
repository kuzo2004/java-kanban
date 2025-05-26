package ru.yandex.practicum.manager;


import ru.yandex.practicum.service.HistoryManager;
import ru.yandex.practicum.service.InMemoryHistoryManager;
import ru.yandex.practicum.service.InMemoryTaskManager;
import ru.yandex.practicum.service.TaskManager;

import java.io.File;


public class Managers {

    private static final File DEFAULT_TASKS_FILE = new File("tasks.csv");

    // поле для целей тестирования, чтобы подменять на временные файлы в момент теста
    private static File tasksFile = DEFAULT_TASKS_FILE;

    public static void setDefaultTasksFile(File file) {
        tasksFile = file;
    }


    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
        // менеджер можно переключать на
        // FileBackedTaskManager.loadFromFile(tasksFile);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}