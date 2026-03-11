package com.acv.assetmanagement.controller;

import com.acv.assetmanagement.model.Device;
import com.acv.assetmanagement.model.DeviceStatus;
import com.acv.assetmanagement.service.DeviceService;
import com.acv.assetmanagement.dto.DeviceTypeStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping
    public String listAllDevices(@RequestParam(required = false) String category, Model model) {
        Iterable<Device> devices = new ArrayList<>();
        if ("all".equals(category)) {
            devices = deviceService.getAllDevices();
        } else if ("deployed".equals(category)) {
            devices = deviceService.getDevicesByStatus(DeviceStatus.DEPLOYED);
        } else if (category != null && !category.isEmpty()) {
            devices = deviceService.getDevicesByType(category);
        }

        long totalDevices = 0;
        long deployedDevices = 0;
        long backupRequired = 35;

        Map<String, DeviceTypeStats> typeStatsMap = new LinkedHashMap<>();

        // We always fetch all devices to calculate the summary cards
        Iterable<Device> allDevicesForStats = deviceService.getAllDevices();
        for (Device d : allDevicesForStats) {
            int qty = (d.getQuantity() != null ? d.getQuantity() : 1);
            totalDevices += qty;

            if (d.getStatus() == DeviceStatus.DEPLOYED) {
                deployedDevices += qty;
            }

            String type = d.getType();
            if (type == null || type.isEmpty())
                type = "Khác";

            DeviceTypeStats stats = typeStatsMap.computeIfAbsent(type, DeviceTypeStats::new);
            stats.addTotal(qty);
            if (d.getStatus() == DeviceStatus.DEPLOYED) {
                stats.addDeployed(qty);
            } else if (d.getStatus() == DeviceStatus.IN_STOCK) {
                stats.addInStock(qty);
            } else if (d.getStatus() == DeviceStatus.BACKUP) {
                stats.addBackup(qty);
            }
        }

        model.addAttribute("devices", devices);
        model.addAttribute("totalDevices", totalDevices);
        model.addAttribute("deployedDevices", deployedDevices);
        model.addAttribute("maintenanceCount", backupRequired);
        model.addAttribute("typeStats", typeStatsMap.values());
        model.addAttribute("selectedCategory", category);

        String titleString = "Danh sách thiết bị";
        if ("all".equals(category))
            titleString = "Tất cả tài sản";
        else if (category != null)
            titleString = "Danh sách " + category;

        model.addAttribute("title", titleString);
        model.addAttribute("currentUri", "/devices");
        return "devices/list";
    }

    @GetMapping("/deployed")
    public String listDeployedDevices(Model model) {
        return listAllDevices("Đang hoạt động", model);
    }

    @GetMapping("/backup")
    public String listBackupDevices(Model model) {
        return listAllDevices("Dự phòng", model);
    }

    @GetMapping("/in-stock")
    public String listInStockDevices(Model model) {
        model.addAttribute("devices", deviceService.getDevicesByStatus(DeviceStatus.IN_STOCK));
        model.addAttribute("title", "Thiết bị tồn");
        model.addAttribute("currentUri", "/devices/in-stock");
        return "devices/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        Device device = new Device();
        device.setQuantity(1); // Mặc định là 1 và không cho sửa ở UI
        model.addAttribute("device", device);
        model.addAttribute("statuses", DeviceStatus.values());
        model.addAttribute("title", "Thêm thiết bị mới");
        return "devices/form";
    }

    @PostMapping("/save")
    public String saveDevice(@ModelAttribute("device") Device device, RedirectAttributes redirectAttributes) {
        try {
            device.setQuantity(1); // Đảm bảo số lượng luôn là 1
            deviceService.saveDevice(device);
            return "redirect:/devices?success=device_saved";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            // If it's a new device (no ID), redirect back to add form, otherwise to edit
            // form
            if (device.getId() == null) {
                return "redirect:/devices/add";
            } else {
                return "redirect:/devices/edit/" + device.getId();
            }
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Device device = deviceService.getDeviceById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid device Id:" + id));
        model.addAttribute("device", device);
        model.addAttribute("statuses", DeviceStatus.values());
        model.addAttribute("title", "Chỉnh sửa thiết bị");
        return "devices/form";
    }

    @GetMapping("/delete/{id}")
    public String deleteDevice(@PathVariable("id") Long id) {
        deviceService.deleteDevice(id);
        return "redirect:/devices?success=device_deleted";
    }

    @PostMapping("/checkout")
    public String checkoutDevice(@RequestParam("id") Long id,
            @RequestParam("amount") Integer amount,
            @RequestParam("assignedTo") String assignedTo,
            @RequestParam("location") String location) {
        try {
            deviceService.checkoutDevice(id, amount, assignedTo, location);
            return "redirect:/devices?success=checked_out";
        } catch (Exception e) {
            return "redirect:/devices?error=" + e.getMessage();
        }
    }

    @PostMapping("/checkin")
    public String checkinDevice(@RequestParam("id") Long id,
            @RequestParam("amount") Integer amount) {
        try {
            deviceService.checkinDevice(id, amount);
            return "redirect:/devices?success=checked_in";
        } catch (Exception e) {
            return "redirect:/devices?error=" + e.getMessage();
        }
    }

    @GetMapping("/logs/{assetCode}")
    @ResponseBody
    public List<com.acv.assetmanagement.model.DeviceLog> getDeviceLogs(@PathVariable String assetCode) {
        return deviceService.getLogsByAssetCode(assetCode);
    }
}
