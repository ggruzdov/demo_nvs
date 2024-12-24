package com.ggruzdov.demo.apps.demo_nvs.response;

import com.ggruzdov.demo.apps.demo_nvs.model.Image;

import java.time.Instant;

public record ImageDetailsResponse(
    Long id,
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
