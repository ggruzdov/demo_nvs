package com.github.ggruzdov.slideshow.repository;

import com.github.ggruzdov.slideshow.model.Image;
import com.github.ggruzdov.slideshow.model.SlideShow;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SlideShowRepository extends JpaRepository<SlideShow, Integer> {

    @Query("select ss from SlideShow ss join fetch ss.images join fetch ss.activeImage where ss.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SlideShow> findByIdForUpdate(Integer id);

    @Query("select i from Image i join SlideShow ss on ss.activeImage.id = i.id where ss.id = :slideShowId")
    Image getActiveImage(Integer slideShowId);
}