package com.github.ggruzdov.slideshow.service;

import com.github.ggruzdov.slideshow.component.ImageUrlValidator;
import com.github.ggruzdov.slideshow.model.Image;
import com.github.ggruzdov.slideshow.model.ProofOfPlay;
import com.github.ggruzdov.slideshow.model.SlideShow;
import com.github.ggruzdov.slideshow.model.SlideShowImage;
import com.github.ggruzdov.slideshow.repository.ImageRepository;
import com.github.ggruzdov.slideshow.repository.ProofOfPlayRepository;
import com.github.ggruzdov.slideshow.repository.SlideShowImageRepository;
import com.github.ggruzdov.slideshow.repository.SlideShowRepository;
import com.github.ggruzdov.slideshow.request.AddImageRequest;
import com.github.ggruzdov.slideshow.request.ImageSearchRequest;
import com.github.ggruzdov.slideshow.response.OrderedSlideShowDetailsResponse;
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

    public OrderedSlideShowDetailsResponse getOrderedSlideShow(Integer slideShowId) {
        var orderedImages = imageRepository.findAllSortedByAdditionDateAsc(slideShowId);
        var activeImage = slideShowRepository.getActiveImage(slideShowId);
        var orderedActiveImage = orderedImages.stream().filter(it -> it.id().equals(activeImage.getId())).findFirst().orElseThrow();

        return new OrderedSlideShowDetailsResponse(slideShowId, orderedActiveImage, orderedImages);
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

        // For now, we create a SlideShow when all its images are valid, otherwise reject request.
        var completableFutures = new CompletableFuture[request.size()];
        for (int i = 0; i < request.size(); i++) {
            var currImg = request.get(i);
            completableFutures[i] = CompletableFuture.runAsync(
                () -> imageUrlValidator.validate(currImg.url()), executorService
            ).orTimeout(30, TimeUnit.SECONDS);

            // We want to preserve images order in which they were passed
            slideShow.addImage(new Image(currImg.url(), currImg.duration()));
        }

        CompletableFuture.allOf(completableFutures)
            .thenRun(() -> {
                slideShow.setActiveImage(slideShow.getImages().iterator().next());
                slideShowRepository.save(slideShow);
            })
            .join();

        return slideShow;
    }

    @Transactional
    public void appendImage(Integer slideShowId, Long imageId) {
        var pk = new SlideShowImage.PK(slideShowId, imageId);
        slideShowImageRepository.save(new SlideShowImage(pk));
    }

    // Apparently, this method will be under high load and everything
    // except saving the very event should be processed asynchronously.
    // However, that is a big topic and a good one to discuss.
    @Transactional
    public void saveProofOfPlay(Integer slideShowId, Long imageId) {
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

        var nextSlideShowImage = getNexSlideShowImageOrElseFirst(slideShowId, activeImage.getId());

        log.info("SlideShow {} next imageId is: {}", slideShowId, nextSlideShowImage.getPk().getImageId());
        var nextImage = slideShow.getImage(nextSlideShowImage.getPk().getImageId());

        slideShow.setActiveImage(nextImage);
        proofOfPlayRepository.save(new ProofOfPlay(slideShowId, imageId));
    }

    @Transactional
    public void deleteImage(Long id) {
        var image = imageRepository.findByIdForUpdate(id);
        if (image == null) {
            log.info("Image {} is already deleted", id);
            return;
        }

        image.getSlideShows().forEach(slideShow -> {
            if (slideShow.getActiveImage().equals(image)) {
                var nextSlideShowImage = getNexSlideShowImageOrElseFirst(slideShow.getId(), image.getId());
                slideShow.setActiveImage(imageRepository.getReferenceById(nextSlideShowImage.getPk().getImageId()));
            }
            slideShow.getImages().remove(image);

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

    private SlideShowImage getNexSlideShowImageOrElseFirst(Integer slideShowId, Long imageId) {
        return slideShowImageRepository
            .getNext(slideShowId, imageId)
            .orElseGet(() -> {
                log.info("SlideShow {} reached its end, starting new circle", slideShowId);
                return slideShowImageRepository.findFirstByPkSlideShowIdOrderByCreatedAt(slideShowId);
            });
    }
}
