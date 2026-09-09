package unaj.subastaya.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import unaj.subastaya.model.Auction;
import unaj.subastaya.model.Bid;
import unaj.subastaya.model.LedgerTransaction;
import unaj.subastaya.model.Wallet;
import unaj.subastaya.repository.AuctionRepository;
import unaj.subastaya.repository.BidRepository;
import unaj.subastaya.repository.LedgerTransactionRepository;
import unaj.subastaya.repository.WalletRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class AuctionClosingWorker {

    private static final Logger log = LoggerFactory.getLogger(AuctionClosingWorker.class);

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final WalletRepository walletRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;

    public AuctionClosingWorker(AuctionRepository auctionRepository,
                                BidRepository bidRepository,
                                WalletRepository walletRepository,
                                LedgerTransactionRepository ledgerTransactionRepository) {
        this.auctionRepository = auctionRepository;
        this.bidRepository = bidRepository;
        this.walletRepository = walletRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
    }

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void closeExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Auction> expiredAuctions = auctionRepository.findByStateAndEndDateBefore("ACTIVE", now);

        if (expiredAuctions.isEmpty()) {
            return;
        }

        log.info("Worker Job: Procesando {} subastas vencidas", expiredAuctions.size());

        for (Auction auction : expiredAuctions) {
            processAuctionClosing(auction);
        }
    }

    private void processAuctionClosing(Auction auction) {
        // Buscar la puja ganadora directamente desde la base de datos
        Optional<Bid> winningBidOpt = bidRepository.findTopByAuctionIdOrderByAmountDesc(auction.getId());

        if (winningBidOpt.isEmpty()) {
            auction.setState("DESIERTA");
            auctionRepository.save(auction);
            log.info("Auditoría: Subasta id={} marcada como DESIERTA (sin ofertas)", auction.getId());
            return;
        }

        Bid winningBid = winningBidOpt.get();
        BigDecimal winningAmount = winningBid.getAmount();
        Long winnerId = winningBid.getBidder().getId();
        Long buyerId = auction.getBuyer().getId();

        // 1. Debitar al Comprador (ganador)
        Wallet winnerWallet = walletRepository.findByUserId(winnerId);
        if (winnerWallet == null) {
            throw new IllegalStateException("Billetera del comprador no encontrada id=" + winnerId);
        }
        winnerWallet.setRetainedBalance(winnerWallet.getRetainedBalance().subtract(winningAmount));
        winnerWallet.setTotalBalance(winnerWallet.getTotalBalance().subtract(winningAmount));
        walletRepository.save(winnerWallet);

        // 2. Acreditar al Vendedor
        Wallet buyerWallet = walletRepository.findByUserId(buyerId);
        if (buyerWallet == null) {
            throw new IllegalStateException("Billetera del comprador no encontrada id=" + buyerId);
        }
        buyerWallet.setTotalBalance(buyerWallet.getTotalBalance().add(winningAmount));
        buyerWallet.setAvailableBalance(buyerWallet.getAvailableBalance().add(winningAmount));
        walletRepository.save(buyerWallet);

        // 3. Escribir asientos contables en Ledger (usando setAuctionId)
        LocalDateTime now = LocalDateTime.now();

        LedgerTransaction debitTx = new LedgerTransaction();
        debitTx.setWallet(winnerWallet);
        debitTx.setAuctionId(auction.getId());
        debitTx.setAmount(winningAmount.negate());
        debitTx.setType("AUCTION_PAYMENT");
        debitTx.setDate(now);
        ledgerTransactionRepository.save(debitTx);

        LedgerTransaction creditTx = new LedgerTransaction();
        creditTx.setWallet(buyerWallet);
        creditTx.setAuctionId(auction.getId());
        creditTx.setAmount(winningAmount);
        creditTx.setType("AUCTION_SALE");
        creditTx.setDate(now);
        ledgerTransactionRepository.save(creditTx);

        // 4. Marcar como FINALIZADA
        auction.setState("FINALIZADA");
        auctionRepository.save(auction);

        log.info("Auditoría Venta: Subasta id={} FINALIZADA. Ganador id={}, Comprador id={}, Monto={}",
                auction.getId(), winnerId, buyerId, winningAmount);
    }
}