package com.ggruzdov.demo.apps.demo_nvs.repository;

import com.ggruzdov.demo.apps.demo_nvs.model.SlideShowImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlideShowImageRepository extends JpaRepository<SlideShowImage, SlideShowImage.PK> {
}