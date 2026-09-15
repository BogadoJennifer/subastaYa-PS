package unaj.subastaya.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import unaj.subastaya.model.Bid;


import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByAuctionId(Long auctionId);
    List<Bid> findByBidderId(Long bidderId);
    // Consulta útil para obtener directamente la puja más alta de una subasta
    Optional<Bid> findHighestBid(Long auctionId);

}