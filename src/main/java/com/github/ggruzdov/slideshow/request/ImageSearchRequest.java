package com.github.ggruzdov.slideshow.request;

import jakarta.validation.constraints.NotBlank;

public record ImageSearchRequest(

    @NotBlank
    String name
) {
}
