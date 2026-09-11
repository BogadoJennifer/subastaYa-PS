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
    public ResponseEntity<String> registerBid(
            @PathVariable Long auctionId,
            @RequestParam Long bidderId,
            @RequestParam BigDecimal amount) {

        try {
            biddingService.registerBid(auctionId, bidderId, amount);

            return ResponseEntity.status(201)
                    .body("Oferta registrada correctamente");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        }
    }
}
