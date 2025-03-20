package ru.yandex.practicum.entity;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test
    void testEqualsEpic() {

        Epic epic1 = new Epic(1, "Epic 1", "Description 1", new HashMap<>());
        Epic epic2 = new Epic(1, "Epic 2", "Description 2", new HashMap<>());

        assertEquals(epic1, epic2, "Epic с одинаковым id должны быть равны");

        Epic epic3 = new Epic(2, "Epic 3", "Description 3", new HashMap<>());
        assertNotEquals(epic1, epic3, "Epic с разными id не должны быть равны");
    }

    @Test
    void testEpicCannotAddAsSubtask() {

        Epic epic1 = new Epic(1, "Epic 1", "Description 1", new HashMap<>());

        Map<Integer, Task> subtasksList = new HashMap<>();
        subtasksList.put(epic1.getId(), epic1);
        Epic epic2 = new Epic(2, "Epic 2", "Description 2", subtasksList);

        assertFalse(epic2.subtasks.containsKey(epic1.getId()),
                "Epic нельзя добавлять в список подзадач");
    }
}