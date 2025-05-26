package ru.yandex.practicum.service.handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.yandex.practicum.entity.*;
import ru.yandex.practicum.exceptions.TaskIdConflictException;
import ru.yandex.practicum.exceptions.TimeConflictException;
import ru.yandex.practicum.service.TaskManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

class TaskListTypeToken extends TypeToken<List<Task>> {
    // здесь ничего не нужно реализовывать
}

public class BaseHttpHandler implements HttpHandler {
    public final String PATH;
    public final TaskManager manager;
    public final Gson gson;

    public BaseHttpHandler(String path, TaskManager managers, Gson gson) {
        this.PATH = path;
        this.manager = managers;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "GET" -> get(exchange);
                case "POST" -> post(exchange);
                case "DELETE" -> delete(exchange);
                default -> sendResponse(exchange, "Такой команды нет.", 405);
            }
        } catch (TaskIdConflictException | TimeConflictException e) {
            sendHasInteractions(exchange, "Not Acceptable");  //406
        } catch (NoSuchElementException e) {
            sendNotFound(exchange, "Not Found"); //404
        } catch (IllegalArgumentException e) {
            sendIllegalArgument(exchange, e.getMessage());//400
        } catch (Exception e) {
            sendResponse(exchange, e.getMessage(), 500);
        }

    }

    private void delete(HttpExchange exchange) throws IOException {
        TaskType taskTypePath = TaskType.valueOf(seekTaskTypeStringFromPath(PATH));
        String param = getPathParam(exchange);
        if (param.isEmpty()) {
            throw new IllegalArgumentException("Нет такой команды.");// сразу все задачи по условию нельзя удалить
        } else {
            int id = Integer.parseInt(param);
            Task task = manager.getTaskById(id).orElseThrow();  //NoSuchElementException
            if (task.getTaskType() != taskTypePath) {
                throw new IllegalArgumentException("Задача с данным id не соответствует типу вызываемой задачи");
            }
            manager.deleteTask(task);
        }
        sendText(exchange, ""); //200
    }

    private void get(HttpExchange exchange) throws IOException {
        TaskType taskTypePath = TaskType.valueOf(seekTaskTypeStringFromPath(PATH));
        String param = getPathParam(exchange);

        if (param.isEmpty()) {
            String body = gson.toJson(manager.getAllTasksByType(taskTypePath));
            sendText(exchange, body);
            return;
        }

        String[] paramArray = param.split("/");
        int id = Integer.parseInt(paramArray[0]);
        Task task = manager.getTaskById(id).orElseThrow();

        if (task.getTaskType() != taskTypePath) {
            throw new IllegalArgumentException("Задача с данным id не соответствует типу вызываемой задачи");
        }

        if (taskTypePath == TaskType.EPIC && paramArray.length == 2 && "subtasks".equals(paramArray[1])) {
            // Обработка /epics/{id}/subtasks
            Map<Integer, Task> taskList = manager.getSubtasksByEpic((Epic) task).orElse(new HashMap<>());
            taskList.put(id, task);
            sendText(exchange, gson.toJson(taskList));
        } else {
            // Обработка /epics/{id}  либо /tasks/{id} либо /subtasks/{id}
            sendText(exchange, gson.toJson(task));
        }
    }


    private void post(HttpExchange exchange) throws IOException {
        String param = getPathParam(exchange);

        if (param.isEmpty()) {  //create
            Task task = parseTaskFromJson(exchange, false);
            manager.addTask(task);

            // Отправляем ответ клиенту (201 create)
            String body = "Задача создана, ID: " + task.getId();
            sendTextAfterCreateAndUpdate(exchange, body); //201

        } else { //update
            Task task = parseTaskFromJson(exchange, true);
            manager.addTask(task);
            // Отправляем ответ клиенту (201 update)
            String body = "Задача обновлена, ID: " + task.getId();
            sendTextAfterCreateAndUpdate(exchange, body); //201

        }
    }

    protected void sendResponse(HttpExchange h, String text, int statusCode) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(statusCode, resp.length);
        try (OutputStream os = h.getResponseBody()) {
            os.write(resp);
        }
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        sendResponse(h, text, 200);
    }

    protected void sendTextAfterCreateAndUpdate(HttpExchange h, String text) throws IOException {
        sendResponse(h, text, 201);
    }

    protected void sendNotFound(HttpExchange h, String text) throws IOException {
        sendResponse(h, text, 404);
    }

    protected void sendHasInteractions(HttpExchange h, String text) throws IOException {
        sendResponse(h, text, 406);
    }

    protected void sendIllegalArgument(HttpExchange h, String text) throws IOException {
        sendResponse(h, text, 400);
    }


    protected String getPathParam(HttpExchange exchange) {
        String requestedPath = exchange.getRequestURI().getPath();
        String beginPath = PATH + "/";
        return requestedPath.substring(requestedPath.indexOf(beginPath) + beginPath.length());
    }

    protected Task parseTaskFromJson(HttpExchange exchange, boolean isUpdate) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(
                exchange.getRequestBody(), StandardCharsets.UTF_8)) {

            TaskType taskTypePath = TaskType.valueOf(seekTaskTypeStringFromPath(PATH));

            // gson парсер почему-то видит только отдельный класс TaskDto, в внутренний - не видит.
            TaskDto taskDto = gson.fromJson(reader, TaskDto.class);

            // Валидация обязательных полей
            if (taskDto.name == null || taskDto.name.isBlank()) {
                throw new IllegalArgumentException("Required fields: name");
            }
            // Для подзадачи проверяем parentEpicId (при создании)
            Epic parentEpic = null;
            if (taskTypePath == TaskType.SUBTASK) {
                if (taskDto.parentEpicId == null) {
                    throw new IllegalArgumentException("For SUBTASK, parentEpicId is required");
                }
                Task findedParentTask = manager.getTaskById(taskDto.parentEpicId).orElseThrow();
                if (!(findedParentTask instanceof Epic)) {
                    throw new IllegalArgumentException("Parent epic not found");
                } else {
                    parentEpic = (Epic) findedParentTask;
                }
            }

            // Создание
            // проверить хранит ли Json id, и если да, то нет ли такого в базе,
            // чтобы закачать одну задачу два раза
            if (!isUpdate && taskDto.id != null &&
                    manager.getTaskById(taskDto.id).isPresent()) {
                throw new TaskIdConflictException("Not Acceptable");
            }

            // Обновление
            if (isUpdate && taskDto.id == null) {
                // Для обновления задачи проверяем, чтобы было обязательно передано id
                throw new IllegalArgumentException("For update, id is required");

            } else if (isUpdate && manager.getTaskById(taskDto.id).isEmpty()) {
                // Для обновления задачи ищем id по базе, чтобы оно там было
                throw new NoSuchElementException("Not Found");

            } else if (isUpdate) {
                // у старой версии задачи должен совпадать TaskType с командой пути
                Task oldTask = manager.getTaskById(taskDto.id).get();

                if (taskTypePath != oldTask.getTaskType()) {
                    throw new IllegalArgumentException("Not Acceptable");
                }
            }

            // Подготовка поля Status
            Status taskStatus = taskDto.status != null
                    ? Status.valueOf(taskDto.status)
                    : Status.NEW;


            // Вызываем соответствующий метод TaskManager
            if (isUpdate) {
                return manager.updateTask(
                        taskTypePath,
                        taskDto.id,
                        taskDto.name,
                        taskDto.description,
                        taskStatus,
                        taskDto.startTime,
                        taskDto.duration
                );
            } else {
                return manager.createTask(
                        taskTypePath,
                        taskDto.name,
                        taskDto.description,
                        parentEpic,
                        taskDto.startTime,
                        taskDto.duration
                );
            }

        } catch (JsonSyntaxException | DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid data format: " + e.getMessage(), e);
        }
    }

    private String seekTaskTypeStringFromPath(String PATH) {
        if (PATH.startsWith("/tasks")) {
            return "TASK";
        } else if (PATH.startsWith("/subtasks")) {
            return "SUBTASK";
        } else if (PATH.startsWith("/epics")) {
            return "EPIC";
        } else {
            return "";
        }
    }
}

