package dpr.playground.taskprovider.endpoint;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import dpr.playground.taskprovider.tasks.model.ErrorDTO;

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ErrorDTO> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        if (ex.getCause() != null && ex.getCause() instanceof ConstraintViolationException constraintViolation) {
            if ("uq_user__username".equals(constraintViolation.getConstraintName())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErrorDTO("Username already used"));
            }
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDTO(ex.getMessage()));
    }
}
