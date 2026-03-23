package pawtner_core.pawtner_care_api.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    String sortBy,
    String sortDirection,
    boolean first,
    boolean last,
    boolean ignorePagination
) {

    public static <T> PageResponse<T> fromPage(Page<T> page, String sortBy, String sortDirection, boolean ignorePagination) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            sortBy,
            sortDirection,
            page.isFirst(),
            page.isLast(),
            ignorePagination
        );
    }

    public static <T> PageResponse<T> fromList(List<T> content, String sortBy, String sortDirection, boolean ignorePagination) {
        int totalPages = content.isEmpty() ? 0 : 1;

        return new PageResponse<>(
            content,
            0,
            content.size(),
            content.size(),
            totalPages,
            sortBy,
            sortDirection,
            true,
            true,
            ignorePagination
        );
    }
}
