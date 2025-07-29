package com.noir.restaurant.mappers;

import com.noir.restaurant.domain.ReviewCreateUpdateRequest;
import com.noir.restaurant.domain.dtos.ReviewCreateUpdateRequestDto;
import com.noir.restaurant.domain.dtos.ReviewDto;
import com.noir.restaurant.domain.entities.Review;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

    ReviewCreateUpdateRequest toReviewCreateUpdateRequest(ReviewCreateUpdateRequestDto dto);

    ReviewDto toDto(Review review);
}
