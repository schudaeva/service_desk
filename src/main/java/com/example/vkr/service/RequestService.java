package com.example.vkr.service;

import com.example.vkr.dto.RequestCreateDto;
import com.example.vkr.entity.*;
import com.example.vkr.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final RequestStatusRepository statusRepository;

    @Transactional
    public Request createRequest(RequestCreateDto dto, User creator) {
        Request request = new Request();
        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());
        request.setPriority(dto.getPriority());
        request.setDeadline(dto.getDeadline());
        request.setCreatedBy(creator);
        request.setSource("WEB");
        request.setUpdatedAt(LocalDateTime.now());

        if (dto.getEquipmentId() != null) {
            Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                    .orElseThrow(() -> new RuntimeException("Equipment not found"));
            request.setEquipment(equipment);
        }

        if (dto.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(dto.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            request.setAssignedTo(assignedTo);
        }

        RequestStatus newStatus = statusRepository.findByCode("NEW")
                .orElseThrow(() -> new RuntimeException("Status NEW not found"));
        request.setStatus(newStatus);

        return requestRepository.save(request);
    }

    @Transactional
    public Request changeStatus(Integer requestId, String statusCode, User user) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        RequestStatus newStatus = statusRepository.findByCode(statusCode)
                .orElseThrow(() -> new RuntimeException("Status not found"));

        request.setStatus(newStatus);
        request.setUpdatedAt(LocalDateTime.now());

        return requestRepository.save(request);
    }
}