package com.ggruzdov.demo.apps.demo_nvs;

import com.ggruzdov.demo.apps.demo_nvs.model.Image;
import com.ggruzdov.demo.apps.demo_nvs.model.SlideShow;
import com.ggruzdov.demo.apps.demo_nvs.response.AddImageResponse;
import com.ggruzdov.demo.apps.demo_nvs.response.AddSlideShowResponse;
import com.ggruzdov.demo.apps.demo_nvs.response.ImageDetailsResponse;
import com.ggruzdov.demo.apps.demo_nvs.response.SlideShowDetailsResponse;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoNvsApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private RestClient restClient;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @SuppressWarnings("SqlWithoutWhere")
    @BeforeEach
    void cleanUpDB() {
        transactionTemplate.execute(tx -> {
            entityManager.createNativeQuery("DELETE FROM slide_shows_images").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM slide_shows").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM images").executeUpdate();
            entityManager.createNativeQuery("DELETE FROM proofs_of_play").executeUpdate();
            return null;
        });
    }

    @Test
    void addImage() {
        // Given
        var mountainsImage =
            """
              {
                "url": "https://nvs.s3.us-east-2.amazonaws.com/images/Mountains.jpg",
                "duration": 30
              }
            """;

        // When
        var result = restClient
            .post()
            .uri("http://localhost:%d/addImage".formatted(port))
            .contentType(MediaType.APPLICATION_JSON)
            .body(mountainsImage)
            .retrieve()
            .body(AddImageResponse.class);

        // Then
        assertNotNull(result);
        var image = entityManager.find(Image.class, result.id());
        assertNotNull(result);
        assertEquals("mountains", image.getName());
    }

    @Test
    void addSlideShow() {
        // Given
        var imageList =
        """
          [
            {
              "url": "https://nvs.s3.us-east-2.amazonaws.com/images/Sea.jpg",
              "duration": 10
            },
            {
              "url": "https://nvs.s3.us-east-2.amazonaws.com/images/Bird.jpg",
              "duration": 15
            },
            {
              "url": "https://nvs.s3.us-east-2.amazonaws.com/images/Wick.jpg",
              "duration": 20
            }
          ]
        """;

        // When
        var result = restClient
            .post()
            .uri("http://localhost:%d/addSlideshow".formatted(port))
            .contentType(MediaType.APPLICATION_JSON)
            .body(imageList)
            .retrieve()
            .body(AddSlideShowResponse.class);

        // Then
        assertNotNull(result);
        var slideShow = entityManager.find(SlideShow.class, result.id());
        assertEquals(3, slideShow.getImages().size());
    }

    @Test
    void getSlideShow() {
        // Given
        var slideShow = persistSlideShow();

        // When
        var result = restClient
            .get()
            .uri("http://localhost:%d/slideShow/%d/slideshowOrder".formatted(port, slideShow.getId()))
            .retrieve()
            .body(new ParameterizedTypeReference<SlideShowDetailsResponse>() {});

        // Then
        assertNotNull(result);
        assertEquals(3, result.images().size());
        assertEquals("https://nvs.s3.us-east-2.amazonaws.com/images/Sea.jpg", result.activeImage().url());
    }

    @Test
    void imageSearch() {
        // Given
        var image = persistMountainsImage();

        // When
        var result = restClient
            .get()
            .uri("http://localhost:%d/images/search?name=mountains".formatted(port))
            .retrieve()
            .body(new ParameterizedTypeReference<List<ImageDetailsResponse>>() {});

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(image.getId(), result.getFirst().id());
        assertEquals(image.getUrl(), result.getFirst().url());
    }

    @Test
    void saveProofOfPlay() {
        // Given
        var persistedSlideShow = persistSlideShow();

        // When
        restClient
            .post()
            .uri("http://localhost:%d/slideShow/%d/proof-of-play/%d".formatted(port, persistedSlideShow.getId(), persistedSlideShow.getActiveImage().getId()))
            .retrieve()
            .toEntity(Void.class);

        // Then
        var result = entityManager.find(SlideShow.class, persistedSlideShow.getId());
        assertNotNull(result);
        assertEquals("bird", result.getActiveImage().getName());
    }

    @Test
    void deleteImage() {
        // Given
        var image = persistMountainsImage();

        // When
        restClient
            .delete()
            .uri("http://localhost:%d/deleteImage/%d".formatted(port, image.getId()))
            .retrieve()
            .toEntity(Void.class);

        // Then
        assertNull(entityManager.find(Image.class, image.getId()));
    }

    @Test
    void deleteSlideShow() {
        // Given
        var persistedSlideShow = persistSlideShow();
        var imageIds = persistedSlideShow.getImages().stream().map(Image::getId);

        // When
        restClient
            .delete()
            .uri("http://localhost:%d/deleteSlideshow/%d".formatted(port, persistedSlideShow.getId()))
            .retrieve()
            .toEntity(Void.class);

        // Then
        assertNull(entityManager.find(SlideShow.class, persistedSlideShow.getId()));
        // Ensure that all connected images still remain in DB
        imageIds.forEach(imageId -> {
            var image = entityManager
                .createQuery("SELECT i FROM Image i LEFT JOIN FETCH i.slideShows WHERE i.id = :id", Image.class)
                .setParameter("id", imageId)
                .getSingleResult();
            assertNotNull(image);
            assertTrue(image.getSlideShows().isEmpty());
        });
    }

    private Image persistMountainsImage() {
        return transactionTemplate.execute(tx -> {
            var image = new Image("https://nvs.s3.us-east-2.amazonaws.com/images/Mountains.jpg", 30);
            entityManager.persist(image);
            return image;
        });
    }

    private SlideShow persistSlideShow() {
        return transactionTemplate.execute(tx -> {
            var slideShow = new SlideShow();
            slideShow.addImage(new Image("https://nvs.s3.us-east-2.amazonaws.com/images/Sea.jpg", 10));
            slideShow.addImage(new Image("https://nvs.s3.us-east-2.amazonaws.com/images/Bird.jpg", 15));
            slideShow.addImage(new Image("https://nvs.s3.us-east-2.amazonaws.com/images/Wick.jpg", 20));
            slideShow.setActiveImage(slideShow.getImages().first());
            entityManager.persist(slideShow);

            return slideShow;
        });
    }
}
