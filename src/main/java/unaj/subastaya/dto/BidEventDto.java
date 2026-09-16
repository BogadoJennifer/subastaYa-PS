package unaj.subastaya.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidEventDto {

    private Long auctionId;
    private Long bidId;
    private BigDecimal amount;
    private String bidder;
    private LocalDateTime bidDate;
    private LocalDateTime endDate;
    private boolean wasExtended;

    public BidEventDto(Long auctionId,
                       Long bidId,
                       BigDecimal amount,
                       String bidder,
                       LocalDateTime bidDate,
                       LocalDateTime endDate,
                       boolean wasExtended) {

        this.auctionId = auctionId;
        this.bidId = bidId;
        this.amount = amount;
        this.bidder = bidder;
        this.bidDate = bidDate;
        this.endDate = endDate;
        this.wasExtended = wasExtended;
    }

    public Long getAuctionId() {
        return auctionId;
    }

    public Long getBidId() {
        return bidId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getBidder() {
        return bidder;
    }

    public LocalDateTime getBidDate() {
        return bidDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public boolean isWasExtended() {
        return wasExtended;
    }
}
