package pawtner_core.pawtner_care_api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import pawtner_core.pawtner_care_api.auth.service.AuthTokenService;

@ExtendWith(MockitoExtension.class)
class BearerTokenAuthFilterTest {

    @Mock
    private AuthTokenService authTokenService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validTokenAuthenticatesProtectedRequest() throws ServletException, IOException {
        BearerTokenAuthFilter filter = new BearerTokenAuthFilter(authTokenService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer login-token");
        when(authTokenService.isValid("login-token")).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("api-client", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        verify(authTokenService).isValid("login-token");
    }

    @Test
    void invalidTokenReturnsUnauthorized() throws ServletException, IOException {
        BearerTokenAuthFilter filter = new BearerTokenAuthFilter(authTokenService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/events");
        MockHttpServletResponse response = new MockHttpServletResponse();

        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer bad-token");
        when(authTokenService.isValid("bad-token")).thenReturn(false);

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(401, response.getStatus());
        assertEquals("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Invalid bearer token\"}", response.getContentAsString());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void authRoutesBypassBearerValidation() throws ServletException, IOException {
        BearerTokenAuthFilter filter = new BearerTokenAuthFilter(authTokenService);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(200, response.getStatus());
        verifyNoInteractions(authTokenService);
    }
}
