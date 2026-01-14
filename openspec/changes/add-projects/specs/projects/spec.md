## ADDED Requirements

### Requirement: Project Creation
The system SHALL allow users to create new projects with a name and optional description.

#### Scenario: Successful project creation
- **WHEN** authenticated user provides valid project name and optional description
- **THEN** new project is created with ACTIVE status
- **AND** createdAt and updatedAt timestamps are automatically set
- **AND** project DTO is returned with generated ID

#### Scenario: Project creation validation
- **WHEN** user provides invalid name (empty/null)
- **THEN** validation error is returned
- **AND** project is not created

#### Scenario: Project creation without description
- **WHEN** user provides valid name but no description
- **THEN** project is created with description field as null
- **AND** project is successfully created

### Requirement: Project Retrieval
The system SHALL allow users to retrieve individual projects by ID.

#### Scenario: Get existing project
- **WHEN** user requests valid project ID
- **THEN** complete project DTO is returned
- **AND** all fields are populated

#### Scenario: Get non-existent project
- **WHEN** user requests invalid project ID
- **THEN** 404 NOT FOUND is returned
- **AND** error message indicates project not found

### Requirement: Project Update
The system SHALL allow users to update project name and description.

#### Scenario: Successful project name update
- **WHEN** user provides new project name
- **THEN** project name is updated
- **AND** updatedAt timestamp is automatically set
- **AND** updated project DTO is returned

#### Scenario: Successful project description update
- **WHEN** user provides new project description
- **THEN** project description is updated
- **AND** updatedAt timestamp is automatically set

#### Scenario: Update non-existent project
- **WHEN** user attempts to update invalid project ID
- **THEN** 404 NOT FOUND is returned

#### Scenario: Update archived project name
- **WHEN** user attempts to update archived project name
- **THEN** name is successfully updated
- **AND** project status remains ARCHIVED

### Requirement: Project Listing
The system SHALL provide paginated project listing.

#### Scenario: Get paginated projects
- **WHEN** user requests projects with page and size parameters
- **THEN** paginated response is returned
- **AND** pagination metadata is included
- **AND** default page=0, size=20 when not specified

#### Scenario: Empty project list
- **WHEN** no projects exist
- **THEN** empty content list is returned
- **AND** totalElements=0

### Requirement: Project Archival
The system SHALL allow users to archive active projects with optional unfinished task rejection.

#### Scenario: Archive project without rejecting unfinished tasks
- **WHEN** user calls POST /projects/{id}?action=archive&rejectUnfinishedTasks=false
- **THEN** project status changes to ARCHIVED
- **AND** all task statuses remain unchanged
- **AND** tasks in project cannot be modified

#### Scenario: Archive project and reject unfinished tasks
- **WHEN** user calls POST /projects/{id}?action=archive&rejectUnfinishedTasks=true
- **THEN** project status changes to ARCHIVED
- **AND** tasks with status NEW or PENDING are set to REJECTED
- **AND** tasks with status DONE or REJECTED remain unchanged
- **AND** tasks in project cannot be modified

#### Scenario: Archive already archived project
- **WHEN** user calls POST /projects/{id}?action=archive on project that is already ARCHIVED
- **THEN** operation succeeds with no changes
- **AND** project remains ARCHIVED

#### Scenario: Archive non-existent project
- **WHEN** user calls POST /projects/{nonExistentId}?action=archive
- **THEN** 404 NOT FOUND is returned

### Requirement: Project Restoration
The system SHALL allow users to restore archived projects to active status.

#### Scenario: Restore archived project
- **WHEN** user calls POST /projects/{id}?action=restore on project with ARCHIVED status
- **THEN** project status changes to ACTIVE
- **AND** updatedAt timestamp is automatically set
- **AND** tasks in project can be modified again

#### Scenario: Restore already active project
- **WHEN** user calls POST /projects/{id}?action=restore on project that is already ACTIVE
- **THEN** operation succeeds with no changes
- **AND** project remains ACTIVE

#### Scenario: Restore non-existent project
- **WHEN** user calls POST /projects/{nonExistentId}?action=restore
- **THEN** 404 NOT FOUND is returned

### Requirement: Project Existence Validation
The system SHALL validate that a referenced project exists.

#### Scenario: Validate existing project
- **WHEN** system validates an existing project ID
- **THEN** validation passes
- **AND** no exception is thrown

#### Scenario: Validate non-existent project
- **WHEN** system validates a non-existent project ID
- **THEN** ProjectNotFoundException is thrown
- **AND** operation is not completed

### Requirement: Project Status Validation
The system SHALL validate that a project is in ACTIVE status for modification operations.

#### Scenario: Validate active project
- **WHEN** system validates project with ACTIVE status
- **THEN** validation passes
- **AND** boolean true is returned

#### Scenario: Validate archived project
- **WHEN** system validates project with ARCHIVED status
- **THEN** validation fails
- **AND** boolean false is returned
