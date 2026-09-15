package unaj.subastaya.controller;

import org.springframework.web.bind.annotation.*;
import unaj.subastaya.dto.BidResultDto;
import unaj.subastaya.dto.BidRequestDto;
import unaj.subastaya.service.BiddingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/auctions/{auctionId}/bids")
public class BidController {

    private final BiddingService biddingService;

    public BidController(BiddingService biddingService) {
        this.biddingService = biddingService;
    }

    @PostMapping
    public ResponseEntity<BidResultDto> registerBid(
            @PathVariable Long auctionId,
            @RequestBody BidRequestDto dto) {

        BidResultDto result = biddingService.registerBid(auctionId, dto.bidderId(), dto.amount());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}