package com.gr6.SmartCart.module_v3.recommendation.ai;

import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.enums.CategoryStatus;
import com.gr6.SmartCart.common.enums.OrderStatus;
import com.gr6.SmartCart.common.enums.ProductStatus;
import com.gr6.SmartCart.common.enums.ShopStatus;
import com.gr6.SmartCart.common.enums.VariantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AiProductRepository extends JpaRepository<Product, Long> {

    @Query("""
            select distinct p
            from Product p
            join fetch p.shop s
            join fetch p.category c
            left join fetch p.variants v
            where p.status = :productStatus
              and s.status = :shopStatus
              and c.categoryStatus = :categoryStatus
              and exists (
                    select 1
                    from ProductVariant pv
                    where pv.product = p
                      and pv.status = :variantStatus
                      and coalesce(pv.stockQuantity, 0) > 0
              )
            """)
    List<Product> findActiveProducts(
            @Param("productStatus") ProductStatus productStatus,
            @Param("shopStatus") ShopStatus shopStatus,
            @Param("categoryStatus") CategoryStatus categoryStatus,
            @Param("variantStatus") VariantStatus variantStatus
    );

    @Query("""
            select distinct p
            from Product p
            join fetch p.shop s
            join fetch p.category c
            left join fetch p.variants v
            where p.status = :productStatus
              and s.status = :shopStatus
              and c.categoryStatus = :categoryStatus
              and p.productId <> :productId
              and c.categoryId = :categoryId
              and exists (
                    select 1
                    from ProductVariant pv
                    where pv.product = p
                      and pv.status = :variantStatus
                      and coalesce(pv.stockQuantity, 0) > 0
              )
            order by coalesce(p.soldCount, 0) desc, p.productId desc
            """)
    List<Product> findSameCategoryCandidates(
            @Param("productId") Long productId,
            @Param("categoryId") Long categoryId,
            @Param("productStatus") ProductStatus productStatus,
            @Param("shopStatus") ShopStatus shopStatus,
            @Param("categoryStatus") CategoryStatus categoryStatus,
            @Param("variantStatus") VariantStatus variantStatus
    );

    @Query("""
            select r.product.productId, avg(r.rating), count(r.reviewId)
            from Review r
            group by r.product.productId
            """)
    List<Object[]> findRatingStats();

    @Query("""
            select v.product.productId, min(v.price), max(v.price)
            from ProductVariant v
            where v.status = :status
            group by v.product.productId
            """)
    List<Object[]> findPriceRanges(@Param("status") VariantStatus status);

    @Query("""
            select oi.variant.product.productId, coalesce(sum(oi.quantity), 0)
            from OrderItem oi
            join oi.shopOrder so
            where so.status in :statuses
            group by oi.variant.product.productId
            """)
    List<Object[]> findSoldStats(@Param("statuses") Collection<OrderStatus> statuses);

    @Query("""
            select distinct p
            from CartItem ci
            join ci.variant v
            join v.product p
            join fetch p.shop s
            join fetch p.category c
            where ci.user.email = :email
              and p.status = :status
            """)
    List<Product> findCartSeedProducts(
            @Param("email") String email,
            @Param("status") ProductStatus status
    );

    @Query("""
            select distinct p
            from OrderItem oi
            join oi.variant v
            join v.product p
            join oi.shopOrder so
            join so.order o
            join fetch p.shop s
            join fetch p.category c
            where o.user.email = :email
              and so.status in :statuses
              and p.status = :status
            """)
    List<Product> findOrderSeedProducts(
            @Param("email") String email,
            @Param("statuses") Collection<OrderStatus> statuses,
            @Param("status") ProductStatus status
    );
}