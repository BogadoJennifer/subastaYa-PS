package unaj.subastaya.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unaj.subastaya.exception.InsufficientFundsException;
import unaj.subastaya.exception.InvalidBidAmountException;
import unaj.subastaya.exception.ResourceNotFoundException;
import unaj.subastaya.model.Auction;
import unaj.subastaya.model.Bid;
import unaj.subastaya.model.LedgerTransaction;
import unaj.subastaya.model.User;
import unaj.subastaya.model.Wallet;
import unaj.subastaya.repository.BidRepository;
import unaj.subastaya.repository.LedgerTransactionRepository;
import unaj.subastaya.repository.WalletRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class EscrowService {

    private final BidRepository bidRepository;
    private final WalletRepository walletRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;

    public EscrowService(
            BidRepository bidRepository,
            WalletRepository walletRepository,
            LedgerTransactionRepository ledgerTransactionRepository
    ) {
        this.bidRepository = bidRepository;
        this.walletRepository = walletRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
    }

    @Transactional
    public void processEscrow(
            Auction auction,
            User newBidder,
            BigDecimal amount
    ) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidBidAmountException(
                    "El importe de la oferta debe ser positivo"
            );
        }

        Bid previousHighestBid = bidRepository
                .findHighestBid(auction.getId())
                .orElse(null);

        // Release the previous winning bid, even for the same bidder.
        if (previousHighestBid != null) {
            Wallet previousWallet = findWallet(
                    previousHighestBid.getBidder().getId()
            );

            BigDecimal previousAmount = previousHighestBid.getAmount();

            if (previousWallet.getRetainedBalance()
                    .compareTo(previousAmount) < 0) {
                throw new IllegalStateException(
                        "El saldo retenido no cubre la oferta anterior"
                );
            }

            previousWallet.setRetainedBalance(
                    previousWallet.getRetainedBalance()
                            .subtract(previousAmount)
            );

            updateAvailableBalance(previousWallet);
            walletRepository.save(previousWallet);

            recordTransaction(
                    previousWallet,
                    auction.getId(),
                    "RELEASE",
                    previousAmount
            );
        }

        Wallet bidderWallet = findWallet(newBidder.getId());

        updateAvailableBalance(bidderWallet);

        if (bidderWallet.getAvailableBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    "Saldo insuficiente para ofertar"
            );
        }

        // Hold the full amount of the new winning bid.
        bidderWallet.setRetainedBalance(
                bidderWallet.getRetainedBalance().add(amount)
        );

        updateAvailableBalance(bidderWallet);
        walletRepository.save(bidderWallet);

        recordTransaction(
                bidderWallet,
                auction.getId(),
                "HOLD",
                amount
        );
    }

    private Wallet findWallet(Long userId) {
        return walletRepository.findByUser_Id(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Billetera no encontrada para el usuario " + userId
                        )
                );
    }

    private void updateAvailableBalance(Wallet wallet) {
        wallet.setAvailableBalance(
                wallet.getTotalBalance()
                        .subtract(wallet.getRetainedBalance())
        );
    }

    private void recordTransaction(
            Wallet wallet,
            Long auctionId,
            String type,
            BigDecimal amount
    ) {
        LedgerTransaction transaction = new LedgerTransaction();
        transaction.setWallet(wallet);
        transaction.setAuctionId(auctionId);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setDate(LocalDateTime.now());

        ledgerTransactionRepository.save(transaction);
    }
}