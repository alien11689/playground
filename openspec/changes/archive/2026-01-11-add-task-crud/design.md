## Context
- Spring Boot backend with JPA entities
- OpenAPI-generated DTOs for API contract
- PostgreSQL database with existing task table
- Token-based authentication with user context

## Goals / Non-Goals
- Goals: Complete CRUD operations, proper validation, automatic timestamp management
- Non-Goals: Task assignment workflow, status transitions, advanced filtering

## Decisions
- Decision: Use JPA entity separate from DTO for clean separation
- Decision: Implement service layer for business logic
- Decision: Use MapStruct for DTO mapping
- Decision: Controller can use repository directly for read operations
- Decision: No search/filtering functionality in this change
- Alternatives considered: Manual mapping, soft delete, advanced search

## Migration Plan
1. Create Task entity matching database schema
2. Implement repository extending JpaRepository
3. Create service with business logic (write operations)
4. Add MapStruct mapper for DTO conversions
5. Update controller to use service for writes, repository for reads
6. Add comprehensive integration tests

## Open Questions
- None

## Technical Architecture
- Entity: Task (JPA entity)
- Repository: TaskRepository (JpaRepository)
- Service: TaskService (business logic for write operations)
- Mapper: TaskMapper (MapStruct for DTO conversions)
- Controller: TasksController (existing, to be updated)
- DTO: TaskDTO, AddTaskRequestDTO (generated from OpenAPI)

## Testing Strategy
- Unit tests for TaskService with in-memory repository implementation (HashMap-based)
- Integration tests for TasksController endpoints
- Integration tests for repository operations
- Authorization tests for all endpoints
- Test coverage for all CRUD operations and validation scenarios

## Unit Testing Approach
- Minimize mock usage in unit tests
- Repository implementation uses HashMap as in-memory database where possible
- Test repository implements same interface as production repository
- Most business logic tested with real repository behavior
- Mocks only used for external dependencies or complex setup scenarios

## Authorization Testing
- Test that unauthorized users cannot access any task endpoints
- Test that only authenticated users can perform CRUD operations
- Test that token validation works correctly
- Test that missing/invalid tokens return 401 Unauthorized

## Database Schema Alignment
- Task entity matches existing task table schema
- Status enum maps to TaskStatusDTO values
- Timestamp fields use OffsetDateTime
- UUID fields for all identifiers