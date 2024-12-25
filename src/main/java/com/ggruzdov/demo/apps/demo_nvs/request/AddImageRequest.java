package com.ggruzdov.demo.apps.demo_nvs.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(
    description = "Request to add an Image",
    example = """
            {
                "url": "https://nvs.s3.us-east-2.amazonaws.com/images/Sea.jpg",
                "duration": 20
            }
        """
)
public record AddImageRequest(

    @NotBlank
    @Parameter(required = true, description = "Valid URL to download an image. Supported formats: JPEG, PNG, WEBP")
    String url,

    // Perhaps there should be additional constraints, such as Min Max
    @NotNull
    @Parameter(required = true, description = "Image display duration in seconds")
    Integer duration
) {
}
