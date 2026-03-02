package org.example.promotion.repository;

import org.example.promotion.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

        Optional<Coupon> findByCode(String code);

        @Modifying
        @Query("UPDATE Coupon c SET c.currentUses = c.currentUses + :delta, c.version = c.version + 1 WHERE c.id = :id")
        int incrementCurrentUses(@Param("id") Long id, @Param("delta") int delta);

        @Modifying
        // Atomic decrement currentUses
        @Query("UPDATE Coupon c SET c.currentUses = c.currentUses - 1, c.version = c.version + 1 " +
                        "WHERE c.id = :id AND c.currentUses > 0")
        int decrementCurrentUses(@Param("id") Long id);
}
