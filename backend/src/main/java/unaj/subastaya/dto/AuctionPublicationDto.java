package unaj.subastaya.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionPublicationDto (
    Long auctionId,
    String title,
    String state,
    LocalDateTime endDate,
    boolean awarded,
    BigDecimal revenue
){}
