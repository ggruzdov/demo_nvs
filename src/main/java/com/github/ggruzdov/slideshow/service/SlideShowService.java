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

import java.util.ArrayList;
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
        return new OrderedSlideShowDetailsResponse(slideShowId, orderedImages);
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
        var images = new ArrayList<Image>(request.size());

        // For now, we create a SlideShow when all its images are valid, otherwise reject request.
        var completableFutures = new CompletableFuture[request.size()];
        for (int i = 0; i < request.size(); i++) {
            var currImg = request.get(i);
            completableFutures[i] = CompletableFuture.runAsync(
                () -> imageUrlValidator.validate(currImg.url()), executorService
            ).orTimeout(30, TimeUnit.SECONDS);

            // We want to preserve images order in which they were passed
            images.add(new Image(currImg.url(), currImg.duration()));
        }

        var slideShow = new SlideShow();
        CompletableFuture.allOf(completableFutures)
            .thenRun(() -> {
                slideShowRepository.save(slideShow);
                imageRepository.saveAll(images);
                var slideShowImages = images
                    .stream()
                    .map(it -> new SlideShowImage(new SlideShowImage.PK(slideShow.getId(), it.getId())))
                    .toList();
                slideShowImages.getFirst().setCurrent(true);
                slideShowImageRepository.saveAll(slideShowImages);
            })
            .join();

        return slideShow;
    }

    @Transactional
    public void appendImage(Integer slideShowId, Integer imageId) {
        var pk = new SlideShowImage.PK(slideShowId, imageId);
        slideShowImageRepository.save(new SlideShowImage(pk));
    }

    @Transactional
    public void removeImage(Integer slideShowId, Integer imageId) {
        var ssiPk = new SlideShowImage.PK(slideShowId, imageId);
        var slideShowImage = slideShowImageRepository.findByIdForUpdate(ssiPk);
        if (slideShowImage == null) {
            log.info("SlideShowImage {} not found", ssiPk);
            return;
        }

        if (slideShowImage.isCurrent()) {
            var nextSlideShowImage = getNexSlideShowImageOrElseFirst(slideShowId, imageId);
            // Here we see that there is only one image in the SlideShow so we can remove it as well
            if (nextSlideShowImage.equals(slideShowImage)) {
                slideShowImageRepository.deleteById(ssiPk);
                slideShowImageRepository.flush();
                slideShowRepository.deleteById(slideShowId);
            } else {
                nextSlideShowImage.setCurrent(true);
            }
        } else {
            slideShowImageRepository.deleteById(ssiPk);
        }
    }

    // Apparently, this method will be under high load and everything
    // except saving the very event should be processed asynchronously.
    // However, that is a big topic and a good one to discuss.
    @Transactional
    public void saveProofOfPlay(Integer slideShowId, Integer imageId) {
        slideShowRepository.findByIdForUpdate(slideShowId).orElseThrow(
            () -> new EntityNotFoundException("Slide show with id " + slideShowId + " not found")
        );

        var playedSlideShowImage = slideShowImageRepository.findById(new SlideShowImage.PK(slideShowId, imageId)).orElse(null);
        if (playedSlideShowImage == null) {
            log.warn("ImageId = {} doesn't belong to SlideShow with id {}", imageId, slideShowId);
            return;
        }

        if (!playedSlideShowImage.isCurrent()) {
            var currentSlideShowImage = slideShowImageRepository.findByPkSlideShowIdAndIsCurrentTrue(slideShowId);
            log.warn(
                "Active image mismatch detected, activeImage id = {}, played imageId = {}",
                currentSlideShowImage.getPk().getImageId(), imageId
            );
            currentSlideShowImage.setCurrent(false);
        }

        var nextSlideShowImage = getNexSlideShowImageOrElseFirst(slideShowId, imageId);

        log.info("SlideShow {} next imageId is: {}", slideShowId, nextSlideShowImage.getPk().getImageId());
        playedSlideShowImage.setCurrent(false);
        nextSlideShowImage.setCurrent(true);
        proofOfPlayRepository.save(new ProofOfPlay(slideShowId, imageId));
    }

    @Transactional
    public void deleteImage(Integer id) {
        var image = imageRepository.findByIdForUpdate(id);
        if (image == null) {
            log.info("Image {} is already deleted", id);
            return;
        }

        var slideShowImages = slideShowImageRepository.findAllByPkImageId(id);
        slideShowImages.forEach(ssi -> {
            if (ssi.isCurrent()) {
                var nextSlideShowImage = getNexSlideShowImageOrElseFirst(ssi.getPk().getSlideShowId(), image.getId());
                if (!nextSlideShowImage.getImageId().equals(id)) {
                    nextSlideShowImage.setCurrent(true);
                }
            }
        });

        if (!slideShowImages.isEmpty()) {
            slideShowImageRepository.deleteAllByPkImageId(id);
            var slideShowIds = slideShowImages.stream().map(SlideShowImage::getSlideShowId).toList();
            slideShowRepository.deleteIfEmpty(slideShowIds);
        }

        imageRepository.delete(image);
    }

    // It is assumed, that we remove SlideShow itself and its connections to images.
    // However, we do not remove images since they have to remain even if they don't belong to another SlideShows.
    @Transactional
    public void deleteSlideShow(Integer id) {
        slideShowImageRepository.deleteAllByPkSlideShowId(id);
        slideShowRepository.deleteById(id);
    }

    private SlideShowImage getNexSlideShowImageOrElseFirst(Integer slideShowId, Integer imageId) {
        return slideShowImageRepository
            .getNext(slideShowId, imageId)
            .orElseGet(() -> {
                log.info("SlideShow {} reached its end, starting new circle", slideShowId);
                return slideShowImageRepository.findFirstByPkSlideShowIdOrderByCreatedAt(slideShowId);
            });
    }
}
