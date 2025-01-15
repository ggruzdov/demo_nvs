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
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.Objects;

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

    @Column(name = "is_current", nullable = false)
    private boolean isCurrent = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public SlideShowImage(PK pk) {
        this.pk = pk;
    }

    public Integer getSlideShowId() {
        return pk.getSlideShowId();
    }

    public Long getImageId() {
        return pk.getImageId();
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

        @Override
        public final boolean equals(Object o) {
            if (this == o) return true;
            if (o == null) return false;
            Class<?> oEffectiveClass = o instanceof HibernateProxy hp ? hp.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
            Class<?> thisEffectiveClass = this instanceof HibernateProxy hp ? hp.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
            if (thisEffectiveClass != oEffectiveClass) return false;
            PK that = (PK) o;
            return Objects.equals(slideShowId, that.slideShowId) && Objects.equals(imageId, that.imageId);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(slideShowId, imageId);
        }
    }
}
