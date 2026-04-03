package pawtner_core.pawtner_care_api.user.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import pawtner_core.pawtner_care_api.auth.service.AuthTokenService;
import pawtner_core.pawtner_care_api.user.dto.UserActiveUpdateRequest;
import pawtner_core.pawtner_care_api.user.dto.UserResponse;
import pawtner_core.pawtner_care_api.user.enums.UserRole;
import pawtner_core.pawtner_care_api.user.service.UserService;

class UserControllerTest {

    private MockMvc mockMvc;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserService.class);
        AuthTokenService authTokenService = Mockito.mock(AuthTokenService.class);

        mockMvc = MockMvcBuilders.standaloneSetup(new UserController(userService, authTokenService)).build();
    }

    @Test
    void updateUserActiveStatusShouldHandlePutActiveRoute() throws Exception {
        UUID userId = UUID.fromString("bea40f6e-770f-45e4-8bfc-b930ec8a186f");
        UserResponse response = new UserResponse(
            userId,
            "Jane",
            null,
            "Doe",
            "jane@example.com",
            null,
            UserRole.USER,
            Boolean.FALSE,
            LocalDateTime.of(2026, 4, 4, 3, 0),
            LocalDateTime.of(2026, 4, 4, 3, 17)
        );

        when(userService.updateUserActiveStatus(eq(userId), eq(new UserActiveUpdateRequest(Boolean.FALSE))))
            .thenReturn(response);

        mockMvc.perform(
                put("/api/users/{id}/active", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "active": false
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.active").value(false));

        verify(userService).updateUserActiveStatus(eq(userId), eq(new UserActiveUpdateRequest(Boolean.FALSE)));
    }

    @Test
    void patchUserActiveStatusShouldHandlePatchActiveRoute() throws Exception {
        UUID userId = UUID.fromString("bea40f6e-770f-45e4-8bfc-b930ec8a186f");
        UserResponse response = new UserResponse(
            userId,
            "Jane",
            null,
            "Doe",
            "jane@example.com",
            null,
            UserRole.USER,
            Boolean.TRUE,
            LocalDateTime.of(2026, 4, 4, 3, 0),
            LocalDateTime.of(2026, 4, 4, 3, 26)
        );

        when(userService.updateUserActiveStatus(eq(userId), eq(new UserActiveUpdateRequest(Boolean.TRUE))))
            .thenReturn(response);

        mockMvc.perform(
                patch("/api/users/{id}/active", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                          "active": true
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId.toString()))
            .andExpect(jsonPath("$.active").value(true));

        verify(userService).updateUserActiveStatus(eq(userId), eq(new UserActiveUpdateRequest(Boolean.TRUE)));
    }
}
