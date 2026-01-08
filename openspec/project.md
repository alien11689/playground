# Project Context

## Purpose
A full-stack task management application with user authentication, task CRUD operations, and comment functionality. The project demonstrates a modern Spring Boot backend with React frontend integration.

## Tech Stack
- **Backend**: Spring Boot 3.5.7, Java 25, Spring Security, Spring Data JPA
- **Database**: PostgreSQL 18 with Flyway migrations
- **Frontend**: React 19.2.3, TypeScript, Vite 7.3.1
- **UI Framework**: React Bootstrap 2.10.10, Bootstrap 5.3.8
- **API Documentation**: OpenAPI 3.1.1 with SpringDoc
- **Build Tools**: Maven (backend), npm/Vite (frontend)
- **Testing**: JUnit 5, TestContainers (backend), ESLint (frontend)

## Project Conventions

### Code Style
- **TypeScript**: Strict type checking with stylistic rules enabled
- **ESLint**: Uses recommended, strict, and stylistic TypeScript configs
- **React**: Functional components with hooks, React-specific linting rules
- **Java**: Spring Boot conventions, package structure follows domain organization
- **Naming**: CamelCase for variables, PascalCase for components/types

### Architecture Patterns
- **Backend**: Layered architecture with controllers, services, repositories
- **Frontend**: Component-based architecture with React hooks
- **API**: RESTful endpoints following OpenAPI specification
- **Database**: JPA entities with Flyway version-controlled migrations
- **Security**: Token-based authentication with Spring Security

### Testing Strategy
- **Backend**: Unit tests with JUnit 5, integration tests with TestContainers
- **Frontend**: ESLint for code quality, no specific test framework currently configured
- **API**: OpenAPI specification serves as contract testing foundation

### Git Workflow
- Standard Git workflow with main branch
- Maven build process includes frontend compilation and bundling
- Frontend built and embedded in Spring Boot static resources

## Domain Context
Task management system with the following core entities:
- **Users**: Registration, authentication, profile management
- **Tasks**: Creation, assignment, status tracking (NEW, PENDING, DONE, REJECTED)
- **Comments**: Task discussion threads with CRUD operations
- **Authentication**: Basic auth for login, bearer tokens for API access

## Important Constraints
- Java 25 required for backend compilation
- PostgreSQL database required (Docker Compose setup provided)
- Frontend must be built before backend deployment (handled by Maven)
- API follows OpenAPI contract - changes require spec updates

## External Dependencies
- **PostgreSQL**: Primary data storage (Docker container)
- **OpenAPI Generator**: Code generation from API specification
- **React Bootstrap**: UI component library
- **Axios**: HTTP client for API calls (planned usage)
