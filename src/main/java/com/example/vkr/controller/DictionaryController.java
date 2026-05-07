package com.example.vkr.controller;

import com.example.vkr.entity.*;
import com.example.vkr.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/dictionaries")
@RequiredArgsConstructor
public class DictionaryController {

    private final RequestStatusRepository requestStatusRepository;
    private final StatusTransitionRepository statusTransitionRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final RequestTypeRepository requestTypeRepository;
    private final RoleRepository roleRepository;

    // ==================== ГЛАВНАЯ СТРАНИЦА ====================
    @GetMapping
    public String index(Model model) {
        model.addAttribute("statuses", requestStatusRepository.findByIsActiveTrueOrderBySortOrderAsc());
        model.addAttribute("transitions", statusTransitionRepository.findAll());
        model.addAttribute("equipmentTypes", equipmentTypeRepository.findByIsActiveTrue());
        model.addAttribute("requestTypes", requestTypeRepository.findByIsActiveTrue());
        model.addAttribute("allStatuses", requestStatusRepository.findByIsActiveTrue());
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/dictionaries";
    }

    // ==================== УПРАВЛЕНИЕ СТАТУСАМИ ====================
    @GetMapping("/statuses/create")
    public String showCreateStatusForm(Model model) {
        model.addAttribute("status", new RequestStatus());
        return "admin/statuses/create";
    }

    @PostMapping("/statuses/create")
    public String createStatus(@ModelAttribute RequestStatus status) {
        status.setIsActive(true);
        requestStatusRepository.save(status);
        return "redirect:/admin/dictionaries";
    }

    @GetMapping("/statuses/edit/{id}")
    public String showEditStatusForm(@PathVariable Integer id, Model model) {
        RequestStatus status = requestStatusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Status not found"));
        model.addAttribute("status", status);
        return "admin/statuses/edit";
    }

    @PostMapping("/statuses/edit/{id}")
    public String updateStatus(@PathVariable Integer id, @ModelAttribute RequestStatus status) {
        status.setStatusId(id);
        requestStatusRepository.save(status);
        return "redirect:/admin/dictionaries";
    }

    @GetMapping("/statuses/delete/{id}")
    public String deleteStatus(@PathVariable Integer id) {
        RequestStatus status = requestStatusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Status not found"));
        status.setIsActive(false);
        requestStatusRepository.save(status);
        return "redirect:/admin/dictionaries";
    }

    // ==================== УПРАВЛЕНИЕ ПЕРЕХОДАМИ ====================
    @GetMapping("/transitions/create")
    public String showCreateTransitionForm(Model model) {
        model.addAttribute("transition", new StatusTransition());
        model.addAttribute("statuses", requestStatusRepository.findByIsActiveTrue());
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/transitions/create";
    }

    @PostMapping("/transitions/create")
    public String createTransition(@RequestParam Integer fromStatusId,
                                   @RequestParam Integer toStatusId,
                                   @RequestParam String roleId) {
        StatusTransition transition = new StatusTransition();
        transition.setFromStatus(requestStatusRepository.findById(fromStatusId).orElseThrow());
        transition.setToStatus(requestStatusRepository.findById(toStatusId).orElseThrow());
        transition.setRoleId(roleId);
        statusTransitionRepository.save(transition);
        return "redirect:/admin/dictionaries";
    }

    @GetMapping("/transitions/delete/{id}")
    public String deleteTransition(@PathVariable Long id) {
        statusTransitionRepository.deleteById(id);
        return "redirect:/admin/dictionaries";
    }

    // ==================== УПРАВЛЕНИЕ ТИПАМИ ОБОРУДОВАНИЯ ====================
    @GetMapping("/equipment-types/create")
    public String showCreateEquipmentTypeForm(Model model) {
        model.addAttribute("equipmentType", new EquipmentType());
        return "admin/equipment-types/create";
    }

    @PostMapping("/equipment-types/create")
    public String createEquipmentType(@ModelAttribute EquipmentType type) {
        type.setIsActive(true);
        equipmentTypeRepository.save(type);
        return "redirect:/admin/dictionaries";
    }

    @GetMapping("/equipment-types/edit/{id}")
    public String showEditEquipmentTypeForm(@PathVariable Integer id, Model model) {
        EquipmentType type = equipmentTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment type not found"));
        model.addAttribute("equipmentType", type);
        return "admin/equipment-types/edit";
    }

    @PostMapping("/equipment-types/edit/{id}")
    public String updateEquipmentType(@PathVariable Integer id, @ModelAttribute EquipmentType type) {
        type.setTypeId(id);
        equipmentTypeRepository.save(type);
        return "redirect:/admin/dictionaries";
    }

    @GetMapping("/equipment-types/delete/{id}")
    public String deleteEquipmentType(@PathVariable Integer id) {
        EquipmentType type = equipmentTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment type not found"));
        type.setIsActive(false);
        equipmentTypeRepository.save(type);
        return "redirect:/admin/dictionaries";
    }

    // ==================== УПРАВЛЕНИЕ ТИПАМИ ЗАЯВОК ====================
    @GetMapping("/request-types/create")
    public String showCreateRequestTypeForm(Model model) {
        model.addAttribute("requestType", new RequestType());
        return "admin/request-types/create";
    }

    @PostMapping("/request-types/create")
    public String createRequestType(@ModelAttribute RequestType type) {
        type.setIsActive(true);
        requestTypeRepository.save(type);
        return "redirect:/admin/dictionaries";
    }

    @GetMapping("/request-types/edit/{id}")
    public String showEditRequestTypeForm(@PathVariable Integer id, Model model) {
        RequestType type = requestTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request type not found"));
        model.addAttribute("requestType", type);
        return "admin/request-types/edit";
    }

    @PostMapping("/request-types/edit/{id}")
    public String updateRequestType(@PathVariable Integer id, @ModelAttribute RequestType type) {
        type.setTypeId(id);
        requestTypeRepository.save(type);
        return "redirect:/admin/dictionaries";
    }

    @GetMapping("/request-types/delete/{id}")
    public String deleteRequestType(@PathVariable Integer id) {
        RequestType type = requestTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request type not found"));
        type.setIsActive(false);
        requestTypeRepository.save(type);
        return "redirect:/admin/dictionaries";
    }
}