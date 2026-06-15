package com.manoj.fastserve.Service;


import com.manoj.fastserve.DTO.CreateReviewRequest;
import com.manoj.fastserve.Entity.*;
import com.manoj.fastserve.Exception.BadRequestException;
import com.manoj.fastserve.Exception.ResourceNotFoundException;
import com.manoj.fastserve.Repository.RestaurantRepository;
import com.manoj.fastserve.Repository.ReviewRepository;
import com.manoj.fastserve.Repository.UserRepository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.List;
import java.util.Optional;


import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


import static org.junit.jupiter.api.Assertions.*;



class ReviewServiceTest {


    @Mock
    private ReviewRepository reviewRepository;


    @Mock
    private RestaurantRepository restaurantRepository;


    @Mock
    private UserRepository userRepository;



    private ReviewService reviewService;



    private User user;

    private Restaurant restaurant;



    @BeforeEach
    void setup(){


        MockitoAnnotations.openMocks(this);


        reviewService =
                new ReviewService(
                        reviewRepository,
                        restaurantRepository,
                        userRepository
                );



        user = new User();

        user.setEmail("test@gmail.com");



        restaurant = new Restaurant();

        restaurant.setId(1L);



        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "test@gmail.com",
                                null
                        )
                );

    }




    @Test
    void addReview_success(){


        CreateReviewRequest request =
                new CreateReviewRequest();

        request.setRating(5);
        request.setComment("Amazing");



        when(restaurantRepository.findById(1L))
                .thenReturn(
                        Optional.of(restaurant)
                );


        when(userRepository.findByEmail(
                "test@gmail.com"
        ))
                .thenReturn(
                        Optional.of(user)
                );


        when(reviewRepository.findByRestaurantAndUser(
                restaurant,
                user
        ))
                .thenReturn(
                        Optional.empty()
                );


        Review saved =
                new Review();

        saved.setRating(5);
        saved.setComment("Amazing");



        when(reviewRepository.save(any(Review.class)))
                .thenReturn(saved);



        Review result =
                reviewService.addReview(
                        1L,
                        request
                );



        assertEquals(
                5,
                result.getRating()
        );


        assertEquals(
                "Amazing",
                result.getComment()
        );

    }






    @Test
    void addReview_restaurantNotFound(){


        when(restaurantRepository.findById(1L))
                .thenReturn(Optional.empty());



        CreateReviewRequest request =
                new CreateReviewRequest();



        assertThrows(
                ResourceNotFoundException.class,
                () ->
                        reviewService.addReview(
                                1L,
                                request
                        )
        );

    }







    @Test
    void addReview_duplicateReview(){


        CreateReviewRequest request =
                new CreateReviewRequest();



        when(restaurantRepository.findById(1L))
                .thenReturn(
                        Optional.of(restaurant)
                );



        when(userRepository.findByEmail(
                "test@gmail.com"
        ))
                .thenReturn(
                        Optional.of(user)
                );



        when(reviewRepository.findByRestaurantAndUser(
                restaurant,
                user
        ))
                .thenReturn(
                        Optional.of(new Review())
                );



        assertThrows(
                BadRequestException.class,
                () ->
                        reviewService.addReview(
                                1L,
                                request
                        )
        );

    }







    @Test
    void getReviews_success(){


        when(restaurantRepository.findById(1L))
                .thenReturn(
                        Optional.of(restaurant)
                );


        List<Review> reviews =
                List.of(
                        new Review(),
                        new Review()
                );


        when(reviewRepository.findByRestaurant(
                restaurant
        ))
                .thenReturn(reviews);



        List<Review> result =
                reviewService.getReviews(1L);



        assertEquals(
                2,
                result.size()
        );

    }







    @Test
    void getAverageRating_success(){


        when(restaurantRepository.findById(1L))
                .thenReturn(
                        Optional.of(restaurant)
                );



        Review r1 = new Review();
        r1.setRating(5);


        Review r2 = new Review();
        r2.setRating(3);



        when(reviewRepository.findByRestaurant(
                restaurant
        ))
                .thenReturn(
                        List.of(r1,r2)
                );



        Double result =
                reviewService.getAverageRating(1L);



        assertEquals(
                4.0,
                result
        );

    }







    @Test
    void getAverageRating_noReviews(){


        when(restaurantRepository.findById(1L))
                .thenReturn(
                        Optional.of(restaurant)
                );


        when(reviewRepository.findByRestaurant(
                restaurant
        ))
                .thenReturn(
                        List.of()
                );



        Double result =
                reviewService.getAverageRating(1L);



        assertEquals(
                0.0,
                result
        );

    }


}