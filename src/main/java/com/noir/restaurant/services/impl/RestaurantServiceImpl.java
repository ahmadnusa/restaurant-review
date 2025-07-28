package com.noir.restaurant.services.impl;

import com.noir.restaurant.domain.GeoLocation;
import com.noir.restaurant.domain.RestaurantCreateUpdateRequest;
import com.noir.restaurant.domain.entities.Address;
import com.noir.restaurant.domain.entities.Photo;
import com.noir.restaurant.domain.entities.Restaurant;
import com.noir.restaurant.repositories.RestaurantRepository;
import com.noir.restaurant.services.GeoLocationService;
import com.noir.restaurant.services.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final GeoLocationService geoLocationService;

    @Override
    public Restaurant createRestaurant(RestaurantCreateUpdateRequest request) {
        Address address = request.getAddress();
        GeoLocation geoLocation = geoLocationService.geoLocate(address);
        GeoPoint geoPoint = new GeoPoint(geoLocation.getLatitude(), geoLocation.getLongitude());

        List<String> photoIds = request.getPhotoIds();
        List<Photo> photos = photoIds
                .stream()
                .map(photoUrl -> Photo
                        .builder()
                        .url(photoUrl)
                        .uploadDate(LocalDateTime.now())
                        .build())
                .toList();

        Restaurant restaurant = Restaurant
                .builder()
                .name(request.getName())
                .cuisineType(request.getCuisineType())
                .contactInformation(request.getContactInformation())
                .address(address)
                .geoLocation(geoPoint)
                .operatingHours(request.getOperatingHours())
                .averageRating(0f)
                .photos(photos)
                .build();
        return restaurantRepository.save(restaurant);
    }

    @Override
    public Page<Restaurant> searchRestaurants(String query, Float minRating, Float latitude,
                                              Float longitude, Float radius, Pageable pageable) {

        if (null != minRating && (null == query || query.isEmpty())) {
            return restaurantRepository.findByAverageRatingGreaterThanEqual(minRating, pageable);
        }

        Float searchMinRating = minRating == null ? 0f : minRating;

        if (null != query && !query.trim().isEmpty()) {
            return restaurantRepository.findByQueryAndMinRating(query, searchMinRating, pageable);
        }

        if (null != latitude && null != longitude && null != radius) {
            return restaurantRepository.findByLocationNear(latitude, longitude, radius, pageable);
        }

        return restaurantRepository.findAll(pageable);
    }
}
