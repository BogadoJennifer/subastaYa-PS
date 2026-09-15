package unaj.subastaya.repository;

import unaj.subastaya.model.User;
import unaj.subastaya.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Wallet findByUser(User user);
    Optional<Wallet> findById(Long id);
}