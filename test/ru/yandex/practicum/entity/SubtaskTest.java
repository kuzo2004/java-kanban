package ru.yandex.practicum.entity;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    @Test
    void testEqualsSubtask() {

        Epic epic = new Epic("Epic 1", "Description 1");

        Subtask subtask1 = new Subtask(1, "Subtask 1", "Description 1", Status.NEW, epic);
        Subtask subtask2 = new Subtask(1, "Subtask 2", "Description 2", Status.IN_PROGRESS, epic);

        assertEquals(subtask1, subtask2, "Subtask с одинаковым id должны быть равны");

        Subtask subtask3 = new Subtask("Subtask 3", "Description 3", epic);

        assertNotEquals(subtask1, subtask3, "Subtask с разными id не должны быть равны");
    }

    @Test
    public void testSubtaskCannotBeEpic() {

        Epic epic = new Epic(1, "Epic 1", "Description", new HashMap<>());
        // Создаем подзадачу, связанную с эпиком
        Subtask subtask1 = new Subtask(2, "Subtask 1", "Description", Status.NEW, epic);

        // Попытка создать другую подзадачу, используя subtask1 как родительский эпик
        Subtask subtask2 = new Subtask(3, "Subtask 2", "Description", Status.NEW, subtask1);

        assertNull(subtask2.getParentEpic(), "Нельзя подзадачу назначить чьем-то эпиком.");
    }
}