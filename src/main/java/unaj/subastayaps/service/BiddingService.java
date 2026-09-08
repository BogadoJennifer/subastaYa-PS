package unaj.subastayaps.service;
import unaj.subastayaps.model.Auction;
import unaj.subastayaps.model.Wallet;
import unaj.subastayaps.repository.AuctionRepository;
import unaj.subastayaps.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class BiddingService {

    private final AuctionRepository auctionRepository;
    private final WalletRepository walletRepository;

    public BiddingService(AuctionRepository auctionRepository, WalletRepository walletRepository) {
        this.auctionRepository = auctionRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public void registerBid(Long auctionId, Long buyerId, BigDecimal bidAmount) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Subasta no encontrada"));

        Wallet walletBuyer = walletRepository.findByUserId(buyerId)
                .orElseThrow(() -> new IllegalArgumentException("Billetera no encontrada"));

        // Validar saldo disponible
        if (walletBuyer.getAvailableBalance().compareTo(bidAmount) < 0) {
            throw new IllegalStateException("Saldo insuficiente para ofertar");
        }

        // Actualizar saldos de la billetera
        walletBuyer.setRetainedBalance(walletBuyer.getRetainedBalance().add(bidAmount));
        walletBuyer.setAvailableBalance(walletBuyer.getTotalBalance().subtract(walletBuyer.getRetainedBalance()));

        // Al guardar, Hibernate compara: UPDATE billeteras SET ... WHERE id = ? AND version = ?
        walletRepository.save(walletBuyer);
        auctionRepository.save(auction);
    }
}