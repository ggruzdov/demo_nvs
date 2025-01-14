package com.github.ggruzdov.slideshow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Auxiliary entity for many-to-many relation.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "slide_shows_images")
public class SlideShowImage {

    @EmbeddedId
    private PK pk;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public SlideShowImage(PK pk) {
        this.pk = pk;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Embeddable
    public static class PK {

        @Column(name = "slide_show_id", nullable = false)
        private Integer slideShowId;

        @Column(name = "image_id", nullable = false)
        private Long imageId;



    }
}
