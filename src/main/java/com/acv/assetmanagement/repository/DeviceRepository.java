package com.acv.assetmanagement.repository;

import com.acv.assetmanagement.model.Device;
import com.acv.assetmanagement.model.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    List<Device> findByStatus(DeviceStatus status);

    Optional<Device> findByAssetCode(String assetCode);

    Optional<Device> findByAssetCodeAndStatus(String assetCode, DeviceStatus status);

    java.util.List<Device> findByType(String type);
}
