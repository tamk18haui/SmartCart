package com.gr6.SmartCart.module_v3.recommendation.event;

import com.gr6.SmartCart.common.domain.Product;
import com.gr6.SmartCart.common.domain.User;
import com.gr6.SmartCart.common.domain.UserProductEvent;
import com.gr6.SmartCart.common.enums.RecommendationEventType;
import com.gr6.SmartCart.modules.identity.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationEventService {

    private final UserProductEventRepository eventRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAddToCart(User user, Product product, int quantity) {
        recordProductEvent(user, product, RecommendationEventType.ADD_TO_CART, quantity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordPurchase(User user, Product product, int quantity) {
        recordProductEvent(user, product, RecommendationEventType.PURCHASE, quantity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordViewProductByEmail(String email, Product product) {
        try {
            if (email == null || email.isBlank()) return;
            if (product == null || product.getProductId() == null) return;

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null || user.getUserId() == null) return;

            recordProductEvent(user, product, RecommendationEventType.VIEW_PRODUCT, 1);
        } catch (Exception e) {
            log.warn("Cannot record view product event: {}", e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSearch(String email, String keyword) {
        try {
            if (email == null || email.isBlank()) return;
            if (keyword == null || keyword.isBlank()) return;

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null || user.getUserId() == null) return;

            UserProductEvent event = new UserProductEvent();
            event.setUser(entityManager.getReference(User.class, user.getUserId()));
            event.setEventType(RecommendationEventType.SEARCH);
            event.setKeyword(keyword.trim());
            event.setQuantity(1);

            eventRepository.save(event);
        } catch (Exception e) {
            log.warn("Cannot record search recommendation event: {}", e.getMessage());
        }
    }

    private void recordProductEvent(
            User user,
            Product product,
            RecommendationEventType eventType,
            int quantity
    ) {
        try {
            if (user == null || user.getUserId() == null) return;
            if (product == null || product.getProductId() == null) return;

            UserProductEvent event = new UserProductEvent();
            event.setUser(entityManager.getReference(User.class, user.getUserId()));
            event.setProduct(entityManager.getReference(Product.class, product.getProductId()));
            event.setEventType(eventType);
            event.setQuantity(Math.max(quantity, 1));

            eventRepository.save(event);
        } catch (Exception e) {
            log.warn("Cannot record product recommendation event: {}", e.getMessage());
        }
    }
}