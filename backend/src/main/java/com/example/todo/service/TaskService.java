package com.example.todo.service;

import com.example.todo.domain.Task;
import com.example.todo.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public List<Task> findAll(boolean completed) {
        return repository.findByCompleted(completed);
    }

    public List<Task> findAll() {
        return repository.findAll();
    }

    public Task findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task create(Task task) {
        task.setId(null);
        return repository.save(task);
    }

    public Task update(Long id, Task updates) {
        Task existing = findById(id);
        existing.setTitle(updates.getTitle());
        existing.setCompleted(updates.isCompleted());
        return repository.save(existing);
    }

    public void delete(Long id) {
        Task existing = findById(id);
        repository.delete(existing);
    }
}
