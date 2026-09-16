package unaj.subastaya.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import unaj.subastaya.model.*;
import unaj.subastaya.repository.BidRepository;
import unaj.subastaya.repository.LedgerTransactionRepository;
import unaj.subastaya.repository.WalletRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class EscrowService {

    private final BidRepository bidRepository;
    private final WalletRepository walletRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;

    public EscrowService(
            BidRepository bidRepository,
            WalletRepository walletRepository,
            LedgerTransactionRepository ledgerTransactionRepository) {

        this.bidRepository = bidRepository;
        this.walletRepository = walletRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
    }

    @Transactional
    public void processEscrow(
            Auction auction,
            User newBidder,
            BigDecimal amount) {

        // Buscar la puja más alta ANTERIOR
        Bid beforeHighestBid = bidRepository
                .findHighestBid(auction.getId())
                .orElse(null);

        if (beforeHighestBid != null) {

            User oldBidder = beforeHighestBid.getBidder();

            //si hay una nueva puja
            if (!Objects.equals(
                    oldBidder.getId(),
                    newBidder.getId())) {

                Wallet previousWallet =
                        walletRepository.findByUser(oldBidder);

                BigDecimal previousAmount =
                        beforeHighestBid.getAmount();

                // retengo el monto de la puja
                previousWallet.setRetainedBalance(
                        previousWallet
                                .getRetainedBalance()
                                .subtract(previousAmount)
                );

                // liberamos el monto de la puja
                previousWallet.setAvailableBalance(
                        previousWallet
                                .getAvailableBalance()
                                .add(previousAmount)
                );

                walletRepository.save(previousWallet);

                // Registrar la liberación en el Ledger
                LedgerTransaction releaseTransaction =
                        new LedgerTransaction();

                releaseTransaction.setWallet(previousWallet);
                releaseTransaction.setType("RELEASE");
                releaseTransaction.setAmount(previousAmount);
                releaseTransaction.setDate(LocalDateTime.now());
                releaseTransaction.setAuctionId(auction.getId());

                ledgerTransactionRepository.save(releaseTransaction);
            }
        }


        Wallet newBidderWallet =
                walletRepository.findByUser(newBidder);


        // Verificar que tenga saldo suficiente
        if (newBidderWallet
                .getAvailableBalance()
                .compareTo(amount) < 0) {

            throw new IllegalArgumentException(
                    "El usuario no tiene saldo disponible suficiente"
            );
        }


        // Restar el monto del saldo disponible
        newBidderWallet.setAvailableBalance(
                newBidderWallet
                        .getAvailableBalance()
                        .subtract(amount)
        );

        // Agregar el monto al saldo retenido
        newBidderWallet.setRetainedBalance(
                newBidderWallet
                        .getRetainedBalance()
                        .add(amount)
        );

        walletRepository.save(newBidderWallet);

        // Registrar la retención en el Ledger
        LedgerTransaction holdTransaction =
                new LedgerTransaction();

        holdTransaction.setWallet(newBidderWallet);
        holdTransaction.setType("HOLD");
        holdTransaction.setAmount(amount);
        holdTransaction.setDate(LocalDateTime.now());
        holdTransaction.setAuctionId(auction.getId());

        ledgerTransactionRepository.save(holdTransaction);
    }
}