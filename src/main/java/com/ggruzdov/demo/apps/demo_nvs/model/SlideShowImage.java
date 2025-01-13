package com.ggruzdov.demo.apps.demo_nvs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Auxiliary entity for many-to-many relation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "slide_shows_images")
public class SlideShowImage {

    @EmbeddedId
    private PK pk;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Embeddable
    public static class PK {

        @Column(name = "slide_show_id", nullable = false)
        private Integer slideShowId;

        @Column(name = "image_id", nullable = false)
        private Integer imageId;
    }
}
