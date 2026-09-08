package unaj.subastaya.service;
import unaj.subastaya.model.Auction;
import unaj.subastaya.model.Wallet;
import unaj.subastaya.repository.AuctionRepository;
import unaj.subastaya.repository.WalletRepository;
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

        Wallet walletBuyer = walletRepository.findByUserId(buyerId);
        if (walletBuyer == null) {
            throw new IllegalArgumentException("Billetera no encontrada");
        }

        // Validar saldo disponible
        if (walletBuyer.getAvailableBalance().compareTo(bidAmount) < 0) {
            throw new IllegalStateException("Saldo insuficiente para ofertar");
        }

        // Actualizar saldos de la billetera
        BigDecimal newRetainedBalance = walletBuyer.getRetainedBalance().add(bidAmount);
        walletBuyer.setRetainedBalance(newRetainedBalance);
        walletBuyer.setAvailableBalance(walletBuyer.getTotalBalance().subtract(newRetainedBalance));

        walletRepository.save(walletBuyer);
        auctionRepository.save(auction);
    }
}