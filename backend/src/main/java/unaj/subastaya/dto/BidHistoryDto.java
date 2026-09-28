package unaj.subastaya.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BidHistoryDto(
        Long id,
        Long bidderId,
        String bidderAlias,
        BigDecimal amount,
        LocalDateTime bidDate
) {
}