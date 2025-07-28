package com.noir.restaurant.controllers;

import com.noir.restaurant.domain.RestaurantCreateUpdateRequest;
import com.noir.restaurant.domain.dtos.RestaurantCreateUpdateRequestDto;
import com.noir.restaurant.domain.dtos.RestaurantDto;
import com.noir.restaurant.domain.dtos.RestaurantSummaryDto;
import com.noir.restaurant.domain.entities.Restaurant;
import com.noir.restaurant.mappers.RestaurantMapper;
import com.noir.restaurant.services.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final RestaurantMapper restaurantMapper;

    @PostMapping
    public ResponseEntity<RestaurantDto> createRestaurant(
            @Valid @RequestBody RestaurantCreateUpdateRequestDto request) {
        RestaurantCreateUpdateRequest createUpdateRequest = restaurantMapper.toRestaurantCreateUpdateRequest(
                request);
        Restaurant restaurant = restaurantService.createRestaurant(createUpdateRequest);
        RestaurantDto createdRestaurantDto = restaurantMapper.toRestaurantDto(restaurant);
        return ResponseEntity.ok(createdRestaurantDto);
    }

    @GetMapping
    public Page<RestaurantSummaryDto> searchRestaurants(@RequestParam(required = false) String q,
                                                        @RequestParam(required = false) Float minRating,
                                                        @RequestParam(required = false) Float latitude,
                                                        @RequestParam(required = false) Float longitude,
                                                        @RequestParam(required = false) Float radius,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        Page<Restaurant> searchReuslt = restaurantService.searchRestaurants(
                q, minRating, latitude, longitude, radius, PageRequest.of(page - 1, size));

        return searchReuslt.map(restaurantMapper::toSummaryDto);
    }
}
