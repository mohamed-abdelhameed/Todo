package com.example.todo.service;

public class TaskNotFoundException extends RuntimeException {

    private final Long id;

    public TaskNotFoundException(Long id) {
        super("Task not found: " + id);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
