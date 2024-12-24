package com.ggruzdov.demo.apps.demo_nvs.controller;

import com.ggruzdov.demo.apps.demo_nvs.request.AddImageRequest;
import com.ggruzdov.demo.apps.demo_nvs.request.ImageSearchRequest;
import com.ggruzdov.demo.apps.demo_nvs.response.AddImageResponse;
import com.ggruzdov.demo.apps.demo_nvs.response.AddSlideShowResponse;
import com.ggruzdov.demo.apps.demo_nvs.response.ImageDetailsResponse;
import com.ggruzdov.demo.apps.demo_nvs.response.SlideShowDetailsResponse;
import com.ggruzdov.demo.apps.demo_nvs.service.SlideShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SlideShowController {

    private final SlideShowService slideShowService;

    @GetMapping("/slideShow/{id}/slideshowOrder")
    public SlideShowDetailsResponse getSlideshowSorted(@PathVariable Integer id) {
        log.info("Getting sorted slideshow, slideShowId = {}", id);
        var slideShow = slideShowService.getOne(id);
        return SlideShowDetailsResponse.from(slideShow);
    }

    @GetMapping("/images/search")
    public List<ImageDetailsResponse> searchImages(@Valid ImageSearchRequest request) {
        log.info("Searching images, request = {}", request);
        return slideShowService.searchImages(request)
            .stream()
            .map(ImageDetailsResponse::from)
            .toList();
    }

    @PostMapping("/addImage")
    public AddImageResponse addImage(@Valid @RequestBody AddImageRequest request) {
        log.info("Adding new image {}", request.url());
        var image = slideShowService.createImage(request);
        return new AddImageResponse(image.getId());
    }

    @PostMapping("/addSlideshow")
    public AddSlideShowResponse addSlideShow(@Valid @RequestBody List<AddImageRequest> request) {
        log.info("Adding new SlideShow, images size = {}", request.size());
        var slideShow = slideShowService.create(request);
        return new AddSlideShowResponse(slideShow.getId());
    }

    @DeleteMapping("/deleteImage/{id}")
    public void deleteImage(@PathVariable Long id) {
        log.info("Deleting image {}", id);
        slideShowService.deleteImage(id);
    }

    @DeleteMapping("/deleteSlideshow/{id}")
    public void deleteSlideShow(@PathVariable Integer id) {
        log.info("Deleting SlideShow {}", id);
        slideShowService.deleteSlideShow(id);
    }

    @PostMapping("/slideShow/{id}/proof-of-play/{imageId}")
    public void saveProofOfPlay(@PathVariable Integer id, @PathVariable Long imageId) {
        log.info("Saving proof of play, slideShowId = {}, imageId = {} ", id, imageId);
        slideShowService.saveProofOfPlay(id, imageId);
    }
}
