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
        return deviceRepository.save(device);
    }

    public void deleteDevice(Long id) {
        deviceRepository.deleteById(id);
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

            return deviceRepository.save(deployedDevice);
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
        } else {
            // Nếu không tìm thấy, tạo mới record trong kho
            Device stockDevice = new Device();
            stockDevice.setName(deployed.getName());
            stockDevice.setAssetCode(baseCode);
            stockDevice.setType(deployed.getType());
            stockDevice.setQuantity(amount);
            stockDevice.setStatus(DeviceStatus.IN_STOCK);
            deviceRepository.save(stockDevice);
        }

        if (amount.equals(deployed.getQuantity())) {
            deviceRepository.delete(deployed);
            return null;
        } else {
            deployed.setQuantity(deployed.getQuantity() - amount);
            return deviceRepository.save(deployed);
        }
    }

    public java.util.List<Device> getDevicesByType(String type) {
        return deviceRepository.findByType(type);
    }
}
