package ru.yandex.practicum.service.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.service.TaskManager;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    public SubtaskHandler(String path, TaskManager managers, Gson gson) {
        super(path, managers, gson);
    }
}
