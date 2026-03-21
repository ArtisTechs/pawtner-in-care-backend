package pawtner_core.pawtner_care_api.dto;

import java.util.List;

public record UserPageResponse(
    List<UserResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    String sortBy,
    String sortDirection,
    boolean first,
    boolean last
) {
}
