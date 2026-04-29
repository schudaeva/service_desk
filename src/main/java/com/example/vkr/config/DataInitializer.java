package com.example.vkr.config;

import com.example.vkr.entity.*;
import com.example.vkr.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RequestRepository requestRepository;
    private final RequestStatusRepository requestStatusRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final StatusTransitionRepository statusTransitionRepository;


    @Override
    public void run(String... args) throws Exception {
        // Создание ролей, если их нет
        createRoleIfNotFound("ADMIN");
        createRoleIfNotFound("DISPATCHER");
        createRoleIfNotFound("WORKER");
        createRoleIfNotFound("REQUESTER");

        // Создание тестового администратора, если его нет
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setEmail("admin@example.com");

            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(roleRepository.findByName("ADMIN").get());
            adminRoles.add(roleRepository.findByName("DISPATCHER").get());
            admin.setRoles(adminRoles);

            userRepository.save(admin);
            System.out.println("Тестовый администратор создан: admin / admin");
        }

        // Создание тестового диспетчера
        if (userRepository.findByUsername("dispatcher").isEmpty()) {
            User dispatcher = new User();
            dispatcher.setUsername("dispatcher");
            dispatcher.setPassword(passwordEncoder.encode("dispatcher"));
            dispatcher.setEmail("dispatcher@example.com");

            Set<Role> dispatcherRoles = new HashSet<>();
            dispatcherRoles.add(roleRepository.findByName("DISPATCHER").get());
            dispatcher.setRoles(dispatcherRoles);

            userRepository.save(dispatcher);
            System.out.println("Тестовый диспетчер создан: dispatcher / dispatcher");
        }

        // Создание тестового исполнителя
        if (userRepository.findByUsername("worker").isEmpty()) {
            User worker = new User();
            worker.setUsername("worker");
            worker.setPassword(passwordEncoder.encode("worker"));
            worker.setEmail("worker@example.com");

            Set<Role> workerRoles = new HashSet<>();
            workerRoles.add(roleRepository.findByName("WORKER").get());
            worker.setRoles(workerRoles);

            userRepository.save(worker);
            System.out.println("Тестовый исполнитель создан: worker / worker");
        }

        // Создание тестового заявителя
        if (userRepository.findByUsername("requester").isEmpty()) {
            User requester = new User();
            requester.setUsername("requester");
            requester.setPassword(passwordEncoder.encode("requester"));
            requester.setEmail("requester@example.com");

            Set<Role> requesterRoles = new HashSet<>();
            requesterRoles.add(roleRepository.findByName("REQUESTER").get());
            requester.setRoles(requesterRoles);

            userRepository.save(requester);
            System.out.println("Тестовый заявитель создан: requester / requester");
        }
        if (requestRepository.count() == 0) {
            RequestStatus newStatus = requestStatusRepository.findByCode("NEW").orElse(null);
            User requester = userRepository.findByUsername("requester").orElse(null);
            User worker = userRepository.findByUsername("worker").orElse(null);

            if (newStatus != null && requester != null && worker != null) {
                Request testRequest = new Request();
                testRequest.setTitle("Тестовая заявка");
                testRequest.setDescription("Описание тестовой заявки");
                testRequest.setStatus(newStatus);
                testRequest.setCreatedBy(requester);
                testRequest.setAssignedTo(worker);
                testRequest.setPriority("MEDIUM");
                requestRepository.save(testRequest);
                System.out.println("Тестовая заявка создана");
            }
        }
        if (equipmentTypeRepository.count() == 0) {
            EquipmentType type1 = new EquipmentType();
            type1.setName("Насосное оборудование");
            type1.setDescription("Насосы, насосные станции, гидравлика");
            equipmentTypeRepository.save(type1);

            EquipmentType type2 = new EquipmentType();
            type2.setName("Конвейерное оборудование");
            type2.setDescription("Транспортёры, конвейеры");
            equipmentTypeRepository.save(type2);

            EquipmentType type3 = new EquipmentType();
            type3.setName("Дробильное оборудование");
            type3.setDescription("Дробилки, мельницы");
            equipmentTypeRepository.save(type3);

            System.out.println("Типы оборудования добавлены");
        }


    }

    private void createRoleIfNotFound(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            role.setDescription("Роль: " + roleName);
            roleRepository.save(role);
            System.out.println("Роль создана: " + roleName);
        }
    }


}