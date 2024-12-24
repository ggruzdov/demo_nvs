package com.ggruzdov.demo.apps.demo_nvs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "proofs_of_play")
@Immutable
public class ProofOfPlay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long imageId;

    @Column(nullable = false)
    private Integer slideShowId;

    @CreationTimestamp
    private Instant createdAt;

    public ProofOfPlay(Integer slideShowId, Long imageId) {
        this.slideShowId = slideShowId;
        this.imageId = imageId;
    }
}
