package com.github.ggruzdov.slideshow.repository;

import com.github.ggruzdov.slideshow.model.Image;
import com.github.ggruzdov.slideshow.response.OrderedImageDetailsResponse;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Integer> {

    List<Image> getAllByName(String name);

    @Query("select i from Image i where i.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Image findByIdForUpdate(Integer id);

    @Query(
        "select new com.github.ggruzdov.slideshow.response.OrderedImageDetailsResponse(i.id, i.url, i.duration, ssi.isCurrent, ssi.createdAt, i.createdAt) " +
        "from Image i join SlideShowImage ssi on i.id = ssi.pk.imageId where ssi.pk.slideShowId = :slideShowId " +
        "order by ssi.createdAt"
    )
    List<OrderedImageDetailsResponse> findAllSortedByAdditionDateAsc(Integer slideShowId);
}