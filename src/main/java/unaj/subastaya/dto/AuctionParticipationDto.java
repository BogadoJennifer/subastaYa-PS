package unaj.subastaya.dto;
import java.time.LocalDateTime;

public record AuctionParticipationDto(
    Long auctionId,
    String title,
    String state,
    LocalDateTime endDate,
    boolean open,
    boolean won
){}
