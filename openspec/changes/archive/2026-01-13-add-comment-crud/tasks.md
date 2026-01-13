## 1. Infrastructure Setup
- [x] 1.1 Create Comment entity class
- [x] 1.2 Create CommentRepository interface
- [x] 1.3 Create CommentService class
- [x] 1.4 Add CommentMapper with MapStruct

## 2. CRUD Operations Implementation
- [x] 2.1 Implement addTaskComment() in TasksController
- [x] 2.2 Implement getTaskComments() in TasksController
- [x] 2.3 Implement updateTaskComment() in TasksController
- [x] 2.4 Implement deleteTaskComment() in TasksController

## 3. Business Logic
- [x] 3.1 Authorization: only comment author can edit/delete
- [x] 3.2 Validation: comment content required, min 1 char
- [x] 3.3 Closed task check: prevent comments on DONE/REJECTED tasks
- [x] 3.4 Sorting: newest comments first (createdAt DESC)

## 4. Testing
- [x] 4.1 Create in-memory CommentRepository for unit tests
- [x] 4.2 Write unit tests for CommentService
- [x] 4.3 Write end-to-end tests for comment endpoints
- [x] 4.4 Write tests for comment retrieval pagination
