package com.manoj.fastserve.Controller;

import com.manoj.fastserve.DTO.CreateReviewRequest;
import com.manoj.fastserve.Entity.Review;
import com.manoj.fastserve.Service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/{restaurantId}/reviews")
    public ResponseEntity<Review> addReview(
            @PathVariable Long restaurantId,
            @Valid @RequestBody CreateReviewRequest request
    ) {

        return ResponseEntity.ok(
                reviewService.addReview(
                        restaurantId,
                        request
                )
        );
    }

    @GetMapping("/{restaurantId}/reviews")
    public ResponseEntity<List<Review>> getReviews(
            @PathVariable Long restaurantId
    ) {

        return ResponseEntity.ok(
                reviewService.getReviews(restaurantId)
        );
    }

    @GetMapping("/{restaurantId}/average-rating")
    public ResponseEntity<Double> getAverageRating(
            @PathVariable Long restaurantId
    ) {

        return ResponseEntity.ok(
                reviewService.getAverageRating(restaurantId)
        );
    }
}