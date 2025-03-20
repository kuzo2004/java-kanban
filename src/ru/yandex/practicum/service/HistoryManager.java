package ru.yandex.practicum.service;

import ru.yandex.practicum.entity.Task;

import java.util.List;

public interface HistoryManager {

    public void add(Task task);

    public List<Task> getHistory();
}
