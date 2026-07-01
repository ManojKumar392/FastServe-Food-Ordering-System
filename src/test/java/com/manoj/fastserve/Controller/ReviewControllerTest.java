package com.manoj.fastserve.Controller;


import com.manoj.fastserve.Config.TestCacheConfig;
import com.manoj.fastserve.DTO.CreateReviewRequest;
import com.manoj.fastserve.Entity.Review;
import com.manoj.fastserve.Service.ReviewService;

import com.manoj.fastserve.Util.JwtUtil;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.util.List;


import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@Import(TestCacheConfig.class)
@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {


    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void addReview_shouldCreateReview() throws Exception {


        Review review = new Review();

        review.setRating(5);
        review.setComment("Great food");


        when(reviewService.addReview(
                eq(1L),
                any(CreateReviewRequest.class)
        ))
                .thenReturn(review);



        mockMvc.perform(
                        post("/restaurants/1/reviews")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                          "rating":5,
                          "comment":"Great food"
                        }
                        """)
                )
                .andExpect(status().isOk());

    }





    @Test
    void getReviews_shouldReturnReviews() throws Exception {


        Review review = new Review();

        review.setRating(4);
        review.setComment("Good");



        when(reviewService.getReviews(1L))
                .thenReturn(
                        List.of(review)
                );



        mockMvc.perform(
                        get("/restaurants/1/reviews")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(1));

    }





    @Test
    void getAverageRating_shouldReturnRating() throws Exception {



        when(reviewService.getAverageRating(1L))
                .thenReturn(4.5);



        mockMvc.perform(
                        get("/restaurants/1/average-rating")
                )
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("4.5"));

    }

}