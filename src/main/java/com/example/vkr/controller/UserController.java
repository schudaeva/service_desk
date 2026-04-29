package com.example.vkr.controller;

import com.example.vkr.dto.*;
import com.example.vkr.entity.Role;
import com.example.vkr.entity.User;
import com.example.vkr.repository.RoleRepository;
import com.example.vkr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // ========== СПИСОК ПОЛЬЗОВАТЕЛЕЙ (только ADMIN) ==========
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));
        return "users/list";
    }

    // ========== ФОРМА СОЗДАНИЯ ПОЛЬЗОВАТЕЛЯ ==========
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("userCreateDto", new UserCreateDto());
        model.addAttribute("allRoles", roleRepository.findAll());
        return "users/create";
    }

    // ========== СОЗДАНИЕ ПОЛЬЗОВАТЕЛЯ ==========
    @PostMapping("/create")
    public String createUser(@ModelAttribute UserCreateDto createDto) {
        User user = new User();
        user.setUsername(createDto.getUsername());
        user.setEmail(createDto.getEmail());
        user.setFullName(createDto.getFullName());
        user.setPhone(createDto.getPhone());
        user.setPosition(createDto.getPosition());
        user.setDepartment(createDto.getDepartment());
        // Дефолтный пароль = username + "123"
        user.setPassword(passwordEncoder.encode(createDto.getUsername() + "123"));

        Set<Role> roles = new HashSet<>();
        if (createDto.getRoles() != null) {
            for (String roleName : createDto.getRoles()) {
                roleRepository.findByName(roleName).ifPresent(roles::add);
            }
        }
        user.setRoles(roles);

        userRepository.save(user);
        return "redirect:/users";
    }

    // ========== ФОРМА РЕДАКТИРОВАНИЯ ПОЛЬЗОВАТЕЛЯ ==========
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserCreateDto editDto = new UserCreateDto();
        editDto.setUsername(user.getUsername());
        editDto.setEmail(user.getEmail());
        editDto.setFullName(user.getFullName());
        editDto.setPhone(user.getPhone());
        editDto.setPosition(user.getPosition());
        editDto.setDepartment(user.getDepartment());
        editDto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));

        model.addAttribute("userEditDto", editDto);
        model.addAttribute("userId", id);
        model.addAttribute("allRoles", roleRepository.findAll());
        return "users/edit";
    }

    // ========== ОБНОВЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ==========
    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Integer id, @ModelAttribute UserCreateDto editDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(editDto.getEmail());
        user.setFullName(editDto.getFullName());
        user.setPhone(editDto.getPhone());
        user.setPosition(editDto.getPosition());
        user.setDepartment(editDto.getDepartment());

        Set<Role> roles = new HashSet<>();
        if (editDto.getRoles() != null) {
            for (String roleName : editDto.getRoles()) {
                roleRepository.findByName(roleName).ifPresent(roles::add);
            }
        }
        user.setRoles(roles);

        userRepository.save(user);
        return "redirect:/users";
    }

    // ========== УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ==========
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Integer id) {
        // Не удалять самого себя
        userRepository.deleteById(id);
        return "redirect:/users";
    }

    // ========== ПРОФИЛЬ ТЕКУЩЕГО ПОЛЬЗОВАТЕЛЯ ==========
    @GetMapping("/profile")
    public String showProfile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        model.addAttribute("user", user);
        return "users/profile";
    }

    // ========== ОБНОВЛЕНИЕ ПРОФИЛЯ (email, ФИО, телефон, должность, отдел) ==========
    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute ProfileUpdateDto profileDto,
                                @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(profileDto.getEmail());
        user.setFullName(profileDto.getFullName());
        user.setPhone(profileDto.getPhone());
        user.setPosition(profileDto.getPosition());
        user.setDepartment(profileDto.getDepartment());

        userRepository.save(user);
        return "redirect:/users/profile?success=profile";
    }

    // ========== СМЕНА ПАРОЛЯ ==========
    @PostMapping("/change-password")
    public String changePassword(@ModelAttribute PasswordChangeDto passwordDto,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверка текущего пароля
        if (!passwordEncoder.matches(passwordDto.getCurrentPassword(), user.getPassword())) {
            return "redirect:/users/profile?error=password";
        }

        // Проверка совпадения нового пароля
        if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
            return "redirect:/users/profile?error=passwordMatch";
        }

        // Установка нового пароля
        user.setPassword(passwordEncoder.encode(passwordDto.getNewPassword()));
        userRepository.save(user);

        return "redirect:/users/profile?success=password";
    }

    // ========== КОНВЕРТЕР В DTO ==========
    private UserResponseDto convertToDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setPosition(user.getPosition());
        dto.setDepartment(user.getDepartment());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
        return dto;
    }
}