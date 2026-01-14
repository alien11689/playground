# Change: add-projects

## Why
Currently tasks exist independently without grouping or organization. Users need project management capability to group related tasks, control project lifecycle (active/archived), and enforce restrictions on modifications when a project is archived.

## What Changes
- Add new Project entity with id, name, description (optional), status (ACTIVE/ARCHIVED)
- Add projectId field to Task entity (required, references Project.id)
- Implement project CRUD endpoints: POST /projects, GET /projects, GET /projects/{id}, PUT /projects/{id}
- Implement project status management via POST /projects/{id}?action=archive|restore
- Add rejectUnfinishedTasks parameter for archive action (sets unfinished tasks to REJECTED)
- Validate project status before creating/updating tasks
- Validate project status before updating task comments
- Add shared validation method in ProjectService.isProjectActive(projectId)
- Update task listing endpoint: GET /tasks?projectId={optional} for filtering
- Add database migrations for Project table and Task.project_id column

## Impact
- Affected specs: projects (new), tasks (modified)
- Affected code: New Project infrastructure (entity, repository, service, mapper, controller), TaskService modifications, CommentService modifications, OpenAPI spec updates
- Breaking: No (new feature, taskId references remain compatible)
