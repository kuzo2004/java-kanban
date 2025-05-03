package ru.yandex.practicum.manager;


import ru.yandex.practicum.service.*;

import java.nio.file.Paths;


public class Managers {

    public static TaskManager getDefault() {
        return new FileBackedTaskManager(Paths.get("Tacks.csv"));
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
