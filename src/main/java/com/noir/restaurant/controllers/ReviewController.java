package com.noir.restaurant.controllers;

import com.noir.restaurant.domain.ReviewCreateUpdateRequest;
import com.noir.restaurant.domain.dtos.ReviewCreateUpdateRequestDto;
import com.noir.restaurant.domain.dtos.ReviewDto;
import com.noir.restaurant.domain.entities.Review;
import com.noir.restaurant.domain.entities.User;
import com.noir.restaurant.mappers.ReviewMapper;
import com.noir.restaurant.services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewMapper reviewMapper;
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDto> createReview(@PathVariable String restaurantId,
                                                  @Valid @RequestBody ReviewCreateUpdateRequestDto review,
                                                  @AuthenticationPrincipal Jwt jwt) {
        ReviewCreateUpdateRequest reviewCreateUpdateRequest = reviewMapper.toReviewCreateUpdateRequest(
                review);

        User user = jwtToUser(jwt);

        Review createdReview = reviewService.createReview(user, restaurantId,
                                                          reviewCreateUpdateRequest);

        return ResponseEntity.ok(reviewMapper.toDto(createdReview));
    }

    @GetMapping
    public Page<ReviewDto> listReviews(@PathVariable String restaurantId,
                                       @PageableDefault(size = 20, page = 0, sort = "datePosted",
                                               direction = Sort.Direction.DESC
                                       ) Pageable pageable) {
        return reviewService
                .listReviews(restaurantId, pageable)
                .map(reviewMapper::toDto);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> getReview(@PathVariable String restaurantId,
                                               @PathVariable String reviewId) {
        return reviewService.getReview(restaurantId, reviewId)
                            .map(reviewMapper::toDto)
                            .map(ResponseEntity::ok)
                            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable String restaurantId,
                                                  @PathVariable String reviewId,
                                                  @Valid @RequestBody ReviewCreateUpdateRequestDto review,
                                                  @AuthenticationPrincipal Jwt jwt) {
        ReviewCreateUpdateRequest reviewCreateUpdateRequest = reviewMapper.toReviewCreateUpdateRequest(
                review);

        User user = jwtToUser(jwt);

        Review updatedReview = reviewService.updateReview(user, restaurantId, reviewId,
                                                          reviewCreateUpdateRequest);

        return ResponseEntity.ok(reviewMapper.toDto(updatedReview));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable String restaurantId,
                                             @PathVariable String reviewId,
                                             @AuthenticationPrincipal Jwt jwt) {
        reviewService.deleteReview(jwtToUser(jwt), restaurantId, reviewId);
        return ResponseEntity.noContent().build();
    }

    private User jwtToUser(Jwt jwt) {
        return User.builder()
                   .id(jwt.getSubject())
                   .username(jwt.getClaimAsString("preferred_username"))
                   .givenName(jwt.getClaimAsString("given_name"))
                   .familyName(jwt.getClaimAsString("family_name"))
                   .build();
    }
}
