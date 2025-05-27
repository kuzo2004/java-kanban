package ru.yandex.practicum.entity;

import com.google.gson.annotations.Expose;

import java.time.Duration;
import java.time.LocalDateTime;

/** Вынесен в отдельный класс, потому что Gson не видит иначе*/


public class TaskDto {
    @Expose
    public TaskType taskType;
    @Expose
    public Integer id;
    @Expose
    public String name;
    @Expose
    public String description;
    @Expose
    public String status;
    @Expose
    public LocalDateTime startTime;
    @Expose
    public Duration duration;
    @Expose
    public Integer parentEpicId;

    public TaskDto(Integer id, String name, String description, String status, LocalDateTime startTime,
                   Duration duration, Integer parentEpicId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.startTime = startTime;
        this.duration = duration;
        this.parentEpicId = parentEpicId;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Integer getParentEpicId() {
        return parentEpicId;
    }

    public void setParentEpicId(Integer parentEpicId) {
        this.parentEpicId = parentEpicId;
    }
}
