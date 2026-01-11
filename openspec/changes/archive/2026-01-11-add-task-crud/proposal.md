# Change: add-task-crud

## Why
TasksController has all endpoints defined in the API, but all methods throw "Not implemented". The task management system requires CRUD backend implementation so users can create, read, and update tasks.

## What Changes
- Implement CRUD for tasks (create, read, update)
- Add service layer and repository for tasks
- Input validation and DTO mapping with MapStruct
- Automatic timestamp and user setting (created/updated)
- Controller uses repository directly for read operations, service for writes
- **BREAKING**: Change default task status to NEW
- Unit tests for TaskService with in-memory repository implementation
- End-to-end tests for TasksController endpoints
- Refactor TaskRepository.findById() to return Optional<Task> instead of nullable Task
- Remove unused existsById() method from TaskRepository
- Change test configuration from ddl-auto: create-drop to validate

## Impact
- Affected specs: tasks (new capability)
- Affected code: TasksController.java, new Task, TaskService, TaskRepository, TaskMapper classes
- Database: task table already exists (V0003 migration)