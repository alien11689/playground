package dpr.playground.taskprovider.tasks;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    
    Page<Project> findAll(Pageable pageable);

    Optional<Project> findById(UUID id);

    boolean existsById(UUID id);
}
