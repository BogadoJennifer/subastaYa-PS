package unaj.subastaya.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class BidResultDto {
    private long bidId;
    private BigDecimal amount;
    private LocalDateTime endDate;
    private boolean wasExtended;

    public BidResultDto(long bidId, BigDecimal amount, LocalDateTime endDate, boolean wasExtended) {
        this.bidId = bidId;
        this.amount = amount;
        this.endDate = endDate;
        this.wasExtended = wasExtended;
    }

    // Getters and setters
    public long getBidId() {
        return bidId;
    }

    public void setBidId(long bidId) {
        this.bidId = bidId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public boolean isWasExtended() {
        return wasExtended;
    }

    public void setWasExtended(boolean wasExtended) {
        this.wasExtended = wasExtended;
    }
}
