package unaj.subastaya.controller;

import unaj.subastaya.model.LedgerTransaction;
import unaj.subastaya.model.Wallet;
import unaj.subastaya.repository.LedgerTransactionRepository;
import unaj.subastaya.repository.WalletRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletRepository walletRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;

    public WalletController(
            WalletRepository walletRepository,
            LedgerTransactionRepository ledgerTransactionRepository) {
        this.walletRepository = walletRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getWalletByUserId(@PathVariable Long userId) {
        Wallet wallet = walletRepository.findByUser_Id(userId)
                .orElse(null);

        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> walletData = new HashMap<>();
        walletData.put("id", wallet.getId());
        walletData.put("userId", wallet.getUser().getId());
        walletData.put("userName", wallet.getUser().getName());
        walletData.put("totalBalance", wallet.getTotalBalance());
        walletData.put("retainedBalance", wallet.getRetainedBalance());
        walletData.put("availableBalance", wallet.getAvailableBalance());

        return ResponseEntity.ok(walletData);
    }

    // Endpoint para consultar el desglose de saldos de una billetera
    @GetMapping("/{id}/balance")
    public ResponseEntity<Map<String, BigDecimal>> getWalletBalance(
            @PathVariable Long id) {

        Wallet wallet = walletRepository.findById(id)
                .orElse(null);

        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }

        // Saldo disponible = Saldo Total - Saldo Retenido
        BigDecimal availableBalance = wallet.getTotalBalance()
                .subtract(wallet.getRetainedBalance());

        // Devolvemos un objeto JSON con los tres saldos
        Map<String, BigDecimal> balances = new HashMap<>();

        balances.put("totalBalance", wallet.getTotalBalance());
        balances.put("retainedBalance", wallet.getRetainedBalance());
        balances.put("availableBalance", availableBalance);

        return ResponseEntity.ok(balances);
    }

    // Endpoint para realizar una carga de saldo simulada
    @PostMapping("/{id}/deposits")
    public ResponseEntity<Wallet> depositMoney(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {

        Wallet wallet = walletRepository.findById(id)
                .orElse(null);

        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().build();
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

        return ResponseEntity.status(201).body(updatedWallet);
    }
    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getWalletTransactions(
            @PathVariable Long id) {

        // Verificar que la billetera exista
        if (!walletRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(
                ledgerTransactionRepository.findByWalletId(id)
        );
    }
}
