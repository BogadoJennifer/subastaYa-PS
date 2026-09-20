package unaj.subastaya.service;
import unaj.subastaya.dto.BidResultDto;
import unaj.subastaya.exception.*;
import unaj.subastaya.model.*;
import unaj.subastaya.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class BiddingService {

    private final AuctionRepository auctionRepository;
    private final WalletRepository walletRepository;
    private final BidRepository bidRepository;
    private final EscrowService escrowService;
    private final UserRepository userRepository;
    private final CategoriesRepository categoriesRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;
    private final AuditLogRepository auditLogRepository;

    public BiddingService(AuctionRepository auctionRepository, WalletRepository walletRepository,
                          BidRepository bidRepository, EscrowService escrowService, UserRepository userRepository,
                          CategoriesRepository categoriesRepository, LedgerTransactionRepository ledgerTransactionRepository, org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate,
                          AuditLogRepository auditLogRepository) {

        this.auctionRepository = auctionRepository;
        this.walletRepository = walletRepository;
        this.bidRepository = bidRepository;
        this.escrowService = escrowService;
        this.userRepository = userRepository;
        this.categoriesRepository = categoriesRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.auditLogRepository = auditLogRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public BidResultDto registerBid(
            Long auctionId,
            Long bidderId,
            BigDecimal bidAmount
    ) {
        if (bidderId == null) {
            throw new InvalidBidAmountException(
                    "Debés indicar el usuario que realiza la oferta"
            );
        }

        if (bidAmount == null
                || bidAmount.signum() <= 0
                || bidAmount.stripTrailingZeros().scale() > 2) {
            throw new InvalidBidAmountException(
                    "La oferta debe ser positiva y tener como máximo dos decimales"
            );
        }

        Auction auction = auctionRepository.findForBidding(auctionId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Subasta no encontrada")
                );

        LocalDateTime now = LocalDateTime.now();

        if (!"ACTIVE".equals(auction.getState())
                || now.isBefore(auction.getStartDate())
                || !now.isBefore(auction.getEndDate())) {
            throw new AuctionNotActiveException(
                    "La subasta no está activa en este momento"
            );
        }

        User bidder = userRepository.findById(bidderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Usuario no encontrado")
                );

        if (auction.getVendor() != null
                && auction.getVendor().getId().equals(bidderId)) {
            throw new InvalidBidAmountException(
                    "El vendedor no puede ofertar en su propia subasta"
            );
        }

        BigDecimal currentPrice = bidRepository
                .findHighestBid(auctionId)
                .map(Bid::getAmount)
                .orElse(auction.getBasePrice());

        BigDecimal minimumBid = currentPrice.add(
                auction.getMinimumIncrement()
        );

        if (bidAmount.compareTo(minimumBid) < 0) {
            throw new InvalidBidAmountException(
                    "La oferta mínima es $" + minimumBid.toPlainString()
            );
        }

        // Process escrow before saving the new bid.
        escrowService.processEscrow(auction, bidder, bidAmount);

        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setBidder(bidder);
        bid.setAmount(bidAmount);
        bid.setBidDate(now);

        Bid savedBid = bidRepository.save(bid);

        Duration remainingTime = Duration.between(
                now,
                auction.getEndDate()
        );

        boolean wasExtended =
                remainingTime.compareTo(Duration.ofSeconds(60)) <= 0;

        if (wasExtended) {
            LocalDateTime previousEndDate = auction.getEndDate();

            auction.setEndDate(previousEndDate.plusMinutes(2));

            AuditLog auditLog = new AuditLog();

            auditLog.setEntity("AUCTION");
            auditLog.setEntityId(auctionId);
            auditLog.setAction("ANTI_SNIPING_EXTENSION");
            auditLog.setUserId(bidderId);
            auditLog.setDate(now);
            auditLog.setDetailJson(
                    "{\"bidId\":" + savedBid.getId()
                            + ",\"previousEndDate\":\"" + previousEndDate
                            + "\",\"newEndDate\":\"" + auction.getEndDate()
                            + "\"}"
            );

            auditLogRepository.save(auditLog);
        }

        auctionRepository.save(auction);

        BidResultDto result = new BidResultDto(
                savedBid.getId(),
                savedBid.getAmount(),
                auction.getEndDate(),
                wasExtended
        );

        Map<String, Object> notification = Map.of(
                "bidId", savedBid.getId(),
                "highestBid", savedBid.getAmount(),
                "lastBidderId", bidderId,
                "endDate", auction.getEndDate(),
                "bidDate", savedBid.getBidDate()
        );

        // Notify clients only after the transaction commits successfully.
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            messagingTemplate.convertAndSend(
                                    "/topic/auctions/" + auctionId,
                                    Optional.of(notification)
                            );
                        } catch (RuntimeException exception) {
                            org.slf4j.LoggerFactory
                                    .getLogger(BiddingService.class)
                                    .error(
                                            "Bid {} was saved, but its notification failed",
                                            savedBid.getId(),
                                            exception
                                    );
                        }
                    }
                }
        );

        return result;
    }

    // Methods to create entities

    public Categories createCategories(
            CategoriesRepository categoriesRepository,
            String name,
            String description) {

        Categories category = new Categories();

        category.setName(name);
        category.setDescription(description);

        return categoriesRepository.save(category);
    }

    public Auction createAuction(
            AuctionRepository auctionRepository,
            User vendor,
            User buyer,
            Categories category,
            String title,
            String description,
            BigDecimal basePrice,
            BigDecimal minimumIncrement,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String state) {

        Auction auction = new Auction();

        auction.setVendor(vendor);
        auction.setBuyer(buyer);
        auction.setCategories(category);
        auction.setStartDate(startDate);
        auction.setEndDate(endDate);
        auction.setState(state);
        auction.setTitle(title);
        auction.setDescription(description);
        auction.setBasePrice(basePrice);
        auction.setMinimumIncrement(minimumIncrement);

        return auctionRepository.save(auction);
    }

    public Wallet createWallet(
            WalletRepository walletRepository,
            User user,
            BigDecimal totalBalance,
            BigDecimal availableBalance,
            BigDecimal reservedBalance) {

        Wallet wallet = new Wallet();

        wallet.setUser(user);
        wallet.setTotalBalance(totalBalance);
        wallet.setAvailableBalance(availableBalance);
        wallet.setReservedBalance(reservedBalance);

        return walletRepository.save(wallet);
    }

    public Bid createBid(
            BidRepository bidRepository,
            Auction auction,
            User bidder,
            BigDecimal amount,
            LocalDateTime bidDate) {

        Bid bid = new Bid();

        bid.setAuction(auction);
        bid.setBidder(bidder);
        bid.setAmount(amount);
        bid.setBidDate(bidDate);

        bidRepository.save(bid);
        return bid;
    }

    public void createLedgerTransaction(
            LedgerTransactionRepository ledgerTransactionRepository,
            Wallet wallet,
            String type,
            BigDecimal amount,
            LocalDateTime date) {

        LedgerTransaction transaction = new LedgerTransaction();

        transaction.setWallet(wallet);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDate(date);

        ledgerTransactionRepository.save(transaction);
    }

    public User createUser(
            UserRepository userRepository,
            String email,
            String passwordHash,
            LocalDateTime registrationDate,
            String name) {

        User user = new User();

        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setRegistrationDate(registrationDate);
        user.setName(name);

        return userRepository.save(user);
    }
}