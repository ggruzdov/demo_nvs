package com.ggruzdov.demo.apps.demo_nvs.service;

import com.ggruzdov.demo.apps.demo_nvs.component.ImageUrlValidator;
import com.ggruzdov.demo.apps.demo_nvs.model.Image;
import com.ggruzdov.demo.apps.demo_nvs.model.ProofOfPlay;
import com.ggruzdov.demo.apps.demo_nvs.model.SlideShow;
import com.ggruzdov.demo.apps.demo_nvs.model.SlideShowImage;
import com.ggruzdov.demo.apps.demo_nvs.repository.ImageRepository;
import com.ggruzdov.demo.apps.demo_nvs.repository.ProofOfPlayRepository;
import com.ggruzdov.demo.apps.demo_nvs.repository.SlideShowImageRepository;
import com.ggruzdov.demo.apps.demo_nvs.repository.SlideShowRepository;
import com.ggruzdov.demo.apps.demo_nvs.request.AddImageRequest;
import com.ggruzdov.demo.apps.demo_nvs.request.ImageSearchRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlideShowService {

    private final ImageRepository imageRepository;
    private final SlideShowRepository slideShowRepository;
    private final SlideShowImageRepository slideShowImageRepository;
    private final ProofOfPlayRepository proofOfPlayRepository;
    private final ImageUrlValidator imageUrlValidator;
    private final ExecutorService executorService;

    public SlideShow getOne(Integer slideShowId) {
         return slideShowRepository.findById(slideShowId)
             .orElseThrow(() -> new EntityNotFoundException("SlideShow with id " + slideShowId + " not found"));
    }

    public List<Image> searchImages(ImageSearchRequest request) {
        return imageRepository.getAllByName(request.name().toLowerCase());
    }

    @Transactional
    public Image createImage(AddImageRequest request) {
        imageUrlValidator.validate(request.url());
        var image = new Image(request.url(), request.duration());

        return imageRepository.save(image);
    }

    @Transactional
    public SlideShow create(List<AddImageRequest> request) {
        var slideShow = new SlideShow();

        // For now, we create SlideShow when all images are valid, otherwise reject request
        var completableFutures = request
            .stream()
            .map(it -> CompletableFuture.runAsync(() -> {
                    imageUrlValidator.validate(it.url());
                    slideShow.addImage(new Image(it.url(), it.duration()));
                }, executorService).orTimeout(30, TimeUnit.SECONDS)
            )
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(completableFutures)
            .thenRun(() -> {
                slideShow.setActiveImage(slideShow.getImages().first());
                slideShowRepository.save(slideShow);
            })
            .join();

        return slideShow;
    }

    @Transactional
    public void appendImage(Integer slideShowId, Integer imageId) {
        var pk = new SlideShowImage.PK(slideShowId, imageId);
        slideShowImageRepository.save(new SlideShowImage(pk));
    }

    // Apparently this method will be under high load and everything
    // except saving the very event should be processed asynchronously.
    // However, that is a big topic, let's discuss it in the interview.
    @Transactional
    public void saveProofOfPlay(Integer slideShowId, Long imageId) {
        proofOfPlayRepository.save(new ProofOfPlay(slideShowId, imageId));

        var slideShow = slideShowRepository.findByIdForUpdate(slideShowId).orElseThrow(
            () -> new EntityNotFoundException("Slide show with id " + slideShowId + " not found")
        );

        var activeImage = slideShow.getActiveImage();
        if (!activeImage.getId().equals(imageId)) {
            log.warn("Active image mismatch detected, activeImage id = {}, played imageId = {}", activeImage.getId(), imageId);
            activeImage = slideShow.getImage(imageId);
            if (activeImage == null) {
                log.warn("ImageId = {} doesn't belong to SlideShow with id {}", imageId, slideShowId);
                return;
            }
        }

        var nextImage = slideShow.getNextImage(activeImage);
        if (nextImage == null) {
            log.info("SlideShow {} reached its end, starting new circle", slideShowId);
            slideShow.setActiveImage(slideShow.getImages().first());
        } else {
            slideShow.setActiveImage(nextImage);
            log.info("Slide show {} next imageId is: {}", slideShowId, nextImage.getId());
        }
    }

    @Transactional
    public void deleteImage(Long id) {
        var image = imageRepository.findByIdForUpdate(id);
        if (image == null) {
            log.info("Image {} is already deleted", id);
            return;
        }

        image.getSlideShows().forEach(slideShow -> {
            slideShow.removeImage(image);
            if (slideShow.getImages().isEmpty()) {
                slideShowRepository.delete(slideShow);
            }
        });

        imageRepository.delete(image);
    }

    // It is assumed, that we remove SlideShow itself and its connections to images.
    // However, we do not remove images since they have to remain even if they don't belong to another SlideShows.
    @Transactional
    public void deleteSlideShow(Integer id) {
        slideShowRepository.deleteById(id);
    }
}
