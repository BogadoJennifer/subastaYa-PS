package unaj.subastaya.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String entity; // EJ: SUBASTA, BILLETERA, SISTEMA

    @Column(nullable = false)
    private Long entityId; // ID del registro afectado

    @Column(nullable = false)
    private String action; // EJ: EXTENSION_TIEMPO, CIERRE_WORKER

    private Long userId; // Null si lo ejecutó el worker del sistema

    @Column(columnDefinition = "TEXT")
    private String detalleJson; // Payload con los cambios

    @Column(nullable = false)
    private LocalDateTime date;

    public AuditLog() {}

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getDetalleJson() { return detalleJson; }
    public void setDetalleJson(String detalleJson) { this.detalleJson = detalleJson; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
}

