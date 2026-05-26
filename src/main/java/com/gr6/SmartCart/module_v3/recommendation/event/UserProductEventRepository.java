package com.gr6.SmartCart.module_v3.recommendation.event;

import com.gr6.SmartCart.common.domain.Product;
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
            select p
            from UserProductEvent e
            join e.product p
            join fetch p.shop s
            join fetch p.category c
            where e.user.email = :email
              and p.status = :productStatus
              and e.eventType in :types
            order by e.createdAt desc
            """)
    List<Product> findRecentSeedProducts(
            @Param("email") String email,
            @Param("types") List<RecommendationEventType> types,
            @Param("productStatus") ProductStatus productStatus,
            Pageable pageable
    );

    @Query("""
            select e.keyword
            from UserProductEvent e
            where e.user.email = :email
              and e.eventType = com.gr6.SmartCart.common.enums.RecommendationEventType.SEARCH
              and e.keyword is not null
              and e.keyword <> ''
            order by e.createdAt desc
            """)
    List<String> findRecentSearchKeywords(
            @Param("email") String email,
            Pageable pageable
    );
}