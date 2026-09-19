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
}