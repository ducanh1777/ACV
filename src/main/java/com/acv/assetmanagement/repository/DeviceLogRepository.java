package com.acv.assetmanagement.repository;

import com.acv.assetmanagement.model.DeviceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceLogRepository extends JpaRepository<DeviceLog, Long> {
    List<DeviceLog> findByAssetCodeOrderByTimestampDesc(String assetCode);
}
