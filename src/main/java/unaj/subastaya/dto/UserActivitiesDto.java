package unaj.subastaya.dto;
import java.math.BigDecimal;
import java.util.List;

public record UserActivitiesDto (
    Long userId,
    List<AuctionParticipationDto> participations,
    List<AuctionPublicationDto> publications,
    BigDecimal totalRevenue
){}
