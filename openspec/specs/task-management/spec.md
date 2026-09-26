# Task Management Specification

## Purpose

Defines how the todo system manages the full lifecycle of a task - creating it, listing and filtering tasks by completion state, retrieving, updating, and deleting it - and the form behavior a user sees while performing those actions.

## Requirements

### Requirement: Task creation

The system SHALL allow a user to create a task by supplying a title, and SHALL reject a task whose title is empty or contains only whitespace. When a title is rejected, the system SHALL present a visible error to the user and SHALL NOT create the task.

#### Scenario: Creating a task with a valid title

- **WHEN** a user submits a non-blank title
- **THEN** the system creates the task with that title and a completed state of false
- **THEN** the new task appears in the task list

#### Scenario: Rejecting a blank title

- **WHEN** a user submits an empty or whitespace-only title
- **THEN** the system presents a visible error identifying the title as the problem
- **THEN** no task is created

### Requirement: Task listing and completion filtering

The system SHALL list every task it holds, and SHALL allow the caller to restrict the list to tasks that are either complete or incomplete.

#### Scenario: Listing all tasks

- **WHEN** a caller requests the task list without a completion filter
- **THEN** the system returns every task it holds

#### Scenario: Filtering to incomplete tasks

- **WHEN** a caller requests the task list filtered to incomplete tasks
- **THEN** the system returns only tasks whose completed state is false

#### Scenario: Filtering to complete tasks

- **WHEN** a caller requests the task list filtered to complete tasks
- **THEN** the system returns only tasks whose completed state is true

### Requirement: Task retrieval

The system SHALL return a single task identified by its identifier, and SHALL respond with a not-found error when no task holds that identifier.

#### Scenario: Retrieving an existing task

- **WHEN** a caller requests a task by an identifier that exists
- **THEN** the system returns that task

#### Scenario: Requesting a task that does not exist

- **WHEN** a caller requests a task by an identifier that no task holds
- **THEN** the system responds with a not-found error and no task body

### Requirement: Task update

The system SHALL allow a caller to change a task's title and its completed state, and SHALL respond with a not-found error when no task holds the requested identifier.

#### Scenario: Updating an existing task

- **WHEN** a caller submits a new title or a new completed state for a task that exists
- **THEN** the system persists the submitted values for that task
- **THEN** the system returns the updated task

#### Scenario: Marking a task complete or incomplete

- **WHEN** a caller toggles the completed state of a task
- **THEN** the system persists the opposite completed state and returns the updated task

#### Scenario: Updating a task that does not exist

- **WHEN** a caller submits an update for an identifier that no task holds
- **THEN** the system responds with a not-found error and changes nothing

### Requirement: Task deletion

The system SHALL remove a task when a caller deletes it, and SHALL respond with a not-found error when no task holds the requested identifier.

#### Scenario: Deleting an existing task

- **WHEN** a caller deletes a task that exists
- **THEN** the system removes that task
- **THEN** the task no longer appears in the task list

#### Scenario: Deleting a task that does not exist

- **WHEN** a caller deletes an identifier that no task holds
- **THEN** the system responds with a not-found error and deletes nothing

### Requirement: Editing prefill

When a user begins editing an existing task, the system SHALL pre-fill the task form with that task's current title, and SHALL discard the pre-fill when the user stops editing.

#### Scenario: Beginning to edit a task

- **WHEN** a user selects a task for editing
- **THEN** the task form shows that task's current title

#### Scenario: Stopping an edit without saving

- **WHEN** a user stops editing a task without saving
- **THEN** the task form no longer shows that task's title
