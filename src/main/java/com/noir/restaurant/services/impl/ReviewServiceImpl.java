package com.noir.restaurant.services.impl;

import com.noir.restaurant.domain.ReviewCreateUpdateRequest;
import com.noir.restaurant.domain.entities.Photo;
import com.noir.restaurant.domain.entities.Restaurant;
import com.noir.restaurant.domain.entities.Review;
import com.noir.restaurant.domain.entities.User;
import com.noir.restaurant.exceptions.RestaurantNotFoundException;
import com.noir.restaurant.exceptions.ReviewNotAllowedException;
import com.noir.restaurant.repositories.RestaurantRepository;
import com.noir.restaurant.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final RestaurantRepository restaurantRepository;

    private static Optional<Review> getReviewFromRestaurant(String reviewId,
                                                            Restaurant restaurant) {
        return restaurant.getReviews().stream().filter(r -> r.getId().equals(reviewId)).findFirst();
    }

    @Override
    public Review createReview(User author, String restaurantId, ReviewCreateUpdateRequest review) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);

        boolean hasExistingReview = restaurant
                .getReviews()
                .stream()
                .anyMatch(r -> r.getWrittenBy().getId().equals(author.getId()));

        if (hasExistingReview) {
            throw new ReviewNotAllowedException(
                    "User has already written a review for this restaurant.");
        }

        LocalDateTime now = LocalDateTime.now();

        List<Photo> photos = review.getPhotoIds().stream().map(url -> Photo.builder()
                                                                           .url(url)
                                                                           .uploadDate(now)
                                                                           .build()).toList();

        String reviewId = UUID.randomUUID().toString();

        Review reviewToCreate = Review.builder()
                                      .id(reviewId)
                                      .content(review.getContent())
                                      .rating(review.getRating())
                                      .photos(photos)
                                      .datePosted(now)
                                      .lastEdited(now)
                                      .writtenBy(author)
                                      .build();

        restaurant.getReviews().add(reviewToCreate);

        updateRestaurantAverageRating(restaurant);

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        return savedRestaurant
                .getReviews()
                .stream()
                .filter(r -> reviewId.equals(r.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Error retrieving created review"));
    }

    @Override
    public Page<Review> listReviews(String restaurantId, Pageable pageable) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);
        List<Review> reviews = restaurant.getReviews();

        Sort sort = pageable.getSort();
        if (sort.isSorted()) {
            Sort.Order order = sort.iterator().next();
            String property = order.getProperty();
            boolean isAscending = order.getDirection().isAscending();

//            Comparator<Review> comparator = switch (property) {
//                case "rating" -> Comparator.comparing(Review::getRating);
//                default -> Comparator.comparing(Review::getDatePosted);
//            };
            Comparator<Review> comparator = "rating".equals(property)
                    ? Comparator.comparing(Review::getRating)
                    : Comparator.comparing(Review::getDatePosted);

            reviews.sort(isAscending ? comparator : comparator.reversed());
        } else {
            reviews.sort(Comparator.comparing(Review::getDatePosted).reversed());
        }

        int start = (int) pageable.getOffset();
        if (start >= reviews.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, reviews.size());
        }

        int end = Math.min(start + pageable.getPageSize(), reviews.size());
        return new PageImpl<>(reviews.subList(start, end), pageable, reviews.size());
    }

    @Override
    public Optional<Review> getReview(String restaurantId, String reviewId) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);
        return getReviewFromRestaurant(reviewId, restaurant);
    }

    @Override
    public Review updateReview(User author, String restaurantId, String reviewId,
                               ReviewCreateUpdateRequest review) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);

        String authorId = author.getId();

        Review existingReview = getReviewFromRestaurant(reviewId, restaurant).orElseThrow(
                () -> new ReviewNotAllowedException("Review does not exist"));

        if (!existingReview.getWrittenBy().getId().equals(authorId)) {
            throw new ReviewNotAllowedException("Cannot update another user's review.");
        }

        if (LocalDateTime.now().isAfter(existingReview.getDatePosted().plusHours(48))) {
            throw new ReviewNotAllowedException("Review can no longer be updated.");
        }

        existingReview.setContent(review.getContent());
        existingReview.setRating(review.getRating());
        existingReview.setLastEdited(LocalDateTime.now());
        existingReview.setPhotos(review.getPhotoIds()
                                       .stream()
                                       .map(photoId -> Photo
                                               .builder()
                                               .url(photoId)
                                               .uploadDate(LocalDateTime.now())
                                               .build())
                                       .toList());

        List<Review> updatedReviews = restaurant
                .getReviews()
                .stream()
                .filter(r -> !r.getId().equals(reviewId))
                .collect(Collectors.toList());
        updatedReviews.add(existingReview);

        restaurant.setReviews(updatedReviews);

        updateRestaurantAverageRating(restaurant);

        restaurantRepository.save(restaurant);

        return existingReview;
    }

    @Override
    public void deleteReview(User author, String restaurantId, String reviewId) {
        Restaurant restaurant = getRestaurantOrThrow(restaurantId);

        Review existingReview = getReviewFromRestaurant(reviewId, restaurant).orElseThrow(
                () -> new ReviewNotAllowedException("Review does not exist"));

        if (!existingReview.getWrittenBy().getId().equals(author.getId())) {
            throw new ReviewNotAllowedException("Cannot delete another user's review.");
        }

        List<Review> filteredReviews = restaurant
                .getReviews()
                .stream()
                .filter(r -> !r.getId().equals(reviewId))
                .toList();

        restaurant.setReviews(filteredReviews);

        updateRestaurantAverageRating(restaurant);

        restaurantRepository.save(restaurant);
    }

    private Restaurant getRestaurantOrThrow(String restaurantId) {
        return restaurantRepository
                .findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(
                        "Restaurant with id " + restaurantId + " not found."));
    }

    private void updateRestaurantAverageRating(Restaurant restaurant) {
        List<Review> reviews = restaurant.getReviews();
        if (reviews.isEmpty()) {
            restaurant.setAverageRating(0.0f);
        } else {
            double averageRating = reviews.stream()
                                          .mapToDouble(Review::getRating)
                                          .average()
                                          .orElse(0.0);

            restaurant.setAverageRating((float) averageRating);
        }
    }
}
