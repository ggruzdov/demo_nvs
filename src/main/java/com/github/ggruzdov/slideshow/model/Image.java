package com.github.ggruzdov.slideshow.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "images")
public class Image implements Comparable<Image> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "images_id_seq")
    @SequenceGenerator(name = "images_id_seq", allocationSize = 10, sequenceName = "images_id_seq")
    private Long id;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer duration;

    @ManyToMany(mappedBy = "images", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<SlideShow> slideShows;

    private Instant createdAt;

    public Image(String url, Integer duration) {
        this.url = url;
        this.duration = duration;
        this.name = url.substring(url.lastIndexOf('/') + 1).split("\\.")[0].toLowerCase();
        this.createdAt = Instant.now();
    }

    @Override
    public int compareTo(Image image) {
        return this.createdAt.compareTo(image.createdAt);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy hp ? hp.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy hp ? hp.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Image that = (Image) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(url);
    }
}
