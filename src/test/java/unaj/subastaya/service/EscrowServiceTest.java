package unaj.subastaya.service;

import org.junit.jupiter.api.Test;
import unaj.subastaya.model.Auction;
import unaj.subastaya.model.Bid;
import unaj.subastaya.model.LedgerTransaction;
import unaj.subastaya.model.User;
import unaj.subastaya.model.Wallet;
import unaj.subastaya.repository.BidRepository;
import unaj.subastaya.repository.LedgerTransactionRepository;
import unaj.subastaya.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EscrowServiceTest {

    @Test
    void WithholdingFundsWhenMakingAnOffer() {

        BidRepository bidRepository = mock(BidRepository.class);
        WalletRepository walletRepository = mock(WalletRepository.class);
        LedgerTransactionRepository ledgerTransactionRepository =
                mock(LedgerTransactionRepository.class);

        EscrowService escrowService = new EscrowService(
                bidRepository,
                walletRepository,
                ledgerTransactionRepository
        );

        User bidder = new User();
        bidder.setId(1L);
        bidder.setName("Usuario");

        Auction auction = new Auction();
        auction.setId(100L);

        Wallet wallet = new Wallet();
        wallet.setUser(bidder);
        wallet.setTotalBalance(new BigDecimal("50000"));
        wallet.setRetainedBalance(BigDecimal.ZERO);
        wallet.setAvailableBalance(new BigDecimal("50000"));

        when(bidRepository.findHighestBid(100L))
                .thenReturn(Optional.empty());

        when(walletRepository.findByUser_Id(1L))
                .thenReturn(Optional.of(wallet));

        escrowService.processEscrow(
                auction,
                bidder,
                new BigDecimal("10000")
        );

        assertEquals(
                new BigDecimal("50000"),
                wallet.getTotalBalance()
        );

        assertEquals(
                new BigDecimal("10000"),
                wallet.getRetainedBalance()
        );

        assertEquals(
                new BigDecimal("40000"),
                wallet.getAvailableBalance()
        );

        verify(walletRepository).save(wallet);

        verify(ledgerTransactionRepository)
                .save(any(LedgerTransaction.class));
    }

    @Test
    void ReleasePreviousOfferAndRetainNewOne() {

        BidRepository bidRepository = mock(BidRepository.class);
        WalletRepository walletRepository = mock(WalletRepository.class);
        LedgerTransactionRepository ledgerTransactionRepository =
                mock(LedgerTransactionRepository.class);

        EscrowService escrowService = new EscrowService(
                bidRepository,
                walletRepository,
                ledgerTransactionRepository
        );

        User previousBidder = new User();
        previousBidder.setId(1L);
        previousBidder.setName("Usuario A");

        User newBidder = new User();
        newBidder.setId(2L);
        newBidder.setName("Usuario B");

        Auction auction = new Auction();
        auction.setId(100L);

        Wallet previousWallet = new Wallet();
        previousWallet.setUser(previousBidder);
        previousWallet.setTotalBalance(new BigDecimal("50000"));
        previousWallet.setRetainedBalance(new BigDecimal("10000"));
        previousWallet.setAvailableBalance(new BigDecimal("40000"));

        Wallet newWallet = new Wallet();
        newWallet.setUser(newBidder);
        newWallet.setTotalBalance(new BigDecimal("50000"));
        newWallet.setRetainedBalance(BigDecimal.ZERO);
        newWallet.setAvailableBalance(new BigDecimal("50000"));

        Bid previousBid = new Bid();
        previousBid.setId(1L);
        previousBid.setAuction(auction);
        previousBid.setBidder(previousBidder);
        previousBid.setAmount(new BigDecimal("10000"));

        when(bidRepository.findHighestBid(100L))
                .thenReturn(Optional.of(previousBid));

        when(walletRepository.findByUser_Id(1L))
                .thenReturn(Optional.of(previousWallet));

        when(walletRepository.findByUser_Id(2L))
                .thenReturn(Optional.of(newWallet));

        escrowService.processEscrow(
                auction,
                newBidder,
                new BigDecimal("15000")
        );

        // Wallet del usuario anterior
        assertEquals(
                new BigDecimal("0"),
                previousWallet.getRetainedBalance()
        );

        assertEquals(
                new BigDecimal("50000"),
                previousWallet.getAvailableBalance()
        );

        // Wallet del nuevo usuario
        assertEquals(
                new BigDecimal("15000"),
                newWallet.getRetainedBalance()
        );

        assertEquals(
                new BigDecimal("35000"),
                newWallet.getAvailableBalance()
        );

        // Se guardan ambas wallets
        verify(walletRepository, times(2))
                .save(any(Wallet.class));

        // RELEASE + HOLD
        verify(ledgerTransactionRepository, times(2))
                .save(any(LedgerTransaction.class));
    }
}