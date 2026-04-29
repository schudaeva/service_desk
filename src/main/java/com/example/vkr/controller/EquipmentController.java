package com.example.vkr.controller;

import com.example.vkr.dto.RequestResponseDto;
import com.example.vkr.entity.Equipment;
import com.example.vkr.entity.EquipmentType;
import com.example.vkr.entity.Request;
import com.example.vkr.repository.EquipmentRepository;
import com.example.vkr.repository.EquipmentTypeRepository;
import com.example.vkr.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final RequestRepository requestRepository;



    @GetMapping
    public String listEquipment(Model model) {
        List<Equipment> equipmentList = equipmentRepository.findAll();
        model.addAttribute("equipmentList", equipmentList);
        return "equipment/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("equipment", new Equipment());
        model.addAttribute("types", equipmentTypeRepository.findAll());
        return "equipment/create";
    }

    @PostMapping("/create")
    public String createEquipment(@ModelAttribute Equipment equipment) {
        equipmentRepository.save(equipment);
        return "redirect:/equipment";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));
        model.addAttribute("equipment", equipment);
        model.addAttribute("types", equipmentTypeRepository.findAll());
        return "equipment/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateEquipment(@PathVariable Integer id, @ModelAttribute Equipment equipment) {
        equipment.setEquipmentId(id);
        equipmentRepository.save(equipment);
        return "redirect:/equipment";
    }

    @GetMapping("/delete/{id}")
    public String deleteEquipment(@PathVariable Integer id) {
        equipmentRepository.deleteById(id);
        return "redirect:/equipment";
    }

    @GetMapping("/{id}")
    public String viewEquipment(@PathVariable Integer id, Model model) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        // Получаем все заявки, связанные с этим оборудованием
        List<Request> relatedRequests = requestRepository.findByEquipment_EquipmentId(id);

        // Преобразуем в DTO для отображения
        List<RequestResponseDto> requestDtos = relatedRequests.stream()
                .map(r -> convertToDto(r))
                .collect(Collectors.toList());

        model.addAttribute("equipment", equipment);
        model.addAttribute("relatedRequests", requestDtos);

        return "equipment/view";
    }

    private RequestResponseDto convertToDto(Request request) {
        RequestResponseDto dto = new RequestResponseDto();
        dto.setRequestId(request.getRequestId());
        dto.setTitle(request.getTitle());
        dto.setDescription(request.getDescription());
        dto.setPriority(request.getPriority());
        dto.setDeadline(request.getDeadline());
        dto.setSource(request.getSource());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());

        if (request.getStatus() != null) {
            dto.setStatusName(request.getStatus().getName());
            dto.setStatusColor(request.getStatus().getColor());
        }
        if (request.getCreatedBy() != null) {
            dto.setCreatedByUsername(request.getCreatedBy().getUsername());
        }
        if (request.getAssignedTo() != null) {
            dto.setAssignedToUsername(request.getAssignedTo().getUsername());
        }

        LocalDateTime now = LocalDateTime.now();
        if (request.getDeadline() != null) {
            if (request.getDeadline().isBefore(now)) {
                dto.setRowCssClass("background-color: #ffe6e6;");
            } else if (request.getDeadline().isBefore(now.plusHours(24))) {
                dto.setRowCssClass("background-color: #fff3cd;");
            }
        }

        return dto;
    }
}