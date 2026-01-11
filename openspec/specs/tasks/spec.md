## Purpose
This capability provides task management functionality, including creating, reading, updating, and listing tasks with proper authentication, validation, and pagination.

## Requirements

### Requirement: Task Creation
The system SHALL allow authenticated users to create new tasks with summary and optional description.

#### Scenario: Successful task creation
- **WHEN** authenticated user provides valid task data
- **THEN** new task is created with NEW status
- **AND** createdAt and createdBy are automatically set
- **AND** task DTO is returned with generated ID

#### Scenario: Task creation validation
- **WHEN** user provides invalid summary (empty/null)
- **THEN** validation error is returned
- **AND** task is not created

#### Scenario: Unauthorized task creation
- **WHEN** unauthenticated user attempts to create task
- **THEN** 401 UNAUTHORIZED is returned
- **AND** task is not created

### Requirement: Task Retrieval
The system SHALL allow users to retrieve individual tasks by ID.

#### Scenario: Get existing task
- **WHEN** user requests valid task ID
- **THEN** complete task DTO is returned
- **AND** all fields are populated

#### Scenario: Get non-existent task
- **WHEN** user requests invalid task ID
- **THEN** 404 NOT FOUND is returned

#### Scenario: Unauthorized task access
- **WHEN** unauthenticated user requests task
- **THEN** 401 UNAUTHORIZED is returned

### Requirement: Task Update
The system SHALL allow users to update existing tasks.

#### Scenario: Successful task update
- **WHEN** user provides valid task update data
- **THEN** task is updated with new values
- **AND** updatedAt and updatedBy are automatically set
- **AND** updated task DTO is returned

#### Scenario: Update non-existent task
- **WHEN** user attempts to update invalid task ID
- **THEN** 404 NOT FOUND is returned

#### Scenario: Unauthorized task update
- **WHEN** unauthenticated user attempts to update task
- **THEN** 401 UNAUTHORIZED is returned
- **AND** task is not updated

### Requirement: Task Listing
The system SHALL provide paginated task listing.

#### Scenario: Get paginated tasks
- **WHEN** user requests tasks with page and size parameters
- **THEN** paginated response is returned
- **AND** pagination metadata is included
- **AND** default page=0, size=20 when not specified

#### Scenario: Empty task list
- **WHEN** no tasks exist
- **THEN** empty content list is returned
- **AND** totalElements=0

#### Scenario: Unauthorized task listing
- **WHEN** unauthenticated user requests tasks list
- **THEN** 401 UNAUTHORIZED is returned
- **AND** no tasks are returned
