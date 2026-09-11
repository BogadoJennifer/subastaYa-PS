package unaj.subastaya.controller;

import org.springframework.web.bind.annotation.*;
import unaj.subastaya.service.BiddingService;
import unaj.subastaya.service.EscrowService;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/auctions/{auctionId}/bids")
public class BidController {

    private final BiddingService biddingService;
    private final EscrowService escrowService;

    public BidController(BiddingService biddingService, EscrowService escrowService) {
        this.biddingService = biddingService;
        this.escrowService = escrowService;
    }

    @PostMapping
    public ResponseEntity<String> placeBid(@PathVariable Long auctionId, @RequestParam long bidId, @RequestParam BigDecimal amount) {
        biddingService.registerBid(auctionId, bidId, amount);
        return ResponseEntity.ok("Oferta resgistrada exitosamente");
    }
}
