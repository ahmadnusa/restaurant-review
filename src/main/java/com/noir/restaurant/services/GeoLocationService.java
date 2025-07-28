package com.noir.restaurant.services;

import com.noir.restaurant.domain.GeoLocation;
import com.noir.restaurant.domain.entities.Address;

public interface GeoLocationService {
    GeoLocation geoLocate(Address address);
}
