package unaj.subastaya.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionCatalogDto(
        Long id,
        String title,
        String description,
        String imageUrl,
        Long categoryId,
        String categoryName,
        BigDecimal basePrice,
        BigDecimal highestBid,
        long bidCount,
        String state,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}