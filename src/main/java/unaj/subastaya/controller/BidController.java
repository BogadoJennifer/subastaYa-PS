package unaj.subastaya.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import unaj.subastaya.dto.BidHistoryDto;
import unaj.subastaya.dto.BidRequestDto;
import unaj.subastaya.dto.BidResultDto;
import unaj.subastaya.exception.ResourceNotFoundException;
import unaj.subastaya.repository.AuctionRepository;
import unaj.subastaya.repository.BidRepository;
import unaj.subastaya.service.BiddingService;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import unaj.subastaya.service.AuditLogService;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/auctions/{auctionId}/bids")
public class BidController {

    private final BiddingService biddingService;
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final AuditLogService auditLogService;

    public BidController(
            BiddingService biddingService,
            BidRepository bidRepository,
            AuctionRepository auctionRepository,
            AuditLogService auditLogService
    ) {
        this.biddingService = biddingService;
        this.bidRepository = bidRepository;
        this.auctionRepository = auctionRepository;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<BidHistoryDto> getBidHistory(
            @PathVariable Long auctionId
    ) {
        if (!auctionRepository.existsById(auctionId)) {
            throw new ResourceNotFoundException(
                    "Subasta no encontrada"
            );
        }

        return bidRepository
                .findTop50ByAuctionIdOrderByBidDateDescIdDesc(auctionId)
                .stream()
                .map(bid -> new BidHistoryDto(
                        bid.getId(),
                        bid.getBidder().getId(),
                        "Postor " + bid.getBidder().getId(),
                        bid.getAmount(),
                        bid.getBidDate()
                ))
                .toList();
    }

    @PostMapping
    public ResponseEntity<BidResultDto> registerBid(
            @PathVariable Long auctionId,
            @RequestBody BidRequestDto dto
    ) {
        try {
            BidResultDto result = biddingService.registerBid(
                    auctionId,
                    dto.bidderId(),
                    dto.amount()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result);

        } catch (ObjectOptimisticLockingFailureException exception) {

            auditLogService.saveRejectedBidAudit(
                    auctionId,
                    dto.bidderId(),
                    "{\"reason\":\"CONCURRENCY_CONFLICT\",\"amount\":"
                            + dto.amount()
                            + "}"
            );

            throw exception;
        }
    }
}