## 1. Infrastructure Setup
- [ ] 1.1 Create Project entity class with id, name, description, status fields
- [ ] 1.2 Create ProjectRepository interface extending JpaRepository
- [ ] 1.3 Create ProjectService class with all CRUD methods
- [ ] 1.4 Add ProjectMapper with MapStruct for DTO conversions
- [ ] 1.5 Create ProjectArchivedException class
- [ ] 1.6 Create ProjectNotFoundException class
- [ ] 1.7 Ensure ProjectArchivedException and ProjectNotFoundException extend RuntimeException
- [ ] 1.8 Configure Project entity to generate UUID in code (no DB default for id)
- [ ] 1.9 Configure Project entity to set status, createdAt, updatedAt in application layer

## 2. Database Migrations
- [ ] 2.1 Create Flyway migration for Project table
- [ ] 2.2 Create Flyway migration to add project_id column to Task table
- [ ] 2.3 Add foreign key constraint from Task.project_id to Project.id
- [ ] 2.4 Create migration for project_id NOT NULL constraint (after data migration if needed)
- [ ] 2.5 Add UNIQUE index on project.name and index on project.status in same migration as project table creation

## 3. Projects Controller Implementation
- [ ] 3.1 Implement createProject() with validation (name required, status set to ACTIVE)
- [ ] 3.2 Implement getProject() with ProjectNotFoundException handling
- [ ] 3.3 Implement updateProject() for name and description
- [ ] 3.4 Implement listProjects() with pagination
- [ ] 3.5 Implement archiveProject() endpoint: POST /projects/{id}?action=archive&rejectUnfinishedTasks={boolean}
- [ ] 3.6 Implement restoreProject() endpoint: POST /projects/{id}?action=restore
- [ ] 3.7 Implement isProjectActive() shared validation method in ProjectService

## 4. Task Entity Modifications
- [ ] 4.1 Add projectId field to Task entity with @ManyToOne relationship
- [ ] 4.2 Update TaskDTO to include projectId field (required)
- [ ] 4.3 Update TaskMapper for new field
- [ ] 4.4 Update TaskRepository if needed

## 5. TaskService Modifications
- [ ] 5.1 Modify createTask() to validate project existence via isProjectActive()
- [ ] 5.2 Modify updateTask() to validate project status via isProjectActive()
- [ ] 5.3 Handle ProjectArchivedException in service methods
- [ ] 5.4 Update getTasks() to support optional projectId filter parameter

## 6. CommentService Modifications
- [ ] 6.1 Modify updateComment() to validate project status via isProjectActive()
- [ ] 6.2 Handle ProjectArchivedException in updateComment()
- [ ] 6.3 Ensure createComment() validation for closed tasks still works

## 7. OpenAPI Specification Updates
- [ ] 7.1 Add Project schema definition with fields
- [ ] 7.2 Add Project endpoints (POST, GET, PUT) with action query parameters
- [ ] 7.3 Update Task schema to include projectId field
- [ ] 7.4 Update Task endpoints for projectId validation and filtering
- [ ] 7.5 Add error schemas for ProjectArchivedException and ProjectNotFoundException
- [ ] 7.6 Regenerate API code using openapi-generator-maven-plugin

## 8. TasksController Updates
- [ ] 8.1 Update createTask() to accept and validate projectId
- [ ] 8.2 Update getTasks() to handle optional projectId query parameter
- [ ] 8.3 Ensure proper error responses for project validation failures

## 9. Testing
- [ ] 9.1 Create unit tests for ProjectService with mocked repository
- [ ] 9.2 Create unit tests for isProjectActive() method
- [ ] 9.3 Create unit tests for TaskService project validation
- [ ] 9.4 Create unit tests for CommentService project validation
- [ ] 9.5 Create integration tests for ProjectsController endpoints
- [ ] 9.6 Create integration tests for task creation/update with project validation
- [ ] 9.7 Create integration tests for comment update with project validation
- [ ] 9.8 Test archive action with rejectUnfinishedTasks=true/false
- [ ] 9.9 Test restore action from archived to active

## 10. Validation Edge Cases
- [ ] 10.1 Verify task cannot be added to archived project
- [ ] 10.2 Verify task cannot be updated when project is archived
- [ ] 10.3 Verify comment cannot be updated when project is archived
- [ ] 10.4 Verify archive works regardless of task statuses
- [ ] 10.5 Verify archive with rejectUnfinishedTasks marks appropriate tasks as REJECTED
- [ ] 10.6 Verify restore allows task/comment modifications again
- [ ] 10.7 Verify non-existent projectId returns appropriate error
