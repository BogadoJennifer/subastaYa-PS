package unaj.subastayaps;

import unaj.subastayaps.model.Auction;
import unaj.subastayaps.model.User;
import unaj.subastayaps.model.Wallet;
import unaj.subastayaps.repository.AuctionRepository;
import unaj.subastayaps.repository.UserRepository;
import unaj.subastayaps.repository.WalletRepository;
import unaj.subastayaps.repository.CategoriesRepository;
import unaj.subastayaps.model.Categories;
import unaj.subastayaps.service.BiddingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class BiddingConcurrencyTest {

    @Autowired
    private BiddingService biddingService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    private Long walletId;
    private Long auctionId;
    private Long userId;

    @BeforeEach
    void setUp() {
        // 1. Limpiar base de datos
        auctionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
        categoriesRepository.deleteAll();
        // 2. Crear usuario y billetera inicial
        User user = new User();
        user.setEmail("comprador@test.com");
        user.setPasswordHash("pass123");
        user.setRegistrationDate(LocalDateTime.now());
        user = userRepository.save(user);
        userId = user.getId();

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setTotalBalance(new BigDecimal("200000.00"));
        wallet.setRetainedBalance(BigDecimal.ZERO);
        wallet.setAvailableBalance(new BigDecimal("200000.00"));
        wallet = walletRepository.save(wallet);
        walletId = wallet.getId();

        // Crear y guardar categoría obligatoria
        Categories category = new Categories();
        category.setName("Electrónica"); // o el nombre del campo en tu modelo (ej. setNombre)
        //category.setSlug("electronica");
        category = categoriesRepository.save(category);

        // Crear subasta asignando la categoría y el usuario
        Auction auction = new Auction();
        auction.setTitle("Subasta Test Concurrencia");
        auction.setDescription("Descripción de prueba");
        auction.setBasePrice(new BigDecimal("50000.00"));
        auction.setMinimumIncrement(new BigDecimal("5000.00"));
        auction.setStartDate(LocalDateTime.now());
        auction.setEndDate(LocalDateTime.now().plusDays(2));
        auction.setState("ACTIVE");
        auction.setSeller(user);
        auction.setCategories(category); // <-- Asignar la categoría persistida

        auction = auctionRepository.save(auction);
        auctionId = auction.getId();
    }

    @Test
    void testOptimisticLockingConcurrentBids() throws InterruptedException {
        int numberOfThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1); // Sincroniza el disparo simultáneo

        AtomicInteger successfulBids = new AtomicInteger(0);
        AtomicInteger failedBids = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    latch.await(); // Esperan en la línea de largada
                    biddingService.registerBid(auctionId, userId, new BigDecimal("10000.00"));
                    successfulBids.incrementAndGet();
                } catch (OptimisticLockingFailureException e) {
                    failedBids.incrementAndGet(); // Captura el fallo de versión
                } catch (Exception ignored) {
                }
            });
        }

        // Dispara ambos hilos al mismo milisegundo exacto
        latch.countDown();
        executorService.shutdown();
        executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);

        // Verificaciones: Uno debe ganar y el otro debe fallar por Optimistic Locking
        assertEquals(1, successfulBids.get(), "Exactamente una puja debió ser exitosa");
        assertEquals(1, failedBids.get(), "Exactamente una puja debió fallar por versión desactualizada");
    }
}