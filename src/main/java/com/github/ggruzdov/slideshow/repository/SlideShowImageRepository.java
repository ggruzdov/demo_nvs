package com.github.ggruzdov.slideshow.repository;

import com.github.ggruzdov.slideshow.model.SlideShowImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlideShowImageRepository extends JpaRepository<SlideShowImage, SlideShowImage.PK> {
}