package unaj.subastaya.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import unaj.subastaya.dto.WalletSummaryDto;
import unaj.subastaya.model.User;
import unaj.subastaya.model.Wallet;
import unaj.subastaya.repository.LedgerTransactionRepository;
import unaj.subastaya.repository.WalletRepository;
import unaj.subastaya.service.WalletService;

import java.math.BigDecimal;

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
            WalletService walletService
    ) {
        this.walletRepository = walletRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
        this.walletService = walletService;
    }

    @GetMapping("/me")
    public ResponseEntity<WalletSummaryDto> getMyWallet(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        return ResponseEntity.ok(
                walletService.getWalletByUserId(authenticatedUser.getId())
        );
    }

    @PostMapping("/me/deposits")
    public ResponseEntity<WalletSummaryDto> depositMoney(
            @AuthenticationPrincipal User authenticatedUser,
            @RequestParam BigDecimal amount
    ) {
        WalletSummaryDto updatedWallet = walletService.depositMoney(
                authenticatedUser.getId(),
                amount
        );

        return ResponseEntity.status(201).body(updatedWallet);
    }

    @GetMapping("/me/transactions")
    public ResponseEntity<?> getMyWalletTransactions(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        Wallet wallet = walletRepository
                .findByUser_Id(authenticatedUser.getId())
                .orElse(null);

        if (wallet == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                ledgerTransactionRepository.findByWalletId(wallet.getId())
        );
    }
}