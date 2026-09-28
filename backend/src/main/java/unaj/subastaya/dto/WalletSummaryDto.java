package unaj.subastaya.dto;
import java.math.BigDecimal;

public record WalletSummaryDto(
        Long walletId,
        Long userId,
        String userName,
        BigDecimal totalBalance,
        BigDecimal retainedBalance,
        BigDecimal availableBalance
) {}
