## ADDED Requirements

### Requirement: Task Comments
The system SHALL support comments associated with tasks for discussion and collaboration.

#### Scenario: Comments on open task
- **WHEN** task is in NEW or PENDING status
- **THEN** users can add comments
- **AND** comments are returned with task view

#### Scenario: Comments on closed task
- **WHEN** task status is DONE or REJECTED
- **THEN** users can view existing comments
- **AND** users cannot add new comments
