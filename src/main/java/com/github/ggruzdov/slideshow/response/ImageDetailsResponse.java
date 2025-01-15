package com.github.ggruzdov.slideshow.response;

import com.github.ggruzdov.slideshow.model.Image;

import java.time.Instant;

public record ImageDetailsResponse(
    Integer id,
    String url,
    Integer duration,
    Instant createdAt
) {
    public static ImageDetailsResponse from(Image image) {
        return new ImageDetailsResponse(
            image.getId(),
            image.getUrl(),
            image.getDuration(),
            image.getCreatedAt()
        );
    }
}
