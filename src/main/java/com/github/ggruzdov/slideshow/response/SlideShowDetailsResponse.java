package com.github.ggruzdov.slideshow.response;

import com.github.ggruzdov.slideshow.model.SlideShow;

import java.util.List;

public record SlideShowDetailsResponse(
    ImageDetailsResponse activeImage,
    List<ImageDetailsResponse> images
) {
    public static SlideShowDetailsResponse from(SlideShow slideShow) {
        return new SlideShowDetailsResponse(
            ImageDetailsResponse.from(slideShow.getActiveImage()),
            slideShow.getImages()
                .stream()
                .map(ImageDetailsResponse::from)
                .toList()
        );
    }
}
