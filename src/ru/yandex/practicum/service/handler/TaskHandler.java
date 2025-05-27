package ru.yandex.practicum.service.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.service.TaskManager;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    public TaskHandler(String path, TaskManager managers, Gson gson) {
        super(path, managers, gson);
    }
}
