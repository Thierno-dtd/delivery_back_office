package com.delivery.delivery_api.rating.repository;

import com.delivery.delivery_api.rating.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    boolean existsByOrderIdAndRaterId(Long orderId, Long raterId);

    List<Rating> findByOrderId(Long orderId);

    Page<Rating> findByRatedId(Long ratedId, Pageable pageable);

    @Query("SELECT COALESCE(AVG(r.score), 0.0) FROM Rating r WHERE r.rated.id = :ratedId")
    Double getAverageScoreByRatedId(@Param("ratedId") Long ratedId);

    @Query("SELECT COALESCE(AVG(r.score), 0.0) FROM Rating r " +
            "WHERE r.rated.id = :ratedId AND r.rater.id != :ratedId")
    Double getAverageScoreAsDriver(@Param("ratedId") Long ratedId);

    long countByRatedId(Long ratedId);

    Optional<Rating> findByOrderIdAndRaterId(Long orderId, Long raterId);
}