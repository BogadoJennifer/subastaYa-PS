package unaj.subastaya.config;

import unaj.subastaya.model.Auction;
import unaj.subastaya.model.User;
import unaj.subastaya.model.Wallet;
import unaj.subastaya.model.Categories;
import unaj.subastaya.repository.AuctionRepository;
import unaj.subastaya.repository.CategoriesRepository;
import unaj.subastaya.repository.UserRepository;
import unaj.subastaya.repository.WalletRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   WalletRepository walletRepository,
                                   CategoriesRepository categoriesRepository,
                                   AuctionRepository auctionRepository) {
        return args -> {
            // Check if data already exists to avoid duplication on restart
            if (userRepository.count() == 0) {

                // 1. Seed Users
                User vendor = new User();
                vendor.setEmail("vendor@test.com");
                vendor.setName("Vendor Master");
                vendor.setPasswordHash("securepassword");
                vendor = userRepository.save(vendor);

                User buyerOne = new User();
                buyerOne.setEmail("buyer1@test.com");
                buyerOne.setName("Buyer One");
                buyerOne.setPasswordHash("securepassword");
                buyerOne = userRepository.save(buyerOne);

                User buyerTwo = new User();
                buyerTwo.setEmail("buyer2@test.com");
                buyerTwo.setName("Buyer Two");
                buyerTwo.setPasswordHash("securepassword");
                buyerTwo = userRepository.save(buyerTwo);

                User noFundsUser = new User();
                noFundsUser.setEmail("nofunds@test.com");
                noFundsUser.setName("No Funds User");
                noFundsUser.setPasswordHash("securepassword");
                noFundsUser = userRepository.save(noFundsUser);

                // 2. Seed Wallets (with initial balances and versioning)
                createWallet(walletRepository, vendor, new BigDecimal("50000.00"));
                createWallet(walletRepository, buyerOne, new BigDecimal("20000.00"));
                createWallet(walletRepository, buyerTwo, new BigDecimal("15000.00"));
                createWallet(walletRepository, noFundsUser, BigDecimal.ZERO);

                // 3. Seed Categories

                Categories electronics = createCategories(categoriesRepository, "Electronics", "Devices, gadgets, and tech accessories");
                Categories vehicles = createCategories(categoriesRepository, "Vehicles", "Cars, motorcycles, and transport items");
                Categories art = createCategories(categoriesRepository, "Art & Collectibles", "Paintings, sculptures, and rare collectibles");

                // 4. Seed 5 Test Auctions
                createAuction(auctionRepository, vendor, electronics, "Smartphone X Pro",
                        "Latest generation smartphone with 256GB storage.", new BigDecimal("500.00"), new BigDecimal("25.00"),
                        LocalDateTime.now().minusHours(2), LocalDateTime.now().plusDays(2), "ACTIVA");

                createAuction(auctionRepository, vendor, electronics, "Gaming Laptop 15",
                        "High performance laptop equipped with dedicated GPU.", new BigDecimal("1200.00"), new BigDecimal("50.00"),
                        LocalDateTime.now().minusHours(1), LocalDateTime.now().plusDays(3), "ACTIVA");

                createAuction(auctionRepository, vendor, vehicles, "Vintage Road Bicycle",
                        "Classic 1980s restoration project bicycle.", new BigDecimal("300.00"), new BigDecimal("15.00"),
                        LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(5), "PROGRAMADA");

                createAuction(auctionRepository, vendor, art, "Abstract Canvas Painting",
                        "Original oil painting signed by the local artist.", new BigDecimal("800.00"), new BigDecimal("40.00"),
                        LocalDateTime.now().minusHours(5), LocalDateTime.now().plusDays(1), "ACTIVA");

                createAuction(auctionRepository, vendor, electronics, "Wireless Headphones",
                        "Active noise-cancelling Bluetooth headphones.", new BigDecimal("150.00"), new BigDecimal("10.00"),
                        LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1), "FINALIZADA");
            }
        };
    }

    private void createWallet(WalletRepository walletRepository, User user, BigDecimal initialBalance) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setTotalBalance(initialBalance);
        wallet.setAvailableBalance(initialBalance);
        wallet.setReservedBalance(BigDecimal.ZERO);
        walletRepository.save(wallet);
    }

    private Categories createCategories(CategoriesRepository categoriesRepository, String name, String description) {
        Categories category = new Categories();
        category.setName(name);
        category.setDescription(description);
        return categoriesRepository.save(category);
    }

    private void createAuction(AuctionRepository auctionRepository, User vendor, Categories category,
                               String title, String description, BigDecimal basePrice,
                               BigDecimal minimumIncrement, LocalDateTime startDate,
                               LocalDateTime endDate, String status) {
        Auction auction = new Auction();
        auction.setSeller(vendor);
        auction.setCategories(category);
        auction.setTitle(title);
        auction.setDescription(description);
        auction.setBasePrice(basePrice);
        auction.setMinimumIncrement(minimumIncrement);
        auction.setStartDate(startDate);
        auction.setEndDate(endDate);
        auction.setStatuS(status);
        auctionRepository.save(auction);
    }
}