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
        String action = isNew ? "Tạo mới" : "Cập nhật";
        String details;

        if (isNew) {
            details = "Tạo mới thiết bị";
        } else {
            Device oldDevice = deviceRepository.findById(device.getId()).orElse(null);
            details = generateUpdateDetails(oldDevice, device);
        }

        Device saved = deviceRepository.save(device);

        deviceLogRepository.save(new com.acv.assetmanagement.model.DeviceLog(
                saved.getName(), saved.getAssetCode(), action, details, getCurrentUsername()));

        return saved;
    }

    private String generateUpdateDetails(Device oldDevice, Device newDevice) {
        if (oldDevice == null) return "Cập nhật thông tin thiết bị";
        
        StringBuilder sb = new StringBuilder("Thay đổi: ");
        boolean changed = false;

        if (!java.util.Objects.equals(oldDevice.getName(), newDevice.getName())) {
            sb.append(String.format("[Tên: %s -> %s] ", oldDevice.getName(), newDevice.getName()));
            changed = true;
        }
        if (!java.util.Objects.equals(oldDevice.getType(), newDevice.getType())) {
            sb.append(String.format("[Loại: %s -> %s] ", oldDevice.getType(), newDevice.getType()));
            changed = true;
        }
        if (!java.util.Objects.equals(oldDevice.getSerialNumber(), newDevice.getSerialNumber())) {
            sb.append(String.format("[Serial: %s -> %s] ", oldDevice.getSerialNumber(), newDevice.getSerialNumber()));
            changed = true;
        }
        if (!java.util.Objects.equals(oldDevice.getStatus(), newDevice.getStatus())) {
            sb.append(String.format("[Trạng thái: %s -> %s] ", oldDevice.getStatus(), newDevice.getStatus()));
            changed = true;
        }
        if (!java.util.Objects.equals(oldDevice.getLocation(), newDevice.getLocation())) {
            sb.append(String.format("[Vị trí: %s -> %s] ", oldDevice.getLocation(), newDevice.getLocation()));
            changed = true;
        }
        if (!java.util.Objects.equals(oldDevice.getAssignedTo(), newDevice.getAssignedTo())) {
            sb.append(String.format("[Người sử dụng: %s -> %s] ", oldDevice.getAssignedTo(), newDevice.getAssignedTo()));
            changed = true;
        }
        if (!java.util.Objects.equals(oldDevice.getTerminal(), newDevice.getTerminal())) {
            sb.append(String.format("[Nhà ga: %s -> %s] ", oldDevice.getTerminal(), newDevice.getTerminal()));
            changed = true;
        }
        if (!java.util.Objects.equals(oldDevice.getDeviceSystem(), newDevice.getDeviceSystem())) {
            sb.append(String.format("[Hệ thống: %s -> %s] ", oldDevice.getDeviceSystem(), newDevice.getDeviceSystem()));
            changed = true;
        }
        if (!java.util.Objects.equals(oldDevice.getQuantity(), newDevice.getQuantity())) {
            sb.append(String.format("[Số lượng: %s -> %s] ", oldDevice.getQuantity(), newDevice.getQuantity()));
            changed = true;
        }
        if (!java.util.Objects.equals(oldDevice.getNotes(), newDevice.getNotes())) {
            sb.append("[Ghi chú đã thay đổi] ");
            changed = true;
        }

        return changed ? sb.toString().trim() : "Cập nhật thông tin thiết bị (không có thay đổi lớn)";
    }

    public void deleteDevice(Long id) {
        deviceRepository.findById(id).ifPresent(device -> {
            deviceLogRepository.save(new com.acv.assetmanagement.model.DeviceLog(
                    device.getName(), device.getAssetCode(), "Xóa", "Xóa thiết bị khỏi hệ thống", getCurrentUsername()));
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
            Device saved = deviceRepository.save(source);

            deviceLogRepository.save(new com.acv.assetmanagement.model.DeviceLog(
                    saved.getName(), saved.getAssetCode(), "Cấp phát",
                    String.format("Cấp phát thiết bị cho %s tại %s", assignedTo, location),
                    getCurrentUsername()));

            return saved;
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
                    saved.getName(), saved.getAssetCode(), "Cấp phát",
                    String.format("Cấp phát %d thiết bị cho %s tại %s", amount, assignedTo, location),
                    getCurrentUsername()));

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
                    stockDevice.getName(), stockDevice.getAssetCode(), "Nhập kho",
                    String.format("Thu hồi %d thiết bị về kho", amount),
                    getCurrentUsername()));
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
                    saved.getName(), saved.getAssetCode(), "Nhập kho",
                    String.format("Thu hồi %d thiết bị về kho (tạo mới bản ghi kho)", amount),
                    getCurrentUsername()));
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
