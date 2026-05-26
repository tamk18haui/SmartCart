package com.gr6.SmartCart.module_v3.recommendation.ai;

import com.gr6.SmartCart.module_v3.recommendation.event.RecommendationEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v3/recommendations/ai")
@RequiredArgsConstructor
public class AiRecommendationController {

    private final AiRecommendationService aiRecommendationService;
    private final RecommendationEventService recommendationEventService;

    @GetMapping("/trending")
    public ResponseEntity<Map<String, Object>> trending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                aiRecommendationService.getTrending(page, size)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(
            Authentication authentication,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (authentication != null && keyword != null && !keyword.isBlank()) {
            recommendationEventService.recordSearch(authentication.getName(), keyword);
        }

        return ResponseEntity.ok(
                aiRecommendationService.searchByKeyword(keyword, page, size)
        );
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> similarByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                aiRecommendationService.getSimilarByProduct(productId, page, size)
        );
    }

    @GetMapping("/personal")
    public ResponseEntity<Map<String, Object>> personal(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String email = authentication == null ? null : authentication.getName();

        return ResponseEntity.ok(
                aiRecommendationService.getPersonal(email, page, size)
        );
    }

    @PostMapping(
            value = "/image-search",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> imageSearch(
            @RequestPart("file") MultipartFile file,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                aiRecommendationService.searchByImage(file, page, size)
        );
    }
}