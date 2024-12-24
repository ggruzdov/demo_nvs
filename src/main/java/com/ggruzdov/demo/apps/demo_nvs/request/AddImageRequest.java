package com.ggruzdov.demo.apps.demo_nvs.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddImageRequest(

    @NotBlank
    String url,

    // Perhaps there should be additional constraints, such as Min Max
    @NotNull
    Integer duration
) {
}
