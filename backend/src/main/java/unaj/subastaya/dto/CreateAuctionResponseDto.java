package unaj.subastaya.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateAuctionResponseDto(
        Long id,
        String title,
        String description,
        String imageUrl,
        Long categoryId,
        String categoryName,
        BigDecimal basePrice,
        BigDecimal minimumIncrement,
        LocalDateTime startDate,
        LocalDateTime endDate,
        String state
) {
}