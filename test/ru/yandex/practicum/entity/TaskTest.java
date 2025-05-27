package ru.yandex.practicum.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TaskTest {
    @Test
    void testEqualsTask() {

        Task task1 = new Task(1, "Task 1", "", null, null);
        Task task2 = new Task(1, "Task 2", "", null, null);
        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");


        Task task3 = new Task(2, "Task 3", "", null, null);
        assertNotEquals(task1, task3, "Задачи с разными id не должны быть равны");
    }
}
