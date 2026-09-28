package unaj.subastaya.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import unaj.subastaya.model.Auction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    List<Auction> findByStateAndEndDateBefore(
            String state,
            LocalDateTime dateTime
    );
    List<Auction> findByStateAndStartDateBefore(
            String state,
            LocalDateTime dateTime
    );

    List<Auction> findByVendorId(Long vendorId);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select a from Auction a where a.id = :id")
    Optional<Auction> findForBidding(@Param("id") Long id);
}