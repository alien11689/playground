# Change: add-task-crud

## Why
TasksController has all endpoints defined in the API, but all methods throw "Not implemented". The task management system requires full CRUD backend implementation so users can create, read, update, and delete tasks.

## What Changes
- Implement full CRUD for tasks (create, read, update, delete)
- Add service layer and repository for tasks
- Input validation and DTO mapping with MapStruct
- Automatic timestamp and user setting (created/updated)
- Controller uses repository directly for read operations, service for writes
- **BREAKING**: Change default task status to NEW

## Impact
- Affected specs: tasks (new capability)
- Affected code: TasksController.java, new Task, TaskService, TaskRepository, TaskMapper classes
- Database: task table already exists (V0003 migration)