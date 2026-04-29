package com.example.vkr.controller;

import com.example.vkr.entity.Material;
import com.example.vkr.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialRepository materialRepository;

    // Список материалов
    @GetMapping
    public String listMaterials(Model model) {
        List<Material> materials = materialRepository.findAll();
        model.addAttribute("materials", materials);
        return "materials/list";
    }

    // Форма создания материала
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("material", new Material());
        return "materials/create";
    }

    // Сохранение нового материала
    @PostMapping("/create")
    public String createMaterial(@ModelAttribute Material material) {
        materialRepository.save(material);
        return "redirect:/materials";
    }

    // Форма редактирования материала
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found"));
        model.addAttribute("material", material);
        return "materials/edit";
    }

    // Обновление материала
    @PostMapping("/edit/{id}")
    public String updateMaterial(@PathVariable Integer id, @ModelAttribute Material material) {
        material.setMaterialId(id);
        materialRepository.save(material);
        return "redirect:/materials";
    }

    // Удаление материала
    @GetMapping("/delete/{id}")
    public String deleteMaterial(@PathVariable Integer id) {
        materialRepository.deleteById(id);
        return "redirect:/materials";
    }
}