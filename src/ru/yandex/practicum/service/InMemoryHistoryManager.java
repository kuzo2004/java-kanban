package ru.yandex.practicum.service;

import ru.yandex.practicum.entity.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private Node head;
    private Node tail;
    private Map<Integer, Node> tasksHistoryMap;


    public InMemoryHistoryManager() {
        tasksHistoryMap = new HashMap<>();

    }

    @Override
    public void add(Task task) {
        if (task != null) {
            int id = task.getId();
            if (tasksHistoryMap.containsKey(id)) {
                remove(id);
            }
            linkLast(task);
        }
    }

    public void linkLast(Task task) {
        if (task == null) {
            return;
        }
        int id = task.getId();
        Node newNode = new Node(tail, task, null);
        tasksHistoryMap.put(id, newNode);

        if (head == null) {
            head = newNode;
        } else {
            tail.next = newNode;
        }
        tail = newNode;
    }

    @Override
    public void remove(int id) {

        if (!tasksHistoryMap.containsKey(id)) {
            return;
        }
        Node oldNode = tasksHistoryMap.remove(id);
        removeNode(oldNode);
    }

    private void removeNode(Node node) {
        if (node == null) {
            return;
        }

        if (head == node && tail == node) {
            // Если был единственный узел
            head = tail = null;
        } else if (node == head) {
            // Если удаляем голову
            head = node.next;
            head.prev = null;
        } else if (node == tail) {
            // Если удаляем хвост
            tail = node.prev;
            tail.next = null;
        } else {
            // Если удаляем узел из середины
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }

        // Обнуляем ссылки удаляемого узла (помогаем сборщику мусора)
        node.prev = null;
        node.next = null;
        node.task = null;
    }

    @Override
    public void clear() {

        //Стандартный LinkedList  рекомендует очищать все ссылки
        if (tasksHistoryMap.isEmpty()) {
            return;
        }

        for (Node x = head; x != null; ) {
            Node next = x.next;
            x.task = null;
            x.next = null;
            x.prev = null;
            x = next;
        }
        head = tail = null;
        tasksHistoryMap.clear();
    }

    @Override
    public List<Task> getHistory() {

        if (tasksHistoryMap.isEmpty()) {
            return Collections.emptyList();
        }

        List<Task> result = new ArrayList<>(tasksHistoryMap.size());
        for (Node current = head; current != null; current = current.next) {
            result.add(current.task.copy());
        }

        return Collections.unmodifiableList(result);
    }

    private static class Node {
        Task task;
        Node next;
        Node prev;

        Node(Node prev, Task task, Node next) {
            this.task = task;
            this.next = next;
            this.prev = prev;
        }
    }
}

