package unaj.subastaya.service;

import org.junit.jupiter.api.Test;
import unaj.subastaya.model.LedgerTransaction;
import unaj.subastaya.model.User;
import unaj.subastaya.model.Wallet;
import unaj.subastaya.repository.AuditLogRepository;
import unaj.subastaya.repository.LedgerTransactionRepository;
import unaj.subastaya.repository.WalletRepository;
import unaj.subastaya.repository.AuditLogRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

class WalletServiceTest {

    @Test
    void DepositWallet() {

        WalletRepository walletRepository = mock(WalletRepository.class);
        LedgerTransactionRepository ledgerTransactionRepository =
                mock(LedgerTransactionRepository.class);
        AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);


        WalletService walletService = new WalletService(
                walletRepository,
                ledgerTransactionRepository,
                auditLogRepository
        );

        User user = new User();
        user.setId(1L);
        user.setName("Usuario");

        Wallet wallet = new Wallet();
        wallet.setId(10L);
        wallet.setUser(user);
        wallet.setTotalBalance(new BigDecimal("50000"));
        wallet.setRetainedBalance(new BigDecimal("10000"));
        wallet.setAvailableBalance(new BigDecimal("40000"));

        when(walletRepository.findById(10L))
                .thenReturn(Optional.of(wallet));

        when(walletRepository.save(wallet))
                .thenReturn(wallet);

        Wallet result = walletService.depositMoney(
                10L,
                new BigDecimal("20000")
        );

        assertEquals(
                new BigDecimal("70000"),
                result.getTotalBalance()
        );

        assertEquals(
                new BigDecimal("10000"),
                result.getRetainedBalance()
        );

        assertEquals(
                new BigDecimal("60000"),
                result.getAvailableBalance()
        );

        verify(walletRepository).save(wallet);

        ArgumentCaptor<LedgerTransaction> transactionCaptor =
                ArgumentCaptor.forClass(LedgerTransaction.class);

        verify(ledgerTransactionRepository)
                .save(transactionCaptor.capture());

        LedgerTransaction transaction =
                transactionCaptor.getValue();

        assertEquals("DEPOSIT", transaction.getType());

        assertEquals(
                new BigDecimal("20000"),
                transaction.getAmount()
        );

        assertEquals(wallet, transaction.getWallet());

        assertNotNull(transaction.getDate());

        System.out.println("Saldo Total: " + wallet.getTotalBalance());
        System.out.println("Saldo Disponible: " + wallet.getAvailableBalance());
        System.out.println("Saldo Retenido: " + wallet.getRetainedBalance());
    }

}
