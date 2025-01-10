package com.ggruzdov.demo.apps.demo_nvs.component;

import com.ggruzdov.demo.apps.demo_nvs.exceptions.InvalidImageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageUrlValidatorImpl implements ImageUrlValidator {

    private final RestClient restClient;

    @Override
    public void validate(String imageUrl) throws InvalidImageException {
        log.debug("Validating image URL: {}", imageUrl);
        try {
            var response = restClient.get()
                .uri(imageUrl)
                .retrieve()
                .toEntity(byte[].class);
            if (!response.hasBody()) {
                throw new InvalidImageException(imageUrl);
            }
        } catch (RestClientResponseException e) {
            if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                throw new InvalidImageException("Image not found: " + imageUrl);
            }

            // Perhaps retry the request a couple of times in case of 500 errors
            throw e;
        }
    }
}
