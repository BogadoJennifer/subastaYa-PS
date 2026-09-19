package unaj.subastaya.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unaj.subastaya.dto.WalletSummaryDto;
import unaj.subastaya.exception.ResourceNotFoundException;
import unaj.subastaya.model.LedgerTransaction;
import unaj.subastaya.model.Wallet;
import unaj.subastaya.repository.LedgerTransactionRepository;
import unaj.subastaya.repository.WalletRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;

    public WalletService(
            WalletRepository walletRepository,
            LedgerTransactionRepository ledgerTransactionRepository
    ) {
        this.walletRepository = walletRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
    }

    public WalletSummaryDto getWalletByUserId(Long userId) {
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Billetera no encontrada")
                );

        return toSummary(wallet);
    }

    @Transactional
    public Wallet depositMoney(Long walletId, BigDecimal amount) {

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Billetera no encontrada")
                );

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El monto debe ser mayor a cero"
            );
        }

        wallet.setTotalBalance(
                wallet.getTotalBalance().add(amount)
        );

        wallet.setAvailableBalance(
                wallet.getTotalBalance()
                        .subtract(wallet.getRetainedBalance())
        );

        Wallet updatedWallet = walletRepository.save(wallet);

        LedgerTransaction transaction = new LedgerTransaction();

        transaction.setWallet(updatedWallet);
        transaction.setType("DEPOSIT");
        transaction.setAmount(amount);
        transaction.setDate(LocalDateTime.now());

        ledgerTransactionRepository.save(transaction);

        return updatedWallet;
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