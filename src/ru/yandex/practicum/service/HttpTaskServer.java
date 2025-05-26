package ru.yandex.practicum.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.practicum.entity.Epic;
import ru.yandex.practicum.entity.Subtask;
import ru.yandex.practicum.entity.Task;
import ru.yandex.practicum.manager.Managers;
import ru.yandex.practicum.service.handler.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;


public class HttpTaskServer {
    private int port;
    private TaskManager manager;
    private Gson  gson;
    private HttpServer httpServer;

   public HttpTaskServer(TaskManager manager, int port) {
        this.manager = manager;
        this.port = port;
        this.gson = getGson();
    }

    public Gson getGson() {
        return new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();
    }

    public void start() throws IOException {
        if (httpServer != null) {
            throw new IllegalStateException("Сервер уже запущен");
        }
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        // Создаем обработчики с передачей базового пути
        httpServer.createContext("/tasks", new TaskHandler("/tasks", manager, gson));
        httpServer.createContext("/subtasks", new SubtaskHandler("/subtasks", manager, gson));
        httpServer.createContext("/epics", new EpicHandler("/epics", manager, gson));
        httpServer.createContext("/history", new HistoryHandler("/history", manager, gson));
        httpServer.createContext("/prioritized", new PrioritizedHandler("/prioritized", manager, gson));

        httpServer.start();
        System.out.println("HTTP-сервер запущен на порту " + port);
    }


    public void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            System.out.println("HTTP-сервер остановлен");
        }
    }

    public static void main(String[] args) throws IOException {

        TaskManager manager = Managers.getDefault();
        initializeTestData(manager);

        HttpTaskServer server = new HttpTaskServer(manager, 8080);
        server.start();

        }

    private static void initializeTestData(TaskManager manager) {
        //  тестовые данные
        Epic tTask0 = new Epic("Переезд", "");
        Subtask tTask1 = new Subtask("Собрать коробки", "Положить все вещи в коробки", tTask0,
                LocalDateTime.of(2025, 5, 14, 9, 30), Duration.ofMinutes(60));
        Subtask tTask2 = new Subtask("Упаковать кошку", "Положить кошку в клетку", tTask0,
                LocalDateTime.of(2025, 5, 14, 10, 30), Duration.ofMinutes(30));
        Task tTask3 = new Task("Включить чайник", "вскипятить 1.5 литра воды",
                LocalDateTime.of(2025, 5, 14, 11, 0), Duration.ofMinutes(10));
        Task tTask4 = new Task("Заварить чай", "зеленый китайский",
                LocalDateTime.of(2025, 5, 14, 11, 10), Duration.ofMinutes(5));
        Epic tTask5 = new Epic("Сделать проект", "прогноз рынка гаджетов");
        Subtask tTask6 = new Subtask("Найти информацию", "Продажи гаджетов по годам", tTask5,
                LocalDateTime.of(2025, 5, 14, 12, 0), Duration.ofMinutes(120));

        Stream.of(tTask0, tTask1, tTask2, tTask3, tTask4, tTask5, tTask6)
                .forEach(manager::addTask);
        manager.getHistoryManager().add(tTask3);
        manager.getHistoryManager().add(tTask6);
    }
}
