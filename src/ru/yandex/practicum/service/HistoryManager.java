package ru.yandex.practicum.service;

import ru.yandex.practicum.entity.Task;

import java.util.List;

public interface HistoryManager {

     void add(Task task);

     void remove(int id);

     void clear();

     List<Task> getHistory();
}

