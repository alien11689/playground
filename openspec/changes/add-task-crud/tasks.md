## 1. Infrastructure Setup
- [x] 1.1 Create Task entity class
- [x] 1.2 Create TaskRepository interface
- [x] 1.3 Create TaskService class
- [x] 1.4 Add TaskMapper with MapStruct for DTO conversions

## 2. CRUD Operations Implementation
- [x] 2.1 Implement createTask() in TasksController
- [x] 2.2 Implement getTask() in TasksController
- [x] 2.3 Implement updateTask() in TasksController
- [x] 2.4 Implement getTasks() with pagination

## 3. Business Logic
- [x] 3.1 Add task validation rules
- [x] 3.2 Implement automatic timestamp setting
- [x] 3.3 Add user context handling
- [x] 3.4 Set default status to NEW for new tasks
- [x] 3.5 Refactor TaskRepository.findById() to return Optional<Task>
- [x] 3.6 Remove unused existsById() method from TaskRepository
- [x] 3.7 Update test configuration to use ddl-auto: validate instead of create-drop

## 4. Testing
- [x] 4.1 Create in-memory TaskRepository implementation for unit tests (HashMap-based)
- [x] 4.2 Write unit tests for TaskService with in-memory repository
- [x] 4.3 Write end-to-end tests for TasksController endpoints

