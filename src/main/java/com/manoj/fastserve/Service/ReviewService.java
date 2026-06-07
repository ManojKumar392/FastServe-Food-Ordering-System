package com.manoj.fastserve.Service;

import com.manoj.fastserve.DTO.CreateReviewRequest;
import com.manoj.fastserve.Entity.Restaurant;
import com.manoj.fastserve.Entity.Review;
import com.manoj.fastserve.Entity.User;
import com.manoj.fastserve.Exception.BadRequestException;
import com.manoj.fastserve.Exception.ResourceNotFoundException;
import com.manoj.fastserve.Repository.RestaurantRepository;
import com.manoj.fastserve.Repository.ReviewRepository;
import com.manoj.fastserve.Repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            RestaurantRepository restaurantRepository,
            UserRepository userRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    public Review addReview(
            Long restaurantId,
            CreateReviewRequest request
    ) {

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Restaurant not found"));

        User user = getCurrentUser();

        if (reviewRepository
                .findByRestaurantAndUser(restaurant, user)
                .isPresent()) {

            throw new BadRequestException(
                    "User has already reviewed this restaurant"
            );
        }

        Review review = new Review();

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setRestaurant(restaurant);
        review.setUser(user);

        return reviewRepository.save(review);
    }

    public List<Review> getReviews(Long restaurantId) {

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Restaurant not found"));

        return reviewRepository.findByRestaurant(restaurant);
    }

    public Double getAverageRating(Long restaurantId) {

        List<Review> reviews = getReviews(restaurantId);

        if (reviews.isEmpty()) {
            return 0.0;
        }

        double total = reviews.stream()
                .mapToInt(Review::getRating)
                .sum();

        return total / reviews.size();
    }

    private User getCurrentUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }
}