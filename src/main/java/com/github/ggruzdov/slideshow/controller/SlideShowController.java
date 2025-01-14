package com.github.ggruzdov.slideshow.controller;

import com.github.ggruzdov.slideshow.request.AddImageRequest;
import com.github.ggruzdov.slideshow.request.ImageSearchRequest;
import com.github.ggruzdov.slideshow.response.AddImageResponse;
import com.github.ggruzdov.slideshow.response.AddSlideShowResponse;
import com.github.ggruzdov.slideshow.response.ImageDetailsResponse;
import com.github.ggruzdov.slideshow.response.OrderedSlideShowDetailsResponse;
import com.github.ggruzdov.slideshow.service.SlideShowService;
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
        summary = "Get a slideshow with ordered images by addition date",
        description = "Retrieves a slideshow by ID with its images ordered by addition date"
    )
    @GetMapping(value = "/slideshow/{id}/ordered", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderedSlideShowDetailsResponse getSlideshowOrdered(@PathVariable Integer id) {
        log.info("Getting ordered slideshow, id = {}", id);
         return slideShowService.getOrderedSlideShow(id);
    }

    @Operation(
        summary = "Simple image search by query params with strict equality",
        description = "Search for images using query parameters. The search is case insensitive"
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
    @PostMapping(value = "/image", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AddImageResponse addImage(@Valid @RequestBody AddImageRequest request) {
        log.info("Adding new image {}", request.url());
        var image = slideShowService.createImage(request);
        return new AddImageResponse(image.getId());
    }

    @Operation(
        summary = "Create a slideshow with a list of images",
        description = "Creates a new slideshow containing the provided list of images"
    )
    @PostMapping(value = "/slideshow", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public AddSlideShowResponse addSlideShow(@Valid @RequestBody List<AddImageRequest> request) {
        log.info("Adding new slideshow, images size = {}", request.size());
        var slideShow = slideShowService.create(request);
        return new AddSlideShowResponse(slideShow.getId());
    }

    @Operation(
        summary = "Append an image to a slideshow",
        description = "Append a registered in the system image to an existing slideshow"
    )
    @PostMapping(value = "/slideshow/{id}/append/{imageId}")
    public void appendImage(@PathVariable Integer id, @PathVariable Long imageId) {
        log.info("Appending image = {} to slideshow {}", imageId, id);
        slideShowService.appendImage(id, imageId);
    }

    @Operation(
        summary = "Delete an image and its relations to slideshows",
        description = "Removes an image and all its associated slideshow relationships"
    )
    @DeleteMapping("/image/{id}")
    public void deleteImage(@PathVariable Long id) {
        log.info("Deleting image {}", id);
        slideShowService.deleteImage(id);
    }

    @Operation(
        summary = "Delete a slideshow",
        description = "Removes a slideshow while preserving all related images in the system"
    )
    @DeleteMapping("/slideshow/{id}")
    public void deleteSlideShow(@PathVariable Integer id) {
        log.info("Deleting SlideShow {}", id);
        slideShowService.deleteSlideShow(id);
    }

    @Operation(
        summary = "Save proof of play event and change active image",
        description = "Records a proof of play event and updates the active image in a slideshow"
    )
    @PostMapping("/slideshow/{id}/proof-of-play/{imageId}")
    public void saveProofOfPlay(@PathVariable Integer id, @PathVariable Long imageId) {
        log.info("Saving proof of play, slideShowId = {}, imageId = {} ", id, imageId);
        slideShowService.saveProofOfPlay(id, imageId);
    }
}
