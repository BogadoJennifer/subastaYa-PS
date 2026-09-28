package unaj.subastaya.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import unaj.subastaya.model.AuditLog;
import unaj.subastaya.repository.AuditLogRepository;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRejectedBidAudit(
            Long auctionId,
            Long bidderId,
            String detail) {

        AuditLog auditLog = new AuditLog();

        auditLog.setEntity("AUCTION");
        auditLog.setEntityId(auctionId);
        auditLog.setAction("REJECTED_BID");
        auditLog.setUserId(bidderId);
        auditLog.setDate(LocalDateTime.now());
        auditLog.setDetailJson(detail);

        auditLogRepository.save(auditLog);
    }
}