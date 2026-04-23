package com.quickbite.reviewservice.controller;

import com.quickbite.reviewservice.dto.ReviewRequest;
import com.quickbite.reviewservice.dto.ReviewResponse;
import com.quickbite.reviewservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> all() {
        return ResponseEntity.ok(reviewService.all());
    }

    @GetMapping("/customer/{email}")
    public ResponseEntity<List<ReviewResponse>> customer(@PathVariable String email) {
        return ResponseEntity.ok(reviewService.forCustomer(email));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<List<ReviewResponse>> restaurant(@PathVariable Long restaurantId) {
        return ResponseEntity.ok(reviewService.forRestaurant(restaurantId));
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> create(@Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
