package com.internship.kyc_system.repository;

import com.internship.kyc_system.entity.KycRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KycRepository extends JpaRepository<KycRecord, Long> {
}