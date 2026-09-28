package unaj.subastaya.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionDetailsDto(
        Long id,
        String title,
        String description,
        BigDecimal basePrice,
        BigDecimal minimumIncrement,
        BigDecimal highestBid,
        String state,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Long highestBidderId,
        boolean currentUserHasBid
) {
}