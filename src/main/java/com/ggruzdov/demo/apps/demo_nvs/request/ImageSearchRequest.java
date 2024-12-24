package com.ggruzdov.demo.apps.demo_nvs.request;

import jakarta.validation.constraints.NotBlank;

public record ImageSearchRequest(

    @NotBlank
    String name
) {
}
