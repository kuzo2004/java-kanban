package ru.yandex.practicum.service.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.service.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {
    public PrioritizedHandler(String path, TaskManager managers, Gson gson) {
        super(path, managers, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod()) &&
                    "/prioritized".equals(exchange.getRequestURI().getPath())) {

                String body = gson.toJson(manager.getPrioritizedTasks());
                sendText(exchange, body);
            } else {
                sendResponse(exchange, "Такой команды нет.", 405);
            }
        } catch (Error e) {
            sendResponse(exchange, e.getMessage(), 500);
        }
    }
}
