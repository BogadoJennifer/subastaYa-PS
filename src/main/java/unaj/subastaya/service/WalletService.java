package unaj.subastaya.service;

import org.springframework.stereotype.Service;
import unaj.subastaya.dto.WalletSummaryDto;
import unaj.subastaya.exception.ResourceNotFoundException;
import unaj.subastaya.model.Wallet;
import unaj.subastaya.repository.WalletRepository;

import java.math.BigDecimal;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public WalletSummaryDto getWalletByUserId(Long userId) {
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Billetera no encontrada")
                );

        return toSummary(wallet);
    }

    private WalletSummaryDto toSummary(Wallet wallet) {
        BigDecimal availableBalance = wallet.getTotalBalance()
                .subtract(wallet.getRetainedBalance());

        return new WalletSummaryDto(
                wallet.getId(),
                wallet.getUser().getId(),
                wallet.getUser().getName(),
                wallet.getTotalBalance(),
                wallet.getRetainedBalance(),
                availableBalance
        );
    }
}