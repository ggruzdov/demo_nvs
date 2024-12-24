package com.ggruzdov.demo.apps.demo_nvs.component;

import com.ggruzdov.demo.apps.demo_nvs.exceptions.InvalidImageException;

public interface ImageUrlValidator {

    void validate(String url) throws InvalidImageException;
}
