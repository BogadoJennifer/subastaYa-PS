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

    // Endpoint para consultar el desglose de saldos de una billetera
    @GetMapping("/{id}/balance")
    public ResponseEntity<Map<String, BigDecimal>> getWalletBalance(
            @PathVariable Long id) {

        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Wallet not found with id: " + id));

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
    @PostMapping("/{id}/deposit")
    public ResponseEntity<Wallet> depositMoney(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {

        // 1. Buscar la billetera
        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Wallet not found with id: " + id));

        // 2. Validar que el monto sea mayor que cero
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().build();
        }

        // 3. Aumentar el saldo total
        wallet.setTotalBalance(
                wallet.getTotalBalance().add(amount)
        );

        // 4. Recalcular el saldo disponible
        // Disponible = Total - Retenido
        wallet.setAvailableBalance(
                wallet.getTotalBalance()
                        .subtract(wallet.getRetainedBalance())
        );

        // 5. Guardar la wallet actualizada
        Wallet updatedWallet = walletRepository.save(wallet);

        // 6. Crear el registro de la operación en el Ledger
        LedgerTransaction transaction = new LedgerTransaction();

        transaction.setWallet(updatedWallet);
        transaction.setType("DEPOSIT");
        transaction.setAmount(amount);
        transaction.setDate(LocalDateTime.now());

        // 7. Guardar la transacción
        ledgerTransactionRepository.save(transaction);

        // 8. Devolver la wallet actualizada
        return ResponseEntity.ok(updatedWallet);
    }
}
