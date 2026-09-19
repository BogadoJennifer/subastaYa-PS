package unaj.subastaya.controller;

import org.springframework.web.bind.annotation.*;
import unaj.subastaya.model.Auction;
import unaj.subastaya.repository.AuctionRepository;
import org.springframework.http.ResponseEntity;
import unaj.subastaya.dto.AuctionCatalogDto;
import unaj.subastaya.model.Bid;
import unaj.subastaya.repository.BidRepository;
import unaj.subastaya.dto.AuctionDetailsDto;
import jakarta.validation.Valid;
import unaj.subastaya.dto.CreateAuctionRequestDto;
import unaj.subastaya.service.AuctionPublicationService;

import java.util.List;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final AuctionPublicationService auctionPublicationService;

    public AuctionController(
            AuctionRepository auctionRepository,
            BidRepository bidRepository,
            AuctionPublicationService auctionPublicationService
    ) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.auctionPublicationService = auctionPublicationService;
    }

    @GetMapping
    public List<Auction> getAllAuctions() {
        return auctionRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Auction> getAuctionById(@PathVariable Long id) {
        return auctionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Auction> createAuction(@Valid @RequestBody CreateAuctionRequestDto request) {
        Auction savedAuction = auctionPublicationService.createAuction(request);
        return ResponseEntity.status(201).body(savedAuction);
    }
    @GetMapping("/catalog")
    public List<AuctionCatalogDto> getCatalog() {
        return auctionRepository.findAll()
                .stream()
                .map(auction -> new AuctionCatalogDto(
                        auction.getId(),
                        auction.getTitle(),
                        auction.getDescription(),
                        auction.getImageUrl(),
                        auction.getCategories().getId(),
                        auction.getCategories().getName(),
                        auction.getBasePrice(),
                        bidRepository.findHighestBid(auction.getId())
                                .map(Bid::getAmount)
                                .orElse(null),
                        bidRepository.countByAuctionId(auction.getId()),
                        auction.getState(),
                        auction.getStartDate(),
                        auction.getEndDate()
                ))
                .toList();
    }
    @GetMapping("/{id}/details")
    public ResponseEntity<AuctionDetailsDto> getAuctionDetails(
            @PathVariable Long id,
            @RequestParam(required = false) Long userId
    ) {
        return auctionRepository.findById(id)
                .map(auction -> {
                    Bid highestBid = bidRepository.findHighestBid(id)
                            .orElse(null);

                    boolean currentUserHasBid =
                            userId != null
                                    && bidRepository.existsByAuctionIdAndBidderId(
                                    id,
                                    userId
                            );

                    AuctionDetailsDto details = new AuctionDetailsDto(
                            auction.getId(),
                            auction.getTitle(),
                            auction.getDescription(),
                            auction.getBasePrice(),
                            auction.getMinimumIncrement(),
                            highestBid != null
                                    ? highestBid.getAmount()
                                    : null,
                            auction.getState(),
                            auction.getStartDate(),
                            auction.getEndDate(),
                            highestBid != null
                                    ? highestBid.getBidder().getId()
                                    : null,
                            currentUserHasBid
                    );

                    return ResponseEntity.ok(details);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


}
