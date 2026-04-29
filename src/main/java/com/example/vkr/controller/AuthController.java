package com.example.vkr.controller;

import com.example.vkr.dto.RequestResponseDto;
import com.example.vkr.entity.Request;
import com.example.vkr.entity.Role;
import com.example.vkr.entity.User;
import com.example.vkr.repository.RequestRepository;
import com.example.vkr.repository.RoleRepository;
import com.example.vkr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RequestRepository requestRepository;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }



    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String email,
                               Model model) {
        // Проверка, существует ли пользователь
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Пользователь с таким именем уже существует");
            return "register";
        }

        // Создание нового пользователя
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);

        // Назначение роли по умолчанию (REQUESTER)
        Role defaultRole = roleRepository.findByName("REQUESTER")
                .orElseThrow(() -> new RuntimeException("Роль REQUESTER не найдена"));
        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        user.setRoles(roles);

        userRepository.save(user);

        return "redirect:/login?registered";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Получаем заявки в зависимости от роли
        List<Request> userRequests;

        boolean isWorker = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("WORKER"));
        boolean isDispatcher = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("DISPATCHER") || r.getName().equals("ADMIN"));

        if (isDispatcher) {
            userRequests = requestRepository.findAll();
        } else if (isWorker) {
            userRequests = requestRepository.findByAssignedTo(currentUser);
        } else {
            userRequests = requestRepository.findByCreatedBy(currentUser);
        }

        // Преобразуем в DTO
        List<RequestResponseDto> requestDtos = userRequests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        model.addAttribute("requests", requestDtos);

        return "dashboard";
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
        // Статус
        if (request.getStatus() != null) {
            dto.setStatusName(request.getStatus().getName());
            dto.setStatusColor(request.getStatus().getColor());
        }

        // Оборудование
        if (request.getEquipment() != null) {
            dto.setEquipmentName(request.getEquipment().getName());
            dto.setEquipmentId(request.getEquipment().getEquipmentId());
        }

        // Создатель
        if (request.getCreatedBy() != null) {
            dto.setCreatedByUsername(request.getCreatedBy().getUsername());
        }

        // Исполнитель
        if (request.getAssignedTo() != null) {
            dto.setAssignedToUsername(request.getAssignedTo().getUsername());
        }

        // Определяем CSS-класс для строки таблицы
        LocalDateTime now = LocalDateTime.now();
        if (request.getDeadline() != null) {
            if (request.getDeadline().isBefore(now)) {
                dto.setRowCssClass("overdue-row");      // просрочена
            } else if (request.getDeadline().isBefore(now.plusHours(24))) {
                dto.setRowCssClass("warning-row");      // истекает через 24 часа
            }
        }
        System.out.println("Request " + request.getRequestId() + " deadline: " + request.getDeadline());
        System.out.println("  rowCssClass: " + dto.getRowCssClass());

        return dto;
    }
}