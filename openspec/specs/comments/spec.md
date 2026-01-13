# comments Specification

## Purpose
TBD - created by archiving change add-comment-crud. Update Purpose after archive.
## Requirements
### Requirement: Comment Creation
The system SHALL allow authenticated users to add comments to tasks.

#### Scenario: Successful comment creation
- **WHEN** authenticated user provides valid comment content
- **THEN** new comment is created
- **AND** createdAt and addedBy are automatically set
- **AND** comment DTO is returned with generated ID

#### Scenario: Comment to closed task
- **WHEN** user attempts to comment on a DONE or REJECTED task
- **THEN** 400 BAD REQUEST is returned
- **AND** comment is not created

#### Scenario: Comment content validation
- **WHEN** user provides empty or whitespace-only comment
- **THEN** 400 BAD REQUEST is returned
- **AND** comment is not created

#### Scenario: Unauthorized comment creation
- **WHEN** unauthenticated user attempts to comment
- **THEN** 401 UNAUTHORIZED is returned

### Requirement: Comment Retrieval
The system SHALL allow users to retrieve comments for a task.

#### Scenario: Get comments list
- **WHEN** user requests comments for a valid task
- **THEN** paginated list of comments is returned
- **AND** comments are sorted by createdAt DESC (newest first)
- **AND** pagination metadata is included

#### Scenario: Empty comments list
- **WHEN** task has no comments
- **THEN** empty content list is returned
- **AND** totalElements=0

### Requirement: Comment Update
The system SHALL allow users to update their own comments.

#### Scenario: Successful comment update
- **WHEN** comment author updates comment content
- **THEN** comment is updated with new content
- **AND** updatedAt is automatically set
- **AND** 204 NO CONTENT is returned

#### Scenario: Update other user's comment
- **WHEN** user attempts to update comment created by someone else
- **THEN** 403 FORBIDDEN is returned
- **AND** comment is not updated

#### Scenario: Invalid comment update
- **WHEN** user provides empty or whitespace-only content
- **THEN** 400 BAD REQUEST is returned
- **AND** comment is not updated

### Requirement: Comment Deletion
The system SHALL allow users to delete their own comments.

#### Scenario: Successful comment deletion
- **WHEN** comment author deletes their comment
- **THEN** comment is removed from database
- **AND** 204 NO CONTENT is returned

#### Scenario: Delete other user's comment
- **WHEN** user attempts to delete comment created by someone else
- **THEN** 403 FORBIDDEN is returned
- **AND** comment is not deleted

