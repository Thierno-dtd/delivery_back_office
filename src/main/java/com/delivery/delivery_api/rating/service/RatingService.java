package com.delivery.delivery_api.rating.service;

import com.delivery.delivery_api.order.entity.Order;
import com.delivery.delivery_api.order.enums.OrderStatus;
import com.delivery.delivery_api.order.repository.OrderRepository;
import com.delivery.delivery_api.rating.dto.request.CreateRatingRequest;
import com.delivery.delivery_api.rating.dto.response.RatingResponse;
import com.delivery.delivery_api.rating.entity.Rating;
import com.delivery.delivery_api.rating.repository.RatingRepository;
import com.delivery.delivery_api.shared.audit.AuditClient;
import com.delivery.delivery_api.shared.exception.BusinessException;
import com.delivery.delivery_api.shared.exception.ConflictException;
import com.delivery.delivery_api.shared.exception.ResourceNotFoundException;
import com.delivery.delivery_api.shared.response.PageResponse;
import com.delivery.delivery_api.shared.utils.PaginationUtil;
import com.delivery.delivery_api.user.entity.User;
import com.delivery.delivery_api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RatingService implements IRatingService {

    private final RatingRepository ratingRepository;
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final AuditClient auditClient;

    @Override
    @Transactional
    public RatingResponse create(CreateRatingRequest request) {
        User rater = userService.getAuthenticatedUser();

        Order order = orderRepository.findByUuid(request.getOrderUuid())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Commande", "uuid", request.getOrderUuid()));

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException(
                    "Vous ne pouvez noter qu'une commande livrée",
                    "ORDER_NOT_DELIVERED",
                    HttpStatus.BAD_REQUEST
            );
        }

        boolean isCustomer = order.getCustomer().getUser().getId().equals(rater.getId());
        boolean isDriver = order.getDriver() != null &&
                order.getDriver().getUser().getId().equals(rater.getId());

        if (!isCustomer && !isDriver) {
            throw new BusinessException(
                    "Vous n'êtes pas autorisé à noter cette commande",
                    "NOT_INVOLVED_IN_ORDER",
                    HttpStatus.FORBIDDEN
            );
        }

        if (ratingRepository.existsByOrderIdAndRaterId(order.getId(), rater.getId())) {
            throw new ConflictException(
                    "Vous avez déjà noté cette commande");
        }

        User rated;
        if (isCustomer) {
            if (order.getDriver() == null) {
                throw new BusinessException(
                        "Impossible de noter — aucun livreur assigné à cette commande",
                        "NO_DRIVER_ASSIGNED",
                        HttpStatus.BAD_REQUEST
                );
            }
            rated = order.getDriver().getUser();
        } else {
            rated = order.getCustomer().getUser();
        }

        Rating rating = Rating.builder()
                .order(order)
                .rater(rater)
                .rated(rated)
                .score(request.getScore())
                .comment(request.getComment())
                .build();

        rating = ratingRepository.save(rating);

        auditClient.log(
                "RATING_CREATED",
                getConnectedUserEmail(),
                String.format("Note %d/5 donnée sur commande %s",
                        request.getScore(), order.getOrderCode()),
                null
        );

        log.info("Note {} créée par {} pour commande {}",
                request.getScore(), rater.getEmail(), order.getOrderCode());

        return toResponse(rating);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatingResponse> findByOrder(String orderUuid) {
        Order order = orderRepository.findByUuid(orderUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Commande", "uuid", orderUuid));
        return ratingRepository.findByOrderId(order.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RatingResponse> findByRated(String userUuid, int page, int size) {
        User user = userService.findByUuidOrThrow(userUuid);
        Page<Rating> ratings = ratingRepository.findByRatedId(
                user.getId(), PaginationUtil.build(page, size));
        List<RatingResponse> content = ratings.getContent().stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.from(ratings, content);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageScore(String userUuid) {
        User user = userService.findByUuidOrThrow(userUuid);
        return ratingRepository.getAverageScoreByRatedId(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalRatings(String userUuid) {
        User user = userService.findByUuidOrThrow(userUuid);
        return ratingRepository.countByRatedId(user.getId());
    }

    private String getConnectedUserEmail() {
        try {
            return SecurityContextHolder.getContext()
                    .getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }

    private RatingResponse toResponse(Rating rating) {
        return RatingResponse.builder()
                .id(rating.getId())
                .orderUuid(rating.getOrder().getUuid())
                .orderCode(rating.getOrder().getOrderCode())
                .raterUuid(rating.getRater().getUuid())
                .raterName(rating.getRater().getEmail())
                .ratedUuid(rating.getRated().getUuid())
                .ratedName(rating.getRated().getEmail())
                .score(rating.getScore())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}