package com.github.ggruzdov.slideshow.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "slide_shows")
public class SlideShow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    private Image activeImage;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(
        name = "slide_shows_images",
        joinColumns = @JoinColumn(name = "slide_show_id"),
        inverseJoinColumns = @JoinColumn(name = "image_id"))
    private Set<Image> images = new LinkedHashSet<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Image getImage(Long imageId) {
        return images.stream().filter(image -> image.getId().equals(imageId)).findFirst().orElse(null);
    }

//    public Image getNextImage(Image image) {
//        var imagesTail = images.tailSet(image).iterator();
//
//        if (imagesTail.hasNext()) {
//            imagesTail.next(); // skip argument image
//            if (imagesTail.hasNext()) {
//                return imagesTail.next();
//            }
//        }
//
//        return null;
//    }

    public void addImage(Image image) {
        images.add(image);
    }

//    public void removeImage(Image image) {
//        if (activeImage.equals(image)) {
//            activeImage = getNextImage(activeImage);
//        }
//
//        images.remove(image);
//    }

    // Since we do not have more than one transient(not persisted)
    // SlideShow in one moment of time using Object methods is suitable.
    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy hp ? hp.getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy hp ? hp.getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        SlideShow that = (SlideShow) o;
        return id != null ? id.equals(that.id) : super.equals(o);
    }

    @Override
    public final int hashCode() {
        return id != null ? Objects.hashCode(id) : super.hashCode();
    }
}
