package com.example.naebuilding.repository;

import com.example.naebuilding.domain.AdminLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdminLogRepository extends JpaRepository<AdminLogEntity, Long>, JpaSpecificationExecutor<AdminLogEntity> {
}
