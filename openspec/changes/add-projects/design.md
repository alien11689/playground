## Context
Tasks currently exist independently without organization. Users need project management to group related tasks and control project lifecycle. Cross-cutting change affecting Project entity, Task entity, and Comment entity modifications.

## Goals / Non-Goals
- Goals:
  - Create Project entity with name, description, and status (ACTIVE/ARCHIVED)
  - Add project-to-task relationship
  - Implement project CRUD operations
  - Implement project archival and restoration
  - Enforce modification restrictions on archived projects
  - Add optional unfinished task rejection on archive
- Non-Goals:
  - Project-specific permissions/ownership (all users can manage all projects)
  - Task deletion or archiving
  - Project-level analytics/reporting

## Decisions

### Database Schema
- Decision: Create new `project` table with columns: id (UUID), name (TEXT NOT NULL), description (TEXT), status (VARCHAR NOT NULL DEFAULT 'ACTIVE'), created_at (TIMESTAMP), updated_at (TIMESTAMP)
- Alternatives considered:
  - JSONB for project metadata → rejected: schema changes need explicit migrations
  - Separate status table → rejected: over-engineering for simple enum
- Rationale: Simple relational design fits current patterns, maintains data integrity

### Task-Project Relationship
- Decision: Add `project_id` column to `task` table as nullable initially, then NOT NULL after data migration
- Alternatives considered:
  - Separate junction table → rejected: unnecessary for many-to-one
  - Keep tasks project-agnostic → rejected: defeats purpose of projects
- Rationale: Direct FK relationship simplifies queries and maintains referential integrity

### Query Parameter for Actions
- Decision: Use `action=archive|restore` query parameter on PUT /projects/{id} endpoint
- Alternatives considered:
  - Separate endpoints PATCH /projects/{id}/archive, PATCH /projects/{id}/restore → rejected: inconsistent with existing patterns
  - PUT body with action field → rejected: action is not project data
- Rationale: Follows RESTful convention for resource state transitions, similar to existing patterns

### Project Status Validation Location
- Decision: Implement shared `isProjectActive(projectId)` method in ProjectService
- Alternatives considered:
  - Validate in each service independently → rejected: code duplication
  - Validate at controller level → rejected: validation belongs in business logic
  - Use Spring Security method security → rejected: authorization, not validation
- Rationale: Single source of truth for project status, easy to test, reusable across TaskService and CommentService

### Error Handling
- Decision: Create specific exceptions: `ProjectArchivedException` and `ProjectNotFoundException`
- Alternatives considered:
  - Generic `RuntimeException` → rejected: no specificity for clients
  - Use HTTP status codes only → rejected: clients need actionable error messages
- Rationale: Specific exceptions allow proper error response mapping and clear client handling

### Unfinished Task Rejection
- Decision: Optional `rejectUnfinishedTasks` boolean parameter on archive action
- Behavior when true:
  - Find all tasks in project with status NOT IN (DONE, REJECTED)
  - Update status to REJECTED
- Behavior when false:
  - Leave all task statuses unchanged
- Rationale: Gives users control over task handling during archive, flexibility for different workflows

## Data Model Changes

### New Table: project
```sql
CREATE TABLE project (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    description TEXT,
    status VARCHAR NOT NULL DEFAULT 'ACTIVE', -- ACTIVE or ARCHIVED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### Modified Table: task
```sql
ALTER TABLE task ADD COLUMN project_id UUID;
ALTER TABLE task ADD CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES project(id);
-- Data migration: associate existing tasks with a default project or require manual assignment
ALTER TABLE task ALTER COLUMN project_id SET NOT NULL;
```

## API Design

### Project Endpoints
- `POST /projects` - Create project (status defaults to ACTIVE)
- `GET /projects` - List projects (pagination)
- `GET /projects/{projectId}` - Get project details
- `PUT /projects/{projectId}` - Update name and description
- `PUT /projects/{projectId}?action=archive&rejectUnfinishedTasks={boolean}` - Archive project
- `PUT /projects/{projectId}?action=restore` - Restore project

### Task Endpoints (Modified)
- `POST /tasks` - Create task with required `projectId` in body
- `PUT /tasks/{taskId}` - Update task (validated: project must be ACTIVE)
- `GET /tasks?projectId={optional}` - List tasks with optional project filter

### Comment Endpoints (Modified)
- `PUT /tasks/{taskId}/comments/{commentId}` - Update comment (validated: project must be ACTIVE)

## Validation Flow

### Task Creation
1. Request reaches TasksController
2. TasksController calls TaskService.createTask()
3. TaskService extracts projectId from DTO
4. TaskService calls ProjectService.isProjectActive(projectId)
5. If false → throws ProjectArchivedException
6. If true → creates task with projectId

### Task Update
1. Request reaches TasksController
2. TasksController calls TaskService.updateTask()
3. TaskService loads existing task
4. TaskService calls ProjectService.isProjectActive(task.getProjectId())
5. If false → throws ProjectArchivedException
6. If true → updates task

### Comment Update
1. Request reaches TasksController
2. TasksController calls CommentService.updateComment()
3. CommentService loads comment, then task
4. CommentService calls ProjectService.isProjectActive(task.getProjectId())
5. If false → throws ProjectArchivedException
6. If true → updates comment

## Risks / Trade-offs

### Risk: Existing tasks without projects
- Impact: Null constraint violation after migration
- Mitigation: Create migration to either assign existing tasks to a default project or require manual assignment before setting NOT NULL constraint
- Timeline: Address before production deployment

### Risk: Performance impact of project validation
- Impact: Additional DB query on each task/comment mutation
- Mitigation: Database indexes on project status; consider caching if needed
- Trade-off: Acceptable for current scale, optimize if data shows bottleneck

### Risk: Breaking changes to task API
- Impact: Existing clients calling POST /tasks without projectId will fail
- Mitigation: Clear migration guide, versioned API if backward compatibility needed
- Trade-off: Breaking change justified for core feature addition

### Risk: Archive action complexity
- Impact: Optional rejectUnfinishedTasks adds logic complexity
- Mitigation: Thorough unit and integration tests for both branches
- Trade-off: Additional complexity provides valuable workflow flexibility

## Migration Plan

### Phase 1: Database Schema
1. Add project table migration
2. Add project_id column to task table (nullable)
3. Add foreign key constraint

### Phase 2: Code Changes
1. Implement Project infrastructure
2. Modify Task entity and related code
3. Implement validation in TaskService and CommentService
4. Update OpenAPI spec and regenerate code

### Phase 3: Data Migration
1. Migrate existing tasks to projects (default project or manual assignment)
2. Set project_id NOT NULL constraint
3. Run smoke tests

### Phase 4: Testing and Validation
1. Run integration tests
2. Manual testing of project lifecycle
3. Load testing for validation performance
4. Production deployment with monitoring

### Rollback Plan
- Remove project validation from TaskService and CommentService
- Revert OpenAPI spec changes
- Drop project_id column if data not critical
- Restore previous task API endpoints

## Open Questions
- None identified at this time
