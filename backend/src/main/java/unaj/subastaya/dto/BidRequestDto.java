package unaj.subastaya.dto;

import java.math.BigDecimal;

public record BidRequestDto(
        Long bidderId,
        BigDecimal amount
) {}
