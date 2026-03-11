package com.acv.assetmanagement.service;

import com.acv.assetmanagement.model.Device;
import com.acv.assetmanagement.model.DeviceStatus;
import com.acv.assetmanagement.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private com.acv.assetmanagement.repository.DeviceLogRepository deviceLogRepository;

    private String getCurrentUsername() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "System";
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public List<Device> getDevicesByStatus(DeviceStatus status) {
        return deviceRepository.findByStatus(status);
    }

    public Optional<Device> getDeviceById(Long id) {
        return deviceRepository.findById(id);
    }

    public Device saveDevice(Device device) {
        boolean isNew = (device.getId() == null);
        String action = isNew ? "CREATE" : "UPDATE";
        String details = isNew ? "Tạo mới thiết bị" : "Cập nhật thông tin thiết bị";
        
        Device saved = deviceRepository.save(device);
        
        deviceLogRepository.save(new com.acv.assetmanagement.model.DeviceLog(
            saved.getName(), saved.getAssetCode(), action, details, getCurrentUsername()
        ));
        
        return saved;
    }

    public void deleteDevice(Long id) {
        deviceRepository.findById(id).ifPresent(device -> {
            deviceLogRepository.save(new com.acv.assetmanagement.model.DeviceLog(
                device.getName(), device.getAssetCode(), "DELETE", "Xóa thiết bị", getCurrentUsername()
            ));
            deviceRepository.deleteById(id);
        });
    }

    public Device checkoutDevice(Long id, Integer amount, String assignedTo, String location) {
        Device source = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị"));

        if (amount > source.getQuantity()) {
            throw new RuntimeException("Số lượng yêu cầu vượt quá số lượng hiện có");
        }

        if (amount.equals(source.getQuantity())
                && (source.getStatus() == DeviceStatus.IN_STOCK || source.getStatus() == DeviceStatus.BACKUP)) {
            // Cập nhật trực tiếp nếu lấy hết
            source.setStatus(DeviceStatus.DEPLOYED);
            source.setAssignedTo(assignedTo);
            source.setLocation(location);
            return deviceRepository.save(source);
        } else {
            // Tách bản ghi
            source.setQuantity(source.getQuantity() - amount);
            deviceRepository.save(source);

            Device deployedDevice = new Device();
            deployedDevice.setName(source.getName());
            deployedDevice.setType(source.getType());
            deployedDevice.setSerialNumber(source.getSerialNumber());
            deployedDevice.setNotes(source.getNotes());
            deployedDevice.setPurchaseDate(source.getPurchaseDate());

            // Tạo mã tài sản mới để tránh trùng lặp
            deployedDevice.setAssetCode(source.getAssetCode() + "-" + System.currentTimeMillis() % 1000);
            deployedDevice.setQuantity(amount);
            deployedDevice.setStatus(DeviceStatus.DEPLOYED);
            deployedDevice.setAssignedTo(assignedTo);
            deployedDevice.setLocation(location);

            Device saved = deviceRepository.save(deployedDevice);
            
            deviceLogRepository.save(new com.acv.assetmanagement.model.DeviceLog(
                saved.getName(), saved.getAssetCode(), "CHECKOUT", 
                String.format("Cấp phát %d thiết bị cho %s tại %s", amount, assignedTo, location), 
                getCurrentUsername()
            ));
            
            return saved;
        }
    }

    public Device checkinDevice(Long id, Integer amount) {
        Device deployed = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thiết bị đang sử dụng"));

        if (amount > deployed.getQuantity()) {
            throw new RuntimeException("Số lượng trả lại vượt quá số lượng đang giữ");
        }

        // Tìm thiết bị gốc trong kho để cộng vào
        String baseCode = deployed.getAssetCode().split("-")[0];
        Optional<Device> stockDeviceOpt = deviceRepository.findByAssetCodeAndStatus(baseCode, DeviceStatus.IN_STOCK);

        if (stockDeviceOpt.isPresent()) {
            Device stockDevice = stockDeviceOpt.get();
            stockDevice.setQuantity(stockDevice.getQuantity() + amount);
            deviceRepository.save(stockDevice);
            
            deviceLogRepository.save(new com.acv.assetmanagement.model.DeviceLog(
                stockDevice.getName(), stockDevice.getAssetCode(), "CHECKIN", 
                String.format("Nhập lại %d thiết bị vào kho", amount), 
                getCurrentUsername()
            ));
        } else {
            // Nếu không tìm thấy, tạo mới record trong kho
            Device stockDevice = new Device();
            stockDevice.setName(deployed.getName());
            stockDevice.setAssetCode(baseCode);
            stockDevice.setType(deployed.getType());
            stockDevice.setQuantity(amount);
            stockDevice.setStatus(DeviceStatus.IN_STOCK);
            Device saved = deviceRepository.save(stockDevice);
            
            deviceLogRepository.save(new com.acv.assetmanagement.model.DeviceLog(
                saved.getName(), saved.getAssetCode(), "CHECKIN", 
                String.format("Nhập lại %d thiết bị vào kho (tạo mới record)", amount), 
                getCurrentUsername()
            ));
        }

        if (amount.equals(deployed.getQuantity())) {
            deviceRepository.delete(deployed);
            return null;
        } else {
            deployed.setQuantity(deployed.getQuantity() - amount);
            return deviceRepository.save(deployed);
        }
    }

    public java.util.List<com.acv.assetmanagement.model.DeviceLog> getLogsByAssetCode(String assetCode) {
        return deviceLogRepository.findByAssetCodeOrderByTimestampDesc(assetCode);
    }

    public java.util.List<Device> getDevicesByType(String type) {
        return deviceRepository.findByType(type);
    }
}
