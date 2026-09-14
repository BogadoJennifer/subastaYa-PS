package unaj.subastaya.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import unaj.subastaya.model.*;
import unaj.subastaya.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
class Dataseeder {

    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            WalletRepository walletRepository,
            CategoriesRepository categoriesRepository,
            AuctionRepository auctionRepository,
            LedgerTransactionRepository ledgerTransactionRepository,
            BidRepository bidRepository) {

        return args -> {

            if (userRepository.count() > 0) {
                return;
            }

            LocalDateTime registrationDate = LocalDateTime.now();

            // Vendor: creates auctions
            User vendor = createUser(
                    userRepository,
                    "vendor@test.com",
                    "vendorpassword",
                    registrationDate,
                    "vendor"
            );

            // Buyer 1: leader bidder
            User buyer1 = createUser(
                    userRepository,
                    "buyer1@test.com",
                    "buyer1password",
                    registrationDate,
                    "buyer1"
            );

            // Buyer 2: authorized bidder
            User buyer2 = createUser(
                    userRepository,
                    "buyer2@test.com",
                    "buyer2password",
                    registrationDate,
                    "buyer2"
            );

            // No funds: user whose bid was rejected
            User noFunds = createUser(
                    userRepository,
                    "noFunds@test.com",
                    "nofundspassword",
                    registrationDate,
                    "noFunds"
            );

            // Assign a wallet to each created user
            Wallet walletVendor = createWallet(
                    walletRepository,
                    vendor,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );

            Wallet walletBuyer1 = createWallet(
                    walletRepository,
                    buyer1,
                    new BigDecimal("150000"),
                    new BigDecimal("105000"),
                    new BigDecimal("45000")
            );

            Wallet walletBuyer2 = createWallet(
                    walletRepository,
                    buyer2,
                    new BigDecimal("200000"),
                    new BigDecimal("200000"),
                    BigDecimal.ZERO
            );

            Wallet walletNoFunds = createWallet(
                    walletRepository,
                    noFunds,
                    new BigDecimal("500"),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );

            // New categories
            Categories tech = createCategories(
                    categoriesRepository,
                    "tech",
                    "technology"
            );

            Categories collections = createCategories(
                    categoriesRepository,
                    "collections",
                    "toys"
            );

            Categories clothing = createCategories(
                    categoriesRepository,
                    "clothes",
                    "variability"
            );

            Categories vehicles = createCategories(
                    categoriesRepository,
                    "cars",
                    "vehicles"
            );

            // Standard active auction
            Auction auctionOne = createAuction(
                    auctionRepository,
                    vendor,
                    null,
                    tech,
                    "Nintendo",
                    "old Nintendo",
                    new BigDecimal("30000"),
                    new BigDecimal("5000"),
                    LocalDateTime.now(),
                    LocalDateTime.now().plusMinutes(30),
                    "ACTIVE"
            );

            Bid bidTwo = createBid(
                    bidRepository,
                    auctionOne,
                    buyer2,
                    new BigDecimal("35000"),
                    LocalDateTime.now()
            );

            Bid bidOne = createBid(
                    bidRepository,
                    auctionOne,
                    buyer1,
                    new BigDecimal("45000"),
                    LocalDateTime.now()
            ); // Leader

            //bidding history
            BigDecimal amountBidOne = bidTwo.getAmount();
            BigDecimal amountBidTwo = bidOne.getAmount();

            User nameBuyerTwo = bidOne.getBidder();
            User nameBuyerOne = bidTwo.getBidder();

            LocalDateTime dateBidOne = bidOne.getBidDate();
            LocalDateTime dateBidTwo = bidTwo.getBidDate();

            //ledger transactions
            createLedgerTransaction(ledgerTransactionRepository, walletBuyer1, "DEPOSIT", BigDecimal.valueOf(150000), LocalDateTime.now());
            createLedgerTransaction(ledgerTransactionRepository, walletBuyer1, "HOLD", BigDecimal.valueOf(45000), LocalDateTime.now());
            createLedgerTransaction(ledgerTransactionRepository, walletBuyer1, "PAYMENT", BigDecimal.valueOf(45000), LocalDateTime.now());

            createLedgerTransaction(ledgerTransactionRepository, walletBuyer2, "DEPOSIT", BigDecimal.valueOf(200000), LocalDateTime.now());
            createLedgerTransaction(ledgerTransactionRepository, walletBuyer2, "HOLD", BigDecimal.valueOf(35000), LocalDateTime.now());
            createLedgerTransaction(ledgerTransactionRepository, walletBuyer2, "RELEASE", BigDecimal.valueOf(35000), LocalDateTime.now());

            createLedgerTransaction(ledgerTransactionRepository, walletVendor, "CHARGE", BigDecimal.valueOf(45000), LocalDateTime.now());

            // Active critical auction:
            // closes in two minutes to test visual alert and anti-sniping rule
            Auction auctionTwo = createAuction(
                    auctionRepository,
                    vendor,
                    null,
                    clothing,
                    "Sweater",
                    "sweater with some details",
                    new BigDecimal("60000"),
                    new BigDecimal("5000"),
                    LocalDateTime.now(),
                    LocalDateTime.now().plusMinutes(2),
                    "ACTIVE"
            );

            // Scheduled auction: starts in 24 hours and has no bids
            Auction auctionThree = createAuction(
                    auctionRepository,
                    vendor,
                    null,
                    collections,
                    "Comics",
                    "marvel comics",
                    new BigDecimal("30000"),
                    new BigDecimal("5000"),
                    LocalDateTime.now().plusHours(24),
                    LocalDateTime.now().plusHours(48),
                    "SCHEDULED"
            );

            if ( LocalDateTime.now().isBefore(auctionThree.getStartDate())){
                if (auctionThree.getBuyer() != null) {
                    throw new IllegalStateException("Auction has not started");
                }
            }

            // Expired auction: used to test Worker
            Auction auctionFour = createAuction(
                    auctionRepository,
                    vendor,
                    null,
                    vehicles,
                    "Ford 2006",
                    "good condition",
                    new BigDecimal("3000000"),
                    new BigDecimal("500000"),
                    LocalDateTime.now().minusHours(48),
                    LocalDateTime.now().minusHours(24),
                    "FINISHED"
            );

            createBid(
                    bidRepository,
                    auctionFour,
                    buyer1,
                    new BigDecimal("400000"),
                    LocalDateTime.now()
            );

            // Finished auction with no bids
            Auction auctionFive = createAuction(
                    auctionRepository,
                    vendor,
                    null,
                    collections,
                    "toy",
                    "spiderman",
                    new BigDecimal("30000"),
                    new BigDecimal("5000"),
                    LocalDateTime.now().minusHours(2),
                    LocalDateTime.now().minusHours(1),
                    "FINISHED"
            );

            if (auctionFive.getBuyer() == null) {
                auctionFive.setState("UNSOLD");
                auctionRepository.save(auctionFive);
            }



        };
    }

    // Methods to create entities

    private Categories createCategories(
            CategoriesRepository categoriesRepository,
            String name,
            String description) {

        Categories category = new Categories();

        category.setName(name);
        category.setDescription(description);

        return categoriesRepository.save(category);
    }

    private Auction createAuction(
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

    private Wallet createWallet(
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

    private Bid createBid(
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

    private void createLedgerTransaction(
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

    private User createUser(
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