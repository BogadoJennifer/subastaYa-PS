package unaj.subastaya.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unaj.subastaya.model.LedgerTransaction;

import java.util.List;

@Repository
public interface LedgerTransactionRepository extends JpaRepository<LedgerTransaction, Long> {

    List<LedgerTransaction> findByWalletId(Long walletId);

    List<LedgerTransaction> findByAuctionId(Long auctionId);

}