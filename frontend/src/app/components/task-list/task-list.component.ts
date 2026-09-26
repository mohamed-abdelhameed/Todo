import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TaskService } from '../../services/task.service';
import { Task } from '../../models/task.model';
import { TaskFilter } from '../../shared/task-filter';
import { TaskFormComponent } from '../task-form/task-form.component';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [CommonModule, TaskFormComponent],
  templateUrl: './task-list.component.html',
  styleUrls: ['./task-list.component.css'],
})
export class TaskListComponent implements OnInit {
  tasks: Task[] = [];
  filter: TaskFilter = 'all';
  editingTask: Task | null = null;

  constructor(private taskService: TaskService) {}

  ngOnInit(): void {
    this.loadTasks();
  }

  loadTasks(): void {
    const completed =
      this.filter === 'completed' ? true : this.filter === 'active' ? false : undefined;
    this.taskService.getTasks(completed).subscribe((tasks) => {
      this.tasks = tasks;
    });
  }

  setFilter(filter: TaskFilter): void {
    this.filter = filter;
    this.loadTasks();
  }

  toggleComplete(task: Task): void {
    this.taskService
      .updateTask(task.id, { ...task, completed: !task.completed })
      .subscribe(() => this.loadTasks());
  }

  startEdit(task: Task): void {
    this.editingTask = task;
  }

  handleSaved(): void {
    this.editingTask = null;
    this.loadTasks();
  }

  handleCancelEdit(): void {
    this.editingTask = null;
  }

  deleteTask(id: number): void {
    this.taskService.deleteTask(id).subscribe(() => this.loadTasks());
  }
}
