package com.quickbite.orderservice.dto.internal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ValidatedItemDto(
        Long id,
        @JsonAlias({"restaurantId"}) Long restaurantId,
        @JsonAlias({"restaurantName"}) String restaurantName,
        Long categoryId,
        String categoryName,
        String name,
        @JsonAlias({"price", "originalPrice"}) double originalPrice,
        Double discountPercent,
        double effectivePrice,
        boolean available,
        @JsonAlias({"categoryActive", "active"}) boolean active
) {}

