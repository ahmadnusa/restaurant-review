package com.noir.restaurant.services;

import com.noir.restaurant.domain.ReviewCreateUpdateRequest;
import com.noir.restaurant.domain.entities.Review;
import com.noir.restaurant.domain.entities.User;

public interface ReviewService {

    Review createReview(User author, String restaurantId, ReviewCreateUpdateRequest review);
}
