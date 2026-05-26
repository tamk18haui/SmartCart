package com.gr6.SmartCart.module_v3.recommendation.event;

import com.gr6.SmartCart.common.domain.UserProductEvent;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.RecommendationEventType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserProductEventRepository extends JpaRepository<UserProductEvent, Long> {

    @Query("""
            select e
            from UserProductEvent e
            left join fetch e.product p
            left join fetch p.shop s
            left join fetch p.category c
            where e.user.email = :email
              and e.eventType in :types
              and (
                    e.product is null
                    or (
                        p.status = :productStatus
                        and s.status = com.gr6.SmartCart.common.enums.ShopStatus.ACTIVE
                        and c.categoryStatus = com.gr6.SmartCart.common.enums.CategoryStatus.ACTIVE
                    )
              )
            order by e.createdAt desc
            """)
    List<UserProductEvent> findRecentEvents(
            @Param("email") String email,
            @Param("types") List<RecommendationEventType> types,
            @Param("productStatus") ProductStatus productStatus,
            Pageable pageable
    );

    @Query("""
            select e
            from UserProductEvent e
            where e.user.email = :email
              and e.eventType = :type
              and e.keyword is not null
              and e.keyword <> ''
            order by e.createdAt desc
            """)
    List<UserProductEvent> findRecentSearchEvents(
            @Param("email") String email,
            @Param("type") RecommendationEventType type,
            Pageable pageable
    );
}