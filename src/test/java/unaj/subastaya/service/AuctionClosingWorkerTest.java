package unaj.subastaya.service;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import unaj.subastaya.model.Auction;
import unaj.subastaya.model.Bid;
import unaj.subastaya.model.LedgerTransaction;
import unaj.subastaya.model.User;
import unaj.subastaya.model.Wallet;
import unaj.subastaya.repository.AuctionRepository;
import unaj.subastaya.repository.BidRepository;
import unaj.subastaya.repository.LedgerTransactionRepository;
import unaj.subastaya.repository.WalletRepository;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

class AuctionClosingWorkerTest {

    @Test
    void PaymentAndCharge() {

        AuctionRepository auctionRepository =
                mock(AuctionRepository.class);

        BidRepository bidRepository =
                mock(BidRepository.class);

        WalletRepository walletRepository =
                mock(WalletRepository.class);

        LedgerTransactionRepository ledgerTransactionRepository =
                mock(LedgerTransactionRepository.class);

        SimpMessagingTemplate messagingTemplate =
                mock(SimpMessagingTemplate.class);

        AuctionClosingWorker worker = new AuctionClosingWorker(
                auctionRepository,
                bidRepository,
                walletRepository,
                ledgerTransactionRepository,
                messagingTemplate
        );

        // =========================
        // USUARIOS
        // =========================

        User vendor = new User();
        vendor.setId(1L);
        vendor.setName("Vendedor");

        User winner = new User();
        winner.setId(2L);
        winner.setName("Comprador");

        // =========================
        // SUBASTA
        // =========================

        Auction auction = new Auction();
        auction.setId(100L);
        auction.setVendor(vendor);
        auction.setState("ACTIVE");
        auction.setEndDate(
                LocalDateTime.now().minusMinutes(1)
        );

        // =========================
        // WALLET DEL GANADOR
        // =========================

        Wallet winnerWallet = new Wallet();

        winnerWallet.setUser(winner);
        winnerWallet.setTotalBalance(
                new BigDecimal("50000")
        );
        winnerWallet.setRetainedBalance(
                new BigDecimal("15000")
        );
        winnerWallet.setAvailableBalance(
                new BigDecimal("35000")
        );

        // =========================
        // WALLET DEL VENDEDOR
        // =========================

        Wallet vendorWallet = new Wallet();

        vendorWallet.setUser(vendor);
        vendorWallet.setTotalBalance(
                new BigDecimal("30000")
        );
        vendorWallet.setRetainedBalance(
                BigDecimal.ZERO
        );
        vendorWallet.setAvailableBalance(
                new BigDecimal("30000")
        );

        // =========================
        // OFERTA GANADORA
        // =========================

        Bid winningBid = new Bid();

        winningBid.setId(10L);
        winningBid.setAuction(auction);
        winningBid.setBidder(winner);
        winningBid.setAmount(
                new BigDecimal("15000")
        );
        winningBid.setBidDate(
                LocalDateTime.now().minusMinutes(2)
        );

        // =========================
        // MOCKS
        // =========================

        when(auctionRepository
                .findByStateAndEndDateBefore(
                        eq("ACTIVE"),
                        any(LocalDateTime.class)
                ))
                .thenReturn(List.of(auction));

        when(bidRepository.findHighestBid(100L))
                .thenReturn(Optional.of(winningBid));

        when(walletRepository.findByUser(winner))
                .thenReturn(winnerWallet);

        when(walletRepository.findByUser(vendor))
                .thenReturn(vendorWallet);

        TransactionSynchronizationManager.initSynchronization();

        try {
            worker.closeExpiredAuctions();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        // =========================
        // COMPROBAR WALLET GANADOR
        // =========================

        assertEquals(
                new BigDecimal("35000"),
                winnerWallet.getTotalBalance()
        );

        assertEquals(
                BigDecimal.ZERO,
                winnerWallet.getRetainedBalance()
        );

        assertEquals(
                new BigDecimal("35000"),
                winnerWallet.getAvailableBalance()
        );

        // =========================
        // COMPROBAR WALLET VENDEDOR
        // =========================

        assertEquals(
                new BigDecimal("45000"),
                vendorWallet.getTotalBalance()
        );

        assertEquals(
                new BigDecimal("45000"),
                vendorWallet.getAvailableBalance()
        );

        // =========================
        // COMPROBAR SUBASTA
        // =========================

        assertEquals(
                "FINISHED",
                auction.getState()
        );

        assertEquals(
                winner,
                auction.getBuyer()
        );

        // =========================
        // COMPROBAR GUARDADO
        // =========================

        ArgumentCaptor<LedgerTransaction> transactionCaptor =
                ArgumentCaptor.forClass(LedgerTransaction.class);

        verify(ledgerTransactionRepository, times(2))
                .save(transactionCaptor.capture());

        List<LedgerTransaction> transactions =
                transactionCaptor.getAllValues();

        assertEquals(2, transactions.size());

        LedgerTransaction payment = transactions.stream()
                .filter(t -> t.getType().equals("PAYMENT"))
                .findFirst()
                .orElseThrow();

        LedgerTransaction charge = transactions.stream()
                .filter(t -> t.getType().equals("CHARGE"))
                .findFirst()
                .orElseThrow();

        assertEquals(new BigDecimal("-15000"), payment.getAmount());
        assertEquals(new BigDecimal("15000"), charge.getAmount());

        assertEquals(100L, payment.getAuctionId());
        assertEquals(100L, charge.getAuctionId());

        assertEquals(winnerWallet, payment.getWallet());
        assertEquals(vendorWallet, charge.getWallet());

        verify(auctionRepository)
                .save(auction);

        // =========================
        // COMPROBAR LEDGER
        // =========================

        verify(ledgerTransactionRepository, times(2))
                .save(any(LedgerTransaction.class));
    }

    @Test
    void debeMarcarComoUnsoldUnaSubastaSinOfertas() {

        AuctionRepository auctionRepository = mock(AuctionRepository.class);
        BidRepository bidRepository = mock(BidRepository.class);
        WalletRepository walletRepository = mock(WalletRepository.class);
        LedgerTransactionRepository ledgerTransactionRepository =
                mock(LedgerTransactionRepository.class);
        SimpMessagingTemplate messagingTemplate =
                mock(SimpMessagingTemplate.class);

        AuctionClosingWorker worker = new AuctionClosingWorker(
                auctionRepository,
                bidRepository,
                walletRepository,
                ledgerTransactionRepository,
                messagingTemplate
        );

        Auction auction = new Auction();
        auction.setId(200L);
        auction.setState("ACTIVE");
        auction.setEndDate(LocalDateTime.now().minusMinutes(1));

        when(auctionRepository.findByStateAndEndDateBefore(
                eq("ACTIVE"),
                any(LocalDateTime.class)
        )).thenReturn(List.of(auction));

        when(bidRepository.findHighestBid(200L))
                .thenReturn(Optional.empty());

        TransactionSynchronizationManager.initSynchronization();

        try {
            worker.closeExpiredAuctions();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        assertEquals("UNSOLD", auction.getState());

        verify(auctionRepository).save(auction);

        verify(walletRepository, never()).save(any(Wallet.class));

        verify(ledgerTransactionRepository, never())
                .save(any(LedgerTransaction.class));
    }
}