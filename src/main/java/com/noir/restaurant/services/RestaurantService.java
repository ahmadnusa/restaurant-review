package com.noir.restaurant.services;

import com.noir.restaurant.domain.RestaurantCreateUpdateRequest;
import com.noir.restaurant.domain.entities.Restaurant;

public interface RestaurantService {
    Restaurant createRestaurant(RestaurantCreateUpdateRequest request);
}
