package unaj.subastaya.controller;

import unaj.subastaya.model.Wallet;
import unaj.subastaya.repository.WalletRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletRepository walletRepository;

    public WalletController(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    // Endpoint para consultar el desglose de saldos de una billetera
    @GetMapping("/{id}/balance")
    public ResponseEntity<Map<String, BigDecimal>> getWalletBalance(
            @PathVariable Long id) {

        Wallet wallet = walletRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Wallet not found with id: " + id));

        // El saldo disponible se calcula como:
        // Saldo Total - Saldo Retenido
        BigDecimal availableBalance = wallet.getTotalBalance()
                .subtract(wallet.getRetainedBalance());

        // Devolvemos un objeto JSON con los tres saldos
        Map<String, BigDecimal> balances = new HashMap<>();

        balances.put("totalBalance", wallet.getTotalBalance());
        balances.put("retainedBalance", wallet.getRetainedBalance());
        balances.put("availableBalance", availableBalance);

        return ResponseEntity.ok(balances);
    }
}
