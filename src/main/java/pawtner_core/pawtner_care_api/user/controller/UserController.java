package pawtner_core.pawtner_care_api.user.controller;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pawtner_core.pawtner_care_api.auth.service.AuthTokenService;
import pawtner_core.pawtner_care_api.common.dto.PageResponse;
import pawtner_core.pawtner_care_api.user.dto.UserDetailResponse;
import pawtner_core.pawtner_care_api.user.dto.UserRequest;
import pawtner_core.pawtner_care_api.user.dto.UserResponse;
import pawtner_core.pawtner_care_api.user.dto.UserUpdateRequest;
import pawtner_core.pawtner_care_api.user.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthTokenService authTokenService;

    public UserController(UserService userService, AuthTokenService authTokenService) {
        this.userService = userService;
        this.authTokenService = authTokenService;
    }

    @GetMapping
    public PageResponse<UserResponse> getUsers(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String firstName,
        @RequestParam(required = false) String middleName,
        @RequestParam(required = false) String lastName,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String profilePicture,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir,
        @RequestParam(defaultValue = "true") boolean ignorePagination
    ) {
        return userService.getUsers(search, firstName, middleName, lastName, email, profilePicture, page, size, sortBy, sortDir, ignorePagination);
    }

    @GetMapping("/{id}")
    public UserDetailResponse getUser(@PathVariable UUID id) {
        return userService.getUser(id);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity
            .created(URI.create("/api/users/" + response.id()))
            .body(response);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(
        @PathVariable UUID id,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
        @Valid @RequestBody UserUpdateRequest request
    ) {
        return userService.updateUser(id, extractCurrentUserId(authorizationHeader), request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    private UUID extractCurrentUserId(String authorizationHeader) {
        return authTokenService.getUserIdFromToken(authTokenService.extractBearerToken(authorizationHeader));
    }
}

