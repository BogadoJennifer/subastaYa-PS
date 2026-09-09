package unaj.subastaya.service;
import unaj.subastaya.model.Auction;
import unaj.subastaya.model.Wallet;
import unaj.subastaya.repository.AuctionRepository;
import unaj.subastaya.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

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

        LocalDateTime now = LocalDateTime.now();

        // 1. Validar que la subasta siga activa temporalmente y por estado
        if (!"ACTIVE".equalsIgnoreCase(auction.getState()) || now.isAfter(auction.getEndDate())) {
            throw new IllegalStateException("La subasta no está activa o ya ha finalizado");
        }

        // 2. Validar que el comprador no sea el vendedor
        if (auction.getBuyer() != null && auction.getBuyer().getId().equals(buyerId)) {
            throw new IllegalArgumentException("El vendedor no puede ofertar en su propia subasta");
        }

        // 3. Validar incremento mínimo respecto al precio base
        if (bidAmount.compareTo(auction.getBasePrice().add(auction.getMinimumIncrement())) < 0) {
            throw new IllegalArgumentException("El monto ofertado no supera el incremento mínimo requerido");
        }

        // 4. Validar billetera y saldo
        Wallet walletBuyer = walletRepository.findByUserId(buyerId);
        if (walletBuyer == null) {
            throw new IllegalArgumentException("Billetera no encontrada");
        }

        if (walletBuyer.getAvailableBalance().compareTo(bidAmount) < 0) {
            throw new IllegalStateException("Saldo insuficiente para ofertar");
        }

        // 5. Anti-Sniping Rule: si restan <= 60 segundos, se extiende 2 minutos
        long secondsRemaining = Duration.between(now, auction.getEndDate()).getSeconds();
        if (secondsRemaining <= 60 && secondsRemaining >= 0) {
            auction.setEndDate(auction.getEndDate().plusMinutes(2));
        }

        // 6. Actualizar saldos contables de la billetera
        BigDecimal newRetainedBalance = walletBuyer.getRetainedBalance().add(bidAmount);
        walletBuyer.setRetainedBalance(newRetainedBalance);
        walletBuyer.setAvailableBalance(walletBuyer.getTotalBalance().subtract(newRetainedBalance));

        walletRepository.save(walletBuyer);
        auctionRepository.save(auction);
    }
}