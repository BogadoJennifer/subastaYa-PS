package unaj.subastaya.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletTransactionDto(
        Long id,
        String type,
        String label,
        String direction,
        BigDecimal amount,
        LocalDateTime date,
        Long auctionId
) {}