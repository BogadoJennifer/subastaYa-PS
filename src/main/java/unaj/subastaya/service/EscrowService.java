package unaj.subastaya.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unaj.subastaya.repository.BidRepository;
import unaj.subastaya.repository.LedgerTransactionRepository;
import unaj.subastaya.repository.WalletRepository;
import unaj.subastaya.model.Bid;
import unaj.subastaya.model.Wallet;
import java.math.BigDecimal;
import unaj.subastaya.model.LedgerTransaction;
import java.time.LocalDateTime;

@Service
public class EscrowService {

    private final WalletRepository walletRepository;
    private final BidRepository bidRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;

    public EscrowService(
            WalletRepository walletRepository,
            BidRepository bidRepository,
            LedgerTransactionRepository ledgerTransactionRepository) {

        this.walletRepository = walletRepository;
        this.bidRepository = bidRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
    }

    @Transactional
    public void processEscrow(
            Long auctionId,
            Long newBidderId,
            java.math.BigDecimal bidAmount) {

        Bid highestBid;
        highestBid = bidRepository
                .findTopByAuctionIdOrderByAmountDesc(auctionId)
                .orElse(null);

        // Buscar la billetera del líder anterior
        if (highestBid != null) {

            Long previousBidderId = highestBid
                    .getBidder()
                    .getId();

            Wallet previousBidderWallet =
                    walletRepository.findByUserId(previousBidderId);

            if (previousBidderWallet == null) {
                throw new IllegalArgumentException(
                        "Billetera del líder anterior no encontrada");
            }

            // Monto que tenía retenido por esta puja
            BigDecimal previousAmount = highestBid.getAmount();

            // Liberar ese monto
            BigDecimal newRetainedBalance =
                    previousBidderWallet.getRetainedBalance()
                            .subtract(previousAmount);

            // Verificar que el saldo retenido no quede negativo
            if (newRetainedBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException(
                        "El saldo retenido del líder anterior es inconsistente");
            }

            // Actualizar saldo retenido
            previousBidderWallet.setRetainedBalance(
                    newRetainedBalance
            );

            // Recalcular saldo disponible
            previousBidderWallet.setAvailableBalance(
                    previousBidderWallet.getTotalBalance()
                            .subtract(newRetainedBalance)
            );

            // Guardar la billetera actualizada
            walletRepository.save(previousBidderWallet);

            // Registrar la liberación en el Ledger
            LedgerTransaction releaseTransaction =
                    new LedgerTransaction();

            releaseTransaction.setWallet(previousBidderWallet);
            releaseTransaction.setType("RELEASE");
            releaseTransaction.setAmount(previousAmount);
            releaseTransaction.setDate(LocalDateTime.now());
            releaseTransaction.setAuctionId(auctionId);

            ledgerTransactionRepository.save(releaseTransaction);
        }
        // Buscar la billetera del nuevo líder
        Wallet newBidderWallet =
                walletRepository.findByUserId(newBidderId);

        if (newBidderWallet == null) {
            throw new IllegalArgumentException(
                    "Billetera del nuevo líder no encontrada");
        }


    // Verificar que el nuevo líder tenga saldo disponible suficiente
        if (newBidderWallet.getAvailableBalance()
                .compareTo(bidAmount) < 0) {

            throw new IllegalStateException(
                    "Saldo insuficiente para retener la oferta");
        }


        // Retener el monto de la nueva oferta
        BigDecimal newRetainedBalance =
                newBidderWallet.getRetainedBalance()
                        .add(bidAmount);

        // Actualizar saldo retenido
        newBidderWallet.setRetainedBalance(
                newRetainedBalance
        );

        // Recalcular saldo disponible
        newBidderWallet.setAvailableBalance(
                newBidderWallet.getTotalBalance()
                        .subtract(newRetainedBalance)
        );

        // Guardar la billetera actualizada
        walletRepository.save(newBidderWallet);


        // Registrar la retención en el Ledger
        LedgerTransaction holdTransaction =
                new LedgerTransaction();

        holdTransaction.setWallet(newBidderWallet);
        holdTransaction.setType("HOLD");
        holdTransaction.setAmount(bidAmount);
        holdTransaction.setDate(LocalDateTime.now());
        holdTransaction.setAuctionId(auctionId);

        ledgerTransactionRepository.save(holdTransaction);

    }
    }


