package unaj.subastaya.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unaj.subastaya.dto.WalletSummaryDto;
import unaj.subastaya.exception.ResourceNotFoundException;
import unaj.subastaya.model.LedgerTransaction;
import unaj.subastaya.model.Wallet;
import unaj.subastaya.repository.LedgerTransactionRepository;
import unaj.subastaya.repository.WalletRepository;
import unaj.subastaya.model.AuditLog;
import unaj.subastaya.repository.AuditLogRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final AuditLogRepository auditLogRepository;

    public WalletService(
            WalletRepository walletRepository,
            LedgerTransactionRepository ledgerTransactionRepository,
            AuditLogRepository auditLogRepository
    ) {
        this.walletRepository = walletRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.auditLogRepository = auditLogRepository;
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

        LocalDateTime now = LocalDateTime.now();

        LedgerTransaction transaction = new LedgerTransaction();

        transaction.setWallet(updatedWallet);
        transaction.setType("DEPOSIT");
        transaction.setAmount(amount);
        transaction.setDate(now);

        ledgerTransactionRepository.save(transaction);

        AuditLog auditLog = new AuditLog();

        auditLog.setEntity("WALLET");
        auditLog.setEntityId(updatedWallet.getId());
        auditLog.setAction("MANUAL_DEPOSIT");
        auditLog.setUserId(updatedWallet.getUser().getId());
        auditLog.setDate(now);
        auditLog.setDetailJson("{\"amount\":" + amount + "}");

        auditLogRepository.save(auditLog);

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