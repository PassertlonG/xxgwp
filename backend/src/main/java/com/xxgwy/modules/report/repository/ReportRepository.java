package com.xxgwy.modules.report.repository;

import com.xxgwy.modules.report.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
}
