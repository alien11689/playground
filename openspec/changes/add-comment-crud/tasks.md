## 1. Infrastructure Setup
- [ ] 1.1 Create Comment entity class
- [ ] 1.2 Create CommentRepository interface
- [ ] 1.3 Create CommentService class
- [ ] 1.4 Add CommentMapper with MapStruct

## 2. CRUD Operations Implementation
- [ ] 2.1 Implement addTaskComment() in TasksController
- [ ] 2.2 Implement getTaskComments() in TasksController
- [ ] 2.3 Implement updateTaskComment() in TasksController
- [ ] 2.4 Implement deleteTaskComment() in TasksController

## 3. Business Logic
- [ ] 3.1 Authorization: only comment author can edit/delete
- [ ] 3.2 Validation: comment content required, min 1 char
- [ ] 3.3 Closed task check: prevent comments on DONE/REJECTED tasks
- [ ] 3.4 Sorting: newest comments first (createdAt DESC)

## 4. Testing
- [ ] 4.1 Create in-memory CommentRepository for unit tests
- [ ] 4.2 Write unit tests for CommentService
- [ ] 4.3 Write end-to-end tests for comment endpoints
- [ ] 4.4 Write tests for comment retrieval pagination
