package unaj.subastaya.service;
import unaj.subastaya.model.*;
import unaj.subastaya.repository.*;
import unaj.subastaya.service.EscrowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class BiddingService {

    private final AuctionRepository auctionRepository;
    private final WalletRepository walletRepository;
    private final BidRepository bidRepository;
    private final EscrowService escrowService;
    private final UserRepository userRepository;
    private final CategoriesRepository categoriesRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;

    public BiddingService(AuctionRepository auctionRepository, WalletRepository walletRepository,
                          BidRepository bidRepository, EscrowService escrowService,UserRepository userRepository,
                          CategoriesRepository categoriesRepository, LedgerTransactionRepository ledgerTransactionRepository) {
        this.auctionRepository = auctionRepository;
        this.walletRepository = walletRepository;
        this.bidRepository = bidRepository;
        this.escrowService = escrowService;
        this.userRepository = userRepository;
        this.categoriesRepository = categoriesRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
    }

    @Transactional
    public void registerBid(Long auctionId, Long buyerId, BigDecimal bidAmount) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Subasta no encontrada"));

        LocalDateTime now = LocalDateTime.now();

        // 1. Validar que la subasta siga activa temporalmente y por estado
        if (!"ACTIVE".equalsIgnoreCase(auction.getState()) || now.isAfter(auction.getEndDate())) {
            throw new IllegalStateException("La subasta no está activa o ya ha finalizado");
        }

        // 2. Validar que el comprador no sea el vendedor *revisar*
        if (auction.getBuyer() != null && auction.getBuyer().getId().equals(buyerId)) {
            throw new IllegalArgumentException("El vendedor no puede ofertar en su propia subasta");
        }

        // 3. Validar incremento mínimo respecto al precio base
        if (bidAmount.compareTo(auction.getBasePrice().add(auction.getMinimumIncrement())) < 0) {
            throw new IllegalArgumentException("El monto ofertado no supera el incremento mínimo requerido");
        }

        // 4. Validar billetera y saldo
        Wallet walletBuyer = walletRepository.findByUserId(buyerId);
        if (walletBuyer == null) {
            throw new IllegalArgumentException("Billetera no encontrada");
        }

        if (walletBuyer.getAvailableBalance().compareTo(bidAmount) < 0) {
            throw new IllegalStateException("Saldo insuficiente para ofertar");
        }

        // 5. Anti-Sniping Rule: si restan <= 60 segundos, se extiende 2 minutos
        long secondsRemaining = Duration.between(now, auction.getEndDate()).getSeconds();
        if (secondsRemaining <= 60 && secondsRemaining >= 0) {
            auction.setEndDate(auction.getEndDate().plusMinutes(2));
        }

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