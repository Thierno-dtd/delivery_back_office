package com.delivery.delivery_api.rating.controller;

import com.delivery.delivery_api.rating.dto.request.CreateRatingRequest;
import com.delivery.delivery_api.rating.dto.response.RatingResponse;
import com.delivery.delivery_api.rating.service.IRatingService;
import com.delivery.delivery_api.shared.response.ApiResponse;
import com.delivery.delivery_api.shared.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/ratings")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Notations", description = "Système de notation client ↔ livreur")
public class RatingController {

    private final IRatingService ratingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DRIVER')")
    @Operation(
            summary = "Donner une note",
            description = "Customer note le Driver et inversement. " +
                    "Une seule note par commande par utilisateur. " +
                    "Commande doit être en statut DELIVERED."
    )
    public ResponseEntity<ApiResponse<RatingResponse>> create(
            @Valid @RequestBody CreateRatingRequest request) {
        RatingResponse response = ratingService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Note enregistrée avec succès", response));
    }

    @GetMapping("/order/{orderUuid}")
    @Operation(summary = "Notes d'une commande")
    public ResponseEntity<ApiResponse<List<RatingResponse>>> findByOrder(
            @PathVariable String orderUuid) {
        return ResponseEntity.ok(
                ApiResponse.success(ratingService.findByOrder(orderUuid)));
    }

    @GetMapping("/user/{userUuid}")
    @Operation(summary = "Notes reçues par un utilisateur avec pagination")
    public ResponseEntity<ApiResponse<PageResponse<RatingResponse>>> findByRated(
            @PathVariable String userUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(ratingService.findByRated(userUuid, page, size)));
    }

    @GetMapping("/user/{userUuid}/average")
    @Operation(summary = "Note moyenne d'un utilisateur")
    public ResponseEntity<ApiResponse<Double>> getAverageScore(
            @PathVariable String userUuid) {
        return ResponseEntity.ok(
                ApiResponse.success(ratingService.getAverageScore(userUuid)));
    }

    @GetMapping("/user/{userUuid}/total")
    @Operation(summary = "Nombre total de notes reçues par un utilisateur")
    public ResponseEntity<ApiResponse<Long>> getTotalRatings(
            @PathVariable String userUuid) {
        return ResponseEntity.ok(
                ApiResponse.success(ratingService.getTotalRatings(userUuid)));
    }
}