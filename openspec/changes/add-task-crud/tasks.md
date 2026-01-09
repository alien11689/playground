## 1. Infrastructure Setup
- [ ] 1.1 Create Task entity class
- [ ] 1.2 Create TaskRepository interface
- [ ] 1.3 Create TaskService class
- [ ] 1.4 Add TaskMapper with MapStruct for DTO conversions

## 2. CRUD Operations Implementation
- [ ] 2.1 Implement createTask() in TasksController
- [ ] 2.2 Implement getTask() in TasksController
- [ ] 2.3 Implement updateTask() in TasksController
- [ ] 2.4 Implement deleteTask() in TasksController
- [ ] 2.5 Implement getTasks() with pagination

## 3. Business Logic
- [ ] 3.1 Add task validation rules
- [ ] 3.2 Implement automatic timestamp setting
- [ ] 3.3 Add user context handling
- [ ] 3.4 Set default status to NEW for new tasks

## 4. Testing
- [ ] 4.1 Create in-memory TaskRepository implementation for unit tests (HashMap-based)
- [ ] 4.2 Write unit tests for TaskService with in-memory repository
- [ ] 4.3 Write integration tests for TasksController endpoints
- [ ] 4.4 Write integration tests for repository operations
- [ ] 4.5 Test validation scenarios
- [ ] 4.6 Test pagination functionality
- [ ] 4.7 Test authorization - unauthorized users cannot access any endpoints
- [ ] 4.8 Test authorization - invalid tokens return 401 Unauthorized
- [ ] 4.9 Test authorization - missing tokens return 401 Unauthorized