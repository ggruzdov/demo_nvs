package com.ggruzdov.demo.apps.demo_nvs.controller;

import com.ggruzdov.demo.apps.demo_nvs.request.AddImageRequest;
import com.ggruzdov.demo.apps.demo_nvs.request.ImageSearchRequest;
import com.ggruzdov.demo.apps.demo_nvs.response.AddImageResponse;
import com.ggruzdov.demo.apps.demo_nvs.response.AddSlideShowResponse;
import com.ggruzdov.demo.apps.demo_nvs.response.ImageDetailsResponse;
import com.ggruzdov.demo.apps.demo_nvs.response.SlideShowDetailsResponse;
import com.ggruzdov.demo.apps.demo_nvs.service.SlideShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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
@Tag(name = "Slideshow series", description = "API endpoints for managing slideshows and images")
public class SlideShowController {

    private final SlideShowService slideShowService;

    @Operation(
        summary = "Get a slideshow with ordered images by creation datetime",
        description = "Retrieves a slideshow by ID with its images ordered by creation date"
    )
    @GetMapping(value = "/slideShow/{id}/slideshowOrder", produces = MediaType.APPLICATION_JSON_VALUE)
    public SlideShowDetailsResponse getSlideshowSorted(@PathVariable Integer id) {
        log.info("Getting sorted slideshow, slideShowId = {}", id);
        var slideShow = slideShowService.getOne(id);
        return SlideShowDetailsResponse.from(slideShow);
    }

    @Operation(
        summary = "Simple image search by query params with strict equality",
        description = "Search for images using query parameters. The search is case insensitive."
    )
    @GetMapping(value = "/images/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ImageDetailsResponse> searchImages(@Valid ImageSearchRequest request) {
        log.info("Searching images, request = {}", request);
        return slideShowService.searchImages(request)
            .stream()
            .map(ImageDetailsResponse::from)
            .toList();
    }

    @Operation(
        summary = "Add an image into system",
        description = "Creates a new image entry in the system with the provided details"
    )
    @PostMapping(value = "/addImage", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AddImageResponse addImage(@Valid @RequestBody AddImageRequest request) {
        log.info("Adding new image {}", request.url());
        var image = slideShowService.createImage(request);
        return new AddImageResponse(image.getId());
    }

    @Operation(
        summary = "Create a slideshow with a list of images",
        description = "Creates a new slideshow containing the provided list of images"
    )
    @PostMapping(value = "/addSlideshow", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AddSlideShowResponse addSlideShow(@Valid @RequestBody List<AddImageRequest> request) {
        log.info("Adding new slideshow, images size = {}", request.size());
        var slideShow = slideShowService.create(request);
        return new AddSlideShowResponse(slideShow.getId());
    }

    @Operation(
        summary = "Delete an image and its relations to slideshows",
        description = "Removes an image and all its associated slideshow relationships"
    )
    @DeleteMapping("/deleteImage/{id}")
    public void deleteImage(@PathVariable Long id) {
        log.info("Deleting image {}", id);
        slideShowService.deleteImage(id);
    }

    @Operation(
        summary = "Delete a slideshow",
        description = "Removes a slideshow while preserving all related images in the system"
    )
    @DeleteMapping("/deleteSlideshow/{id}")
    public void deleteSlideShow(@PathVariable Integer id) {
        log.info("Deleting SlideShow {}", id);
        slideShowService.deleteSlideShow(id);
    }

    @Operation(
        summary = "Save proof of play event and change active image",
        description = "Records a proof of play event and updates the active image in a slideshow"
    )
    @PostMapping("/slideShow/{id}/proof-of-play/{imageId}")
    public void saveProofOfPlay(@PathVariable Integer id, @PathVariable Long imageId) {
        log.info("Saving proof of play, slideShowId = {}, imageId = {} ", id, imageId);
        slideShowService.saveProofOfPlay(id, imageId);
    }
}
