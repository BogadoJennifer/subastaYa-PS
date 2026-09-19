package unaj.subastaya.controller;

import unaj.subastaya.model.LedgerTransaction;
import unaj.subastaya.model.Wallet;
import unaj.subastaya.repository.LedgerTransactionRepository;
import unaj.subastaya.repository.WalletRepository;
import unaj.subastaya.service.WalletService;
import unaj.subastaya.dto.WalletSummaryDto;
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
    private final WalletService walletService;
    public WalletController(
            WalletRepository walletRepository,
            LedgerTransactionRepository ledgerTransactionRepository,
            WalletService walletService) {
        this.walletRepository = walletRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.walletService = walletService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<WalletSummaryDto> getWalletByUserId(
            @PathVariable Long userId
    ) {
        return ResponseEntity.ok(
                walletService.getWalletByUserId(userId)
        );
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


    @PostMapping("/{id}/deposits")
    public ResponseEntity<Wallet> depositMoney(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {

        Wallet updatedWallet = walletService.depositMoney(id, amount);

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
