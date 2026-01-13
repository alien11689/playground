## MODIFIED Requirements

### Requirement: Task Creation
The system SHALL allow authenticated users to create new tasks with summary, optional description, and required project ID. The referenced project must exist and be in ACTIVE status.

#### Scenario: Successful task creation with active project
- **WHEN** authenticated user provides valid task data with valid projectId of ACTIVE project
- **THEN** new task is created with NEW status
- **AND** createdAt and createdBy are automatically set
- **AND** task is associated with the specified project
- **AND** task DTO is returned with generated ID

#### Scenario: Task creation with archived project
- **WHEN** authenticated user provides valid task data with projectId of ARCHIVED project
- **THEN** task creation is rejected
- **AND** error indicates project is archived and modifications are not allowed

#### Scenario: Task creation with non-existent project
- **WHEN** authenticated user provides valid task data with non-existent projectId
- **THEN** task creation is rejected
- **AND** error indicates project not found

#### Scenario: Task creation without projectId
- **WHEN** authenticated user provides valid task data without projectId
- **THEN** validation error is returned
- **AND** task is not created

#### Scenario: Task creation validation
- **WHEN** user provides invalid summary (empty/null) or invalid projectId
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
- **AND** all fields are populated including projectId

#### Scenario: Get non-existent task
- **WHEN** user requests invalid task ID
- **THEN** 404 NOT FOUND is returned

#### Scenario: Unauthorized task access
- **WHEN** unauthenticated user requests task
- **THEN** 401 UNAUTHORIZED is returned

### Requirement: Task Update
The system SHALL allow users to update existing tasks. The task's project must be in ACTIVE status for the update to proceed.

#### Scenario: Successful task update with active project
- **WHEN** user provides valid task update data and task's project is ACTIVE
- **THEN** task is updated with new values
- **AND** updatedAt and updatedBy are automatically set
- **AND** updated task DTO is returned

#### Scenario: Task update with archived project
- **WHEN** user attempts to update task that belongs to ARCHIVED project
- **THEN** task update is rejected
- **AND** error indicates project is archived and modifications are not allowed
- **AND** task remains unchanged

#### Scenario: Update non-existent task
- **WHEN** user attempts to update invalid task ID
- **THEN** 404 NOT FOUND is returned

#### Scenario: Unauthorized task update
- **WHEN** unauthenticated user attempts to update task
- **THEN** 401 UNAUTHORIZED is returned
- **AND** task is not updated

### Requirement: Task Listing
The system SHALL provide paginated task listing with optional project filtering.

#### Scenario: Get all paginated tasks
- **WHEN** user requests tasks without projectId filter
- **THEN** paginated response is returned with all tasks
- **AND** pagination metadata is included

#### Scenario: Get paginated tasks with project filter
- **WHEN** user requests tasks with specific projectId
- **THEN** paginated response is returned with tasks only from that project
- **AND** pagination metadata is included

#### Scenario: Get tasks with invalid project filter
- **WHEN** user requests tasks with non-existent projectId
- **THEN** empty content list is returned
- **AND** totalElements=0

#### Scenario: Empty task list
- **WHEN** no tasks exist or no tasks match filter criteria
- **THEN** empty content list is returned
- **AND** totalElements=0

#### Scenario: Unauthorized task listing
- **WHEN** unauthenticated user requests tasks list
- **THEN** 401 UNAUTHORIZED is returned
- **AND** no tasks are returned
