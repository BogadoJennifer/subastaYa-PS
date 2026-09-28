package unaj.subastaya.service;


import unaj.subastaya.repository.UserRepository;
import unaj.subastaya.repository.BidRepository;
import unaj.subastaya.repository.AuctionRepository;
import unaj.subastaya.repository.LedgerTransactionRepository;
import org.springframework.stereotype.Service;
import unaj.subastaya.dto.UserActivitiesDto;
import unaj.subastaya.exception.ResourceNotFoundException;
import unaj.subastaya.model.User;
import unaj.subastaya.model.Bid;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import unaj.subastaya.model.Auction;
import java.util.ArrayList;
import unaj.subastaya.dto.AuctionParticipationDto;
import unaj.subastaya.dto.AuctionPublicationDto;
import unaj.subastaya.model.LedgerTransaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class UserActivitiesService {
    private final UserRepository userRepository;
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;

    public UserActivitiesService(
            UserRepository userRepository,
            BidRepository bidRepository,
            AuctionRepository auctionRepository,
            LedgerTransactionRepository ledgerTransactionRepository) {

        this.userRepository = userRepository;
        this.bidRepository = bidRepository;
        this.auctionRepository = auctionRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
    }
    public UserActivitiesDto getActivities(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<Bid> bids = bidRepository.findByBidderId(userId);

        Map<Long, Auction> auctionsById = new HashMap<>();

        for (Bid bid : bids) {
            Auction auction = bid.getAuction();
            auctionsById.put(auction.getId(), auction);
        }

        List<AuctionParticipationDto> participations = new ArrayList<>();

        for (Auction auction : auctionsById.values()) {

            boolean open = "ACTIVE".equals(auction.getState())
                    && !LocalDateTime.now().isBefore(auction.getStartDate())
                    && LocalDateTime.now().isBefore(auction.getEndDate());

            boolean won = "FINISHED".equals(auction.getState())
                    && auction.getBuyer() != null
                    && auction.getBuyer().getId().equals(userId);

            AuctionParticipationDto participation =
                    new AuctionParticipationDto(
                            auction.getId(),
                            auction.getTitle(),
                            auction.getState(),
                            auction.getEndDate(),
                            open,
                            won
                    );

            participations.add(participation);
        }

        List<Auction> publications = auctionRepository.findByVendorId(userId);

        List<AuctionPublicationDto> publicationDtos = new ArrayList<>();

        for (Auction auction : publications) {

            boolean awarded = "FINISHED".equals(auction.getState())
                    && auction.getBuyer() != null;

            List<LedgerTransaction> transactions =
                    ledgerTransactionRepository.findByAuctionId(auction.getId());

            BigDecimal revenue = BigDecimal.ZERO;

            for (LedgerTransaction transaction : transactions) {
                if ("CHARGE".equals(transaction.getType())) {
                    revenue = revenue.add(transaction.getAmount());
                }
            }

            AuctionPublicationDto publication =
                    new AuctionPublicationDto(
                            auction.getId(),
                            auction.getTitle(),
                            auction.getState(),
                            auction.getEndDate(),
                            awarded,
                            revenue
                    );

            publicationDtos.add(publication);

        }

        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (AuctionPublicationDto publication : publicationDtos) {
            totalRevenue = totalRevenue.add(publication.revenue());
        }

        return new UserActivitiesDto(
                userId,
                participations,
                publicationDtos,
                totalRevenue
        );
    }

}
