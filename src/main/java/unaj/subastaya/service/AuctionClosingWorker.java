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
    //searh for auctions that are expired
    public void closeExpiredAuctions() {
        LocalDateTime now = LocalDateTime.now();
        List<Auction> expiredAuctions = auctionRepository.findByStateAndEndDateBefore("ACTIVE", now);

        if (expiredAuctions.isEmpty()) {
            return;
        }
        log.info("Worker Job: Procesando {} subastas vencidas", expiredAuctions.size());
        //process each auction that is expired
        for (Auction auction : expiredAuctions) {
            processAuctionClosing(auction);
        }
    }

    private void processAuctionClosing(Auction auction) {

        Optional<Bid> winningBidOpt = bidRepository.findHighestBid(auction.getId());

        //if no found bids, set the state to UNSOLD
        if (winningBidOpt.isEmpty()) {
            auction.setState("UNSOLD");
            auctionRepository.save(auction);
            log.info("Auditoría: Subasta id={} marcada como DESSERT (sin ofertas)", auction.getId());
            return;
        }
        //if found bids, set the state to FINISHED
        Bid winningBid = winningBidOpt.get();
        BigDecimal winningAmount = winningBid.getAmount();
        Long winnerId = winningBid.getBidder().getId();
        Long buyerId = auction.getBuyer().getId();
        Long vendorId = auction.getVendor().getId();

        // debit the amount to the winner
        Wallet winnerWallet = walletRepository.findByUser(winningBid.getBidder());
        if (winnerWallet == null) {
            throw new IllegalStateException("Billetera del comprador no encontrada id=" + winnerId);
        }
        winnerWallet.setRetainedBalance(winnerWallet.getRetainedBalance().subtract(winningAmount));
        winnerWallet.setTotalBalance(winnerWallet.getTotalBalance().subtract(winningAmount));
        walletRepository.save(winnerWallet);

        //Save the debit in ledgerTransaction
        LocalDateTime now = LocalDateTime.now();

        LedgerTransaction debitTx = new LedgerTransaction();
        debitTx.setWallet(winnerWallet);
        debitTx.setAuctionId(auction.getId());
        debitTx.setAmount(winningAmount.negate());
        debitTx.setType("PAYMENT");
        debitTx.setDate(now);
        ledgerTransactionRepository.save(debitTx);

        // credit the bid amount to the vendor
        Wallet vendorWallet = walletRepository.findByUser(auction.getVendor());
        if (vendorWallet == null) {
            throw new IllegalStateException("Billetera del vendedor no encontrada id=" + vendorId);
        }
        vendorWallet.setTotalBalance(vendorWallet.getTotalBalance().add(winningAmount));
        vendorWallet.setAvailableBalance(vendorWallet.getAvailableBalance().add(winningAmount));
        walletRepository.save(vendorWallet);

        //Save the credit in ledgerTransaction
        LedgerTransaction creditTx = new LedgerTransaction();
        creditTx.setWallet(vendorWallet);
        creditTx.setAuctionId(auction.getId());
        creditTx.setAmount(winningAmount);
        creditTx.setType("CHARGE");
        creditTx.setDate(now);
        ledgerTransactionRepository.save(creditTx);

        // set the state to FINISHED
        auction.setState("FINISHED");
        auctionRepository.save(auction);

        log.info("Auditoría Venta: Subasta id={} FINISHED. Ganador id={}, Vendedor id={}, Monto={}",
                auction.getId(), winnerId, vendorId, winningAmount);
    }
}