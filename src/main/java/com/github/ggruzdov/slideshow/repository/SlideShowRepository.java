package com.github.ggruzdov.slideshow.repository;

import com.github.ggruzdov.slideshow.model.SlideShow;
import jakarta.persistence.LockModeType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SlideShowRepository extends JpaRepository<SlideShow, Integer> {

    @Query("select ss from SlideShow ss where ss.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SlideShow> findByIdForUpdate(Integer id);

    @Override
    @Query("delete from SlideShow where id = :id")
    @Modifying
    void deleteById(@NonNull Integer id);

    @Query("delete from SlideShow where not exists (select 1 from SlideShowImage ssi where ssi.pk.slideShowId in :ids)")
    @Modifying
    void deleteIfEmpty(List<Integer> ids);
}