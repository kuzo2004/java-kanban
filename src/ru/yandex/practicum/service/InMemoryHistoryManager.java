package ru.yandex.practicum.service;

import ru.yandex.practicum.entity.*;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private List<Task> lastTasks;

    public InMemoryHistoryManager() {
        this.lastTasks = new ArrayList<>();
    }

    @Override
    public void add(Task task) {
        if (task != null) {
            lastTasks.addLast(task);
        }
        while (lastTasks.size() > 10) {
            lastTasks.removeFirst();
        }
    }


    @Override
    public List<Task> getHistory() {
        return lastTasks;
    }
}
