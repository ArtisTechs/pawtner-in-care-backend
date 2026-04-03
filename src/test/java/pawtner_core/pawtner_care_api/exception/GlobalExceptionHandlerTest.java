package pawtner_core.pawtner_care_api.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    @Test
    void handleUnexpectedReturnsGenericAdminContactMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ApiErrorResponse> response = handler.handleUnexpected(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
            "An unexpected error occurred. Please contact the administrator for assistance.",
            response.getBody().message()
        );
        assertEquals(Map.of(), response.getBody().validationErrors());
    }

    @Test
    void handleDataIntegrityViolationReturnsReferenceAwareMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ResponseEntity<ApiErrorResponse> response = handler.handleDataIntegrityViolation(
            new DataIntegrityViolationException("constraint failure")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
            "Request failed because this record is still referenced by other data. Please remove related records first.",
            response.getBody().message()
        );
        assertEquals(Map.of(), response.getBody().validationErrors());
    }
}
