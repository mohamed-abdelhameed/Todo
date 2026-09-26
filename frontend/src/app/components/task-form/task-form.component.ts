import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TaskService } from '../../services/task.service';
import { Task } from '../../models/task.model';

@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './task-form.component.html',
  styleUrls: ['./task-form.component.css'],
})
export class TaskFormComponent implements OnInit {
  @Input() editingTask: Task | null = null;
  @Output() saved = new EventEmitter<void>();
  @Output() cancelEdit = new EventEmitter<void>();

  title = '';

  constructor(private taskService: TaskService) {}

  ngOnInit(): void {
    if (this.editingTask) {
      this.title = this.editingTask.title;
    }
  }

  submit(): void {
    const trimmed = this.title.trim();
    if (!trimmed) {
      return;
    }

    if (this.editingTask) {
      this.taskService
        .updateTask(this.editingTask.id, { ...this.editingTask, title: trimmed })
        .subscribe(() => this.saved.emit());
    } else {
      this.taskService
        .createTask({ title: trimmed, completed: false })
        .subscribe(() => {
          this.title = '';
          this.saved.emit();
        });
    }
  }

  cancel(): void {
    this.title = '';
    this.cancelEdit.emit();
  }
}
