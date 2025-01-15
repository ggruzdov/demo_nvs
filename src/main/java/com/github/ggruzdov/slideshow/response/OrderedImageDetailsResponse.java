package com.github.ggruzdov.slideshow.response;

import java.time.Instant;

public record OrderedImageDetailsResponse(
    Long id,
    String url,
    Integer duration,
    boolean isCurrent,
    Instant appendedAt,
    Instant createdAt
) {
}
