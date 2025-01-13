package com.github.ggruzdov.slideshow.component;

import com.github.ggruzdov.slideshow.exceptions.InvalidImageException;

public interface ImageUrlValidator {

    void validate(String url) throws InvalidImageException;
}
