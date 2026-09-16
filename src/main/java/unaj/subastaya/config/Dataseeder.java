package unaj.subastaya.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import unaj.subastaya.model.*;
import unaj.subastaya.repository.*;
import unaj.subastaya.service.BiddingService;
import unaj.subastaya.service.EscrowService;

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
            EscrowService escrowService,
            BidRepository bidRepository, BiddingService biddingService) {

        return args -> {

            if (userRepository.count() > 0) {
                return;
            }

            LocalDateTime registrationDate = LocalDateTime.now();

            //BiddingService biddingService1 = new BiddingService(AuctionRepository auctionRepository, WalletRepository walletRepository,
                    //BidRepository bidRepository, EscrowService escrowService,UserRepository userRepository,
                   // CategoriesRepository categoriesRepository, LedgerTransactionRepository ledgerTransactionRepository);

            // Vendor: creates auctions
            User vendor = biddingService.createUser(userRepository,
                    "vendor@test.com",
                    "vendorpassword",
                    registrationDate,
                    "vendor"
            );

            // Buyer 1: leader bidder
            User buyer1 = biddingService.createUser(
                    userRepository,
                    "buyer1@test.com",
                    "buyer1password",
                    registrationDate,
                    "buyer1"
            );

            // Buyer 2: authorized bidder
            User buyer2 = biddingService.createUser(
                    userRepository,
                    "buyer2@test.com",
                    "buyer2password",
                    registrationDate,
                    "buyer2"
            );

            // No funds: user whose bid was rejected
            User noFunds = biddingService.createUser(
                    userRepository,
                    "noFunds@test.com",
                    "nofundspassword",
                    registrationDate,
                    "noFunds"
            );

            // Assign a wallet to each created user
            Wallet walletVendor = biddingService.createWallet(
                    walletRepository,
                    vendor,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );

            Wallet walletBuyer1 = biddingService.createWallet(
                    walletRepository,
                    buyer1,
                    new BigDecimal("150000"),
                    new BigDecimal("105000"),
                    new BigDecimal("45000")
            );

            Wallet walletBuyer2 = biddingService.createWallet(
                    walletRepository,
                    buyer2,
                    new BigDecimal("200000"),
                    new BigDecimal("200000"),
                    BigDecimal.ZERO
            );

            Wallet walletNoFunds = biddingService.createWallet(
                    walletRepository,
                    noFunds,
                    new BigDecimal("500"),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
            );

            // New categories
            Categories tech = biddingService.createCategories(
                    categoriesRepository,
                    "tech",
                    "technology"
            );

            Categories collections = biddingService.createCategories(
                    categoriesRepository,
                    "collections",
                    "toys"
            );

            Categories clothing = biddingService.createCategories(
                    categoriesRepository,
                    "clothes",
                    "variability"
            );

            Categories vehicles = biddingService.createCategories(
                    categoriesRepository,
                    "cars",
                    "vehicles"
            );

            // Standard active auction
            Auction auctionOne = biddingService.createAuction(
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

            Bid bidTwo = biddingService.createBid(
                    bidRepository,
                    auctionOne,
                    buyer2,
                    new BigDecimal("35000"),
                    LocalDateTime.now()
            );

            Bid bidOne = biddingService.createBid(
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
            biddingService.createLedgerTransaction(ledgerTransactionRepository, walletBuyer1, "DEPOSIT", BigDecimal.valueOf(150000), LocalDateTime.now());
            biddingService.createLedgerTransaction(ledgerTransactionRepository, walletBuyer1, "HOLD", BigDecimal.valueOf(45000), LocalDateTime.now());
            biddingService.createLedgerTransaction(ledgerTransactionRepository, walletBuyer1, "PAYMENT", BigDecimal.valueOf(45000), LocalDateTime.now());

            biddingService.createLedgerTransaction(ledgerTransactionRepository, walletBuyer2, "DEPOSIT", BigDecimal.valueOf(200000), LocalDateTime.now());
            biddingService.createLedgerTransaction(ledgerTransactionRepository, walletBuyer2, "HOLD", BigDecimal.valueOf(35000), LocalDateTime.now());
            biddingService.createLedgerTransaction(ledgerTransactionRepository, walletBuyer2, "RELEASE", BigDecimal.valueOf(35000), LocalDateTime.now());

            biddingService.createLedgerTransaction(ledgerTransactionRepository, walletVendor, "CHARGE", BigDecimal.valueOf(45000), LocalDateTime.now());

            // Active critical auction:
            // closes in two minutes to test visual alert and anti-sniping rule
            Auction auctionTwo = biddingService.createAuction(
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
            Auction auctionThree = biddingService.createAuction(
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
            Auction auctionFour = biddingService.createAuction(
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

            biddingService.createBid(
                    bidRepository,
                    auctionFour,
                    buyer1,
                    new BigDecimal("400000"),
                    LocalDateTime.now()
            );

            // Finished auction with no bids
            Auction auctionFive = biddingService.createAuction(
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
}