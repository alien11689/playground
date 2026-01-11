# Change: add-comment-crud

## Why
Task comments API endpoints are defined in OpenAPI and database table exists, but all methods throw "Not implemented". Users need ability to comment on tasks to facilitate collaboration and discussion.

## What Changes
- Implement CRUD for task comments (create, read, update, delete)
- Add Comment entity, CommentRepository, CommentService classes
- Add CommentMapper with MapStruct
- Implement authorization: only comment author can edit/delete own comments
- Add validation: comment content required (min 1 char)
- Comments returned in creation order (newest first)
- Closed task check: prevent adding comments to completed tasks
- Controller uses repository for read, service for writes

## Impact
- Affected specs: comments (new capability)
- Affected code: TasksController.java (implement 4 methods), new Comment infrastructure
- Breaking: No (new feature, not changing existing behavior)
