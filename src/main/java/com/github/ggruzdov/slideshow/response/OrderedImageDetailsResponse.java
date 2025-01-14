package com.github.ggruzdov.slideshow.response;

import java.time.Instant;

public record OrderedImageDetailsResponse(
    Long id,
    String url,
    Integer duration,
    Instant appendedAt,
    Instant createdAt
) {
}
