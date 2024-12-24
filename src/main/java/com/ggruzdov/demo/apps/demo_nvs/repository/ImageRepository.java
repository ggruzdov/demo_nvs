package com.ggruzdov.demo.apps.demo_nvs.repository;

import com.ggruzdov.demo.apps.demo_nvs.model.Image;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {

    List<Image> getAllByName(String name);

    @Query("select i from Image i left join fetch i.slideShows where i.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Image findByIdForUpdate(Long id);
}