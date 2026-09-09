package unaj.subastaya.repository;

import unaj.subastaya.model.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    List<Auction> findByStateAndEndDateBefore(String state, LocalDateTime dateTime);
}
