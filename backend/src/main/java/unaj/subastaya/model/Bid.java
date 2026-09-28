package unaj.subastaya.model;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime bidDate;

    public Bid() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Auction getAuction() { return auction; }
    public void setAuction(Auction auction) { this.auction = auction; }
    public User getBidder() { return bidder; }
    public void setBidder(User bidder) { this.bidder = bidder; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDateTime getBidDate() { return bidDate; }
    public void setBidDate(LocalDateTime bidDate) { this.bidDate = bidDate; }
}
