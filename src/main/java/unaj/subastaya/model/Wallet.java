package unaj.subastaya.model;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "users_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private BigDecimal totalBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal retainedBalance = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Version
    private Integer version;

    public Wallet() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public BigDecimal getTotalBalance() { return totalBalance; }
    public void setTotalBalance(BigDecimal totalBalance) { this.totalBalance = totalBalance; }
    public BigDecimal getRetainedBalance() { return retainedBalance; }
    public void setRetainedBalance(BigDecimal retainedBalance) { this.retainedBalance = retainedBalance; }
    public BigDecimal getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public void setReservedBalance(BigDecimal reservedBalance) {
        this.retainedBalance = reservedBalance;
        this.availableBalance = this.totalBalance.subtract(reservedBalance);
    }
}
