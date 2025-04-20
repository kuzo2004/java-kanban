package ru.yandex.practicum.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    @Test
    void testEqualsTask() {

        Task task1 = new Task(1, "Task 1", "");
        Task task2 = new Task(1, "Task 2", "");
        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");


        Task task3 = new Task(2, "Task 3", "");
        assertNotEquals(task1, task3, "Задачи с разными id не должны быть равны");
    }
}
