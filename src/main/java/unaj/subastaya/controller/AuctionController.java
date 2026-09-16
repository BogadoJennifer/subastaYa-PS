package unaj.subastaya.controller;

import org.springframework.web.bind.annotation.*;
import unaj.subastaya.model.Auction;
import unaj.subastaya.repository.AuctionRepository;
import org.springframework.http.ResponseEntity;
import unaj.subastaya.dto.AuctionCatalogDto;
import unaj.subastaya.model.Bid;
import unaj.subastaya.repository.BidRepository;

import java.util.List;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    public AuctionController(
            AuctionRepository auctionRepository,
            BidRepository bidRepository
    ) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
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
    public ResponseEntity<Auction> createAuction(@RequestBody Auction auction) {
        Auction savedAuction = auctionRepository.save(auction);
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
}
