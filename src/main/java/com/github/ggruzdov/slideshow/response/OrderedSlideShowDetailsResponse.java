package com.github.ggruzdov.slideshow.response;

import java.util.List;

public record OrderedSlideShowDetailsResponse(
    Integer slideShowId,
    OrderedImageDetailsResponse activeImage,
    List<OrderedImageDetailsResponse> images
) {
}
