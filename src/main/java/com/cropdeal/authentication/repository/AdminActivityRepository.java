package com.cropdeal.authentication.repository;

import com.cropdeal.authentication.model.AdminActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdminActivityRepository extends JpaRepository<AdminActivityLog, Long>,
        JpaSpecificationExecutor<AdminActivityLog> {
}