package com.ggruzdov.demo.apps.demo_nvs.component;

import com.ggruzdov.demo.apps.demo_nvs.exceptions.InvalidImageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageUrlValidatorImpl implements ImageUrlValidator {

    private final RestClient restClient;

    @Override
    public void validate(String imageUrl) throws InvalidImageException {
        log.debug("Validating image URL: {}", imageUrl);
        // It is assumed that we make http request and if status is not 200
        // or there is null body in the response than throw InvalidImageException
    }
}
