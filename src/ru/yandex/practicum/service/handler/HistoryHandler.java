package ru.yandex.practicum.service.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.service.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    public HistoryHandler(String path, TaskManager managers, Gson gson) {
        super(path, managers, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        try {
            if ("GET".equals(exchange.getRequestMethod()) &&
                    "/history".equals(exchange.getRequestURI().getPath())) {

                String body = gson.toJson(manager.getHistory());
                sendText(exchange, body);
            } else {
                sendResponse(exchange, "Такой команды нет.", 405);
            }
        } catch (Error e) {
            sendResponse(exchange, e.getMessage(), 500);
        }
    }
}
