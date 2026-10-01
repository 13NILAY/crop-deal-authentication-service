package com.cropdeal.authentication.service;

import com.cropdeal.authentication.dto.AdminActivityRequest;
import com.cropdeal.authentication.model.AdminActivityLog;
import com.cropdeal.authentication.repository.AdminActivityRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class AdminActivityService {
    private final AdminActivityRepository repository;

    public AdminActivityService(AdminActivityRepository repository) {
        this.repository = repository;
    }

    public AdminActivityLog record(String adminId, AdminActivityRequest request) {
        AdminActivityLog entry = new AdminActivityLog();
        entry.setAdminId(adminId);
        entry.setAction(request.action().trim().toUpperCase(java.util.Locale.ROOT));
        entry.setEntityType(request.entityType().trim().toUpperCase(java.util.Locale.ROOT));
        entry.setEntityId(request.entityId().trim());
        entry.setTimestamp(LocalDateTime.now());
        return repository.save(entry);
    }

    public Page<AdminActivityLog> search(String adminId, String action, String entityType,
                                         LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Specification<AdminActivityLog> specification = (root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (adminId != null && !adminId.isBlank()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("adminId")),
                        adminId.trim().toLowerCase(java.util.Locale.ROOT)));
            }
            if (action != null && !action.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("action"),
                        action.trim().toUpperCase(java.util.Locale.ROOT)));
            }
            if (entityType != null && !entityType.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("entityType"),
                        entityType.trim().toUpperCase(java.util.Locale.ROOT)));
            }
            if (from != null) predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), from));
            if (to != null) predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), to));
            return criteriaBuilder.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        Pageable bounded = PageRequest.of(Math.max(0, pageable.getPageNumber()),
                Math.max(1, Math.min(pageable.getPageSize(), 100)), pageable.getSort());
        return repository.findAll(specification, bounded);
    }
}