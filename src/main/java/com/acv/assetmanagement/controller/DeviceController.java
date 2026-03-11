package com.acv.assetmanagement.controller;

import com.acv.assetmanagement.model.Device;
import com.acv.assetmanagement.model.DeviceStatus;
import com.acv.assetmanagement.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/devices")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    @GetMapping
    public String listAllDevices(Model model) {
        model.addAttribute("devices", deviceService.getAllDevices());
        model.addAttribute("title", "Danh sách thiết bị");
        model.addAttribute("currentUri", "/devices");
        return "devices/list";
    }

    @GetMapping("/deployed")
    public String listDeployedDevices(Model model) {
        model.addAttribute("devices", deviceService.getDevicesByStatus(DeviceStatus.DEPLOYED));
        model.addAttribute("title", "Thiết bị triển khai");
        model.addAttribute("currentUri", "/devices/deployed");
        return "devices/list";
    }

    @GetMapping("/backup")
    public String listBackupDevices(Model model) {
        model.addAttribute("devices", deviceService.getDevicesByStatus(DeviceStatus.BACKUP));
        model.addAttribute("title", "Thiết bị dự phòng");
        model.addAttribute("currentUri", "/devices/backup");
        return "devices/list";
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
        model.addAttribute("device", new Device());
        model.addAttribute("statuses", DeviceStatus.values());
        model.addAttribute("title", "Thêm thiết bị mới");
        return "devices/form";
    }

    @PostMapping("/save")
    public String saveDevice(@ModelAttribute("device") Device device) {
        deviceService.saveDevice(device);
        return "redirect:/devices?success=device_saved";
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
}
