package com.example.vkr.controller;
import com.example.vkr.dto.RequestCreateDto;
import com.example.vkr.dto.RequestResponseDto;
import com.example.vkr.entity.*;
import com.example.vkr.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestRepository requestRepository;
    private final RequestStatusRepository requestStatusRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final MaterialRepository materialRepository;
    private final CommentRepository commentRepository;
    private final StatusTransitionRepository statusTransitionRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final RequestMaterialRepository requestMaterialRepository;
    private final MaterialTransactionRepository materialTransactionRepository;
    private final RequestWorkRepository requestWorkRepository;
    private final RequestMaterialActualRepository requestMaterialActualRepository;

    @GetMapping
    public String listRequests(Model model,
                               @AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) Integer equipmentId,
                               @RequestParam(required = false) String statusCode,
                               @RequestParam(required = false) Integer assignedToId,
                               @RequestParam(required = false) String priority,
                               @RequestParam(required = false) LocalDateTime deadlineFrom,
                               @RequestParam(required = false) LocalDateTime deadlineTo,
                               @RequestParam(defaultValue = "requestId") String sortField,
                               @RequestParam(defaultValue = "desc") String sortDir,
                               @RequestParam(required = false) String filter) {

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Request> baseRequests;
        boolean isDispatcher = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("DISPATCHER") || r.getName().equals("ADMIN"));
        boolean isWorker = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("WORKER"));

        if (isDispatcher) {
            baseRequests = requestRepository.findAll();
            System.out.println("Диспетчер/Админ: показано " + baseRequests.size() + " заявок");
        } else if (isWorker) {
            baseRequests = requestRepository.findByAssignedTo(currentUser);
            System.out.println("Исполнитель " + currentUser.getUsername() + ": показано " + baseRequests.size() + " заявок");
        } else {
            baseRequests = requestRepository.findByCreatedBy(currentUser);
            System.out.println("Заявитель " + currentUser.getUsername() + ": показано " + baseRequests.size() + " заявок");
        }

        List<Request> filtered = baseRequests;

        if (search != null && !search.isBlank()) {
            String s = search.toLowerCase();
            filtered = filtered.stream()
                    .filter(r -> (r.getTitle() != null && r.getTitle().toLowerCase().contains(s)) ||
                            (r.getDescription() != null && r.getDescription().toLowerCase().contains(s)))
                    .collect(Collectors.toList());
            System.out.println("После поиска '" + search + "': " + filtered.size() + " заявок");
        }

        if (equipmentId != null) {
            filtered = filtered.stream()
                    .filter(r -> r.getEquipment() != null && r.getEquipment().getEquipmentId().equals(equipmentId))
                    .collect(Collectors.toList());
            System.out.println("После фильтра по оборудованию " + equipmentId + ": " + filtered.size() + " заявок");
        }

        if (statusCode != null && !statusCode.isBlank()) {
            filtered = filtered.stream()
                    .filter(r -> r.getStatus() != null && r.getStatus().getCode().equals(statusCode))
                    .collect(Collectors.toList());
            System.out.println("После фильтра по статусу " + statusCode + ": " + filtered.size() + " заявок");
        }

        if (assignedToId != null) {
            filtered = filtered.stream()
                    .filter(r -> r.getAssignedTo() != null && r.getAssignedTo().getUserId().equals(assignedToId))
                    .collect(Collectors.toList());
            System.out.println("После фильтра по исполнителю " + assignedToId + ": " + filtered.size() + " заявок");
        }

        if (priority != null && !priority.isBlank()) {
            filtered = filtered.stream()
                    .filter(r -> priority.equals(r.getPriority()))
                    .collect(Collectors.toList());
            System.out.println("После фильтра по приоритету " + priority + ": " + filtered.size() + " заявок");
        }

        if (deadlineFrom != null) {
            filtered = filtered.stream()
                    .filter(r -> r.getDeadline() != null && !r.getDeadline().isBefore(deadlineFrom))
                    .collect(Collectors.toList());
        }

        if (deadlineTo != null) {
            filtered = filtered.stream()
                    .filter(r -> r.getDeadline() != null && !r.getDeadline().isAfter(deadlineTo))
                    .collect(Collectors.toList());
        }

        if ("overdue".equals(filter)) {
            filtered = filtered.stream()
                    .filter(r -> r.getDeadline() != null
                            && r.getDeadline().isBefore(LocalDateTime.now())
                            && r.getStatus() != null
                            && !"COMPLETED".equals(r.getStatus().getCode())
                            && !"CANCELLED".equals(r.getStatus().getCode()))
                    .collect(Collectors.toList());
            model.addAttribute("filterApplied", "Показаны только просроченные заявки");
        }


        Comparator<Request> comparator;
        if ("deadline".equals(sortField)) {
            comparator = Comparator.comparing(Request::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()));
        } else {
            comparator = Comparator.comparing(Request::getRequestId);
        }
        if ("desc".equals(sortDir)) {
            comparator = comparator.reversed();
        }
        filtered.sort(comparator);

        List<RequestResponseDto> responseDtos = filtered.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        model.addAttribute("requests", responseDtos);
        model.addAttribute("equipmentList", equipmentRepository.findAll());
        model.addAttribute("statuses", requestStatusRepository.findAll());
        model.addAttribute("workers", userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("WORKER")))
                .collect(Collectors.toList()));
        model.addAttribute("search", search);
        model.addAttribute("selectedEquipmentId", equipmentId);
        model.addAttribute("selectedStatusCode", statusCode);
        model.addAttribute("selectedAssignedToId", assignedToId);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedDeadlineFrom", deadlineFrom);
        model.addAttribute("selectedDeadlineTo", deadlineTo);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);

        return "requests/list";
    }


    private boolean applySearchFilter(Request request, String search) {
        if (search == null || search.isBlank()) return true;
        String lowerSearch = search.toLowerCase();
        return (request.getTitle() != null && request.getTitle().toLowerCase().contains(lowerSearch)) ||
                (request.getDescription() != null && request.getDescription().toLowerCase().contains(lowerSearch));
    }

    private boolean applyEquipmentFilter(Request request, Integer equipmentId) {
        if (equipmentId == null) return true;
        return request.getEquipment() != null && request.getEquipment().getEquipmentId().equals(equipmentId);
    }

    private boolean applyStatusFilter(Request request, String statusCode) {
        if (statusCode == null || statusCode.isBlank()) return true;
        return request.getStatus() != null && request.getStatus().getCode().equals(statusCode);
    }

    private boolean applyDeadlineRangeFilter(Request request, LocalDateTime from, LocalDateTime to) {
        if (from == null && to == null) return true;
        LocalDateTime deadline = request.getDeadline();
        if (deadline == null) return false;
        if (from != null && deadline.isBefore(from)) return false;
        if (to != null && deadline.isAfter(to)) return false;
        return true;
    }


    private List<Request> applySorting(List<Request> requests, String sortField, String sortDir) {
        if (sortField == null) sortField = "requestId";
        if (sortDir == null) sortDir = "desc";

        boolean ascending = "asc".equalsIgnoreCase(sortDir);

        String finalSortField = sortField;
        return requests.stream().sorted((r1, r2) -> {
            int comparison;
            if ("deadline".equals(finalSortField)) {
                LocalDateTime d1 = r1.getDeadline();
                LocalDateTime d2 = r2.getDeadline();
                if (d1 == null && d2 == null) comparison = 0;
                else if (d1 == null) comparison = 1;
                else if (d2 == null) comparison = -1;
                else comparison = d1.compareTo(d2);
            } else {
                comparison = r1.getRequestId().compareTo(r2.getRequestId());
            }
            return ascending ? comparison : -comparison;
        }).collect(Collectors.toList());
    }


    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("requestCreateDto", new RequestCreateDto());
        model.addAttribute("equipmentList", equipmentRepository.findAll());
        model.addAttribute("workers", userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("WORKER")))
                .collect(Collectors.toList()));
        model.addAttribute("materials", materialRepository.findAll());
        return "requests/create";
    }


    @PostMapping("/create")
    public String createRequest(@ModelAttribute RequestCreateDto createDto,
                                @AuthenticationPrincipal UserDetails userDetails) {

        User creator = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RequestStatus newStatus = requestStatusRepository.findByCode("NEW")
                .orElseThrow(() -> new RuntimeException("Status NEW not found"));

        Request request = new Request();
        request.setTitle(createDto.getTitle());
        request.setDescription(createDto.getDescription());
        request.setPriority(createDto.getPriority());
        request.setDeadline(createDto.getDeadline());
        request.setCreatedBy(creator);
        request.setSource("WEB");
        request.setStatus(newStatus);

        if (createDto.getEquipmentId() != null) {
            Equipment equipment = equipmentRepository.findById(createDto.getEquipmentId())
                    .orElseThrow(() -> new RuntimeException("Equipment not found"));
            request.setEquipment(equipment);
        }

        if (createDto.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(createDto.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            request.setAssignedTo(assignedTo);

            RequestStatus assignedStatus = requestStatusRepository.findByCode("ASSIGNED")
                    .orElseThrow(() -> new RuntimeException("Status ASSIGNED not found"));
            request.setStatus(assignedStatus);
        }

        Request savedRequest = requestRepository.save(request);


        if (createDto.getMaterialIds() != null && createDto.getPlannedQuantities() != null) {
            int size = Math.min(createDto.getMaterialIds().size(), createDto.getPlannedQuantities().size());
            for (int i = 0; i < size; i++) {
                Integer materialId = createDto.getMaterialIds().get(i);
                Integer quantity = createDto.getPlannedQuantities().get(i);

                if (materialId != null && quantity != null && quantity > 0) {
                    Material material = materialRepository.findById(materialId)
                            .orElseThrow(() -> new RuntimeException("Material not found"));

                    RequestMaterial rm = new RequestMaterial();
                    rm.setRequest(savedRequest);
                    rm.setMaterial(material);
                    rm.setPlannedQuantity(quantity);
                    requestMaterialRepository.save(rm);
                }
            }
        }


        StatusHistory history = new StatusHistory();
        history.setRequest(savedRequest);
        history.setStatus(savedRequest.getStatus());
        history.setUser(creator);
        statusHistoryRepository.save(history);

        return "redirect:/requests";
    }

    @GetMapping("/{id}")
    public String viewRequest(@PathVariable Integer id, Model model,
                              @AuthenticationPrincipal UserDetails userDetails) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));


        List<Comment> comments = commentRepository.findByRequestOrderByCreatedAtAsc(request);
        // Выполненные работы
        List<RequestWork> works = requestWorkRepository.findByRequestOrderByCreatedAtAsc(request);
        Double totalHours = works.stream().mapToDouble(RequestWork::getHoursSpent).sum();

        List<RequestMaterial> requestMaterials = requestMaterialRepository.findByRequest(request);
        model.addAttribute("requestMaterials", requestMaterials);
        List<RequestMaterialActual> actualMaterials = requestMaterialActualRepository.findByRequest(request);
        model.addAttribute("actualMaterials", actualMaterials);
        model.addAttribute("materials", materialRepository.findAll());  // для выпадающего списка


        RequestResponseDto requestDto = convertToDto(request);
        requestDto.setTotalHours(totalHours);


        String userRole = currentUser.getRoles().stream()
                .map(Role::getName)
                .findFirst()
                .orElse("");

        String currentStatusCode = request.getStatus().getCode();
        List<StatusTransition> availableTransitions = statusTransitionRepository
                .findByFromStatusCodeAndRoleId(currentStatusCode, userRole);

        model.addAttribute("request", requestDto);
        model.addAttribute("comments", comments);
        model.addAttribute("availableTransitions", availableTransitions);
        model.addAttribute("works", works);
        model.addAttribute("totalHours", totalHours);

        return "requests/detail";
    }


    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model,
                               @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean canEdit = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ADMIN") || r.getName().equals("DISPATCHER"));

        if (!canEdit) {
            return "redirect:/requests?accessDenied";
        }

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        List<RequestMaterial> requestMaterials = requestMaterialRepository.findByRequest(request);

        RequestCreateDto createDto = new RequestCreateDto();
        createDto.setTitle(request.getTitle());
        createDto.setDescription(request.getDescription());
        createDto.setPriority(request.getPriority());
        createDto.setDeadline(request.getDeadline());


        if (request.getDeadline() != null) {
            createDto.setFormattedDeadline(request.getDeadline().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
        }

        if (request.getEquipment() != null) {
            createDto.setEquipmentId(request.getEquipment().getEquipmentId());
        }
        if (request.getAssignedTo() != null) {
            createDto.setAssignedToId(request.getAssignedTo().getUserId());
        }

        List<Integer> materialIds = new ArrayList<>();
        List<Integer> plannedQuantities = new ArrayList<>();
        for (RequestMaterial rm : requestMaterials) {
            materialIds.add(rm.getMaterial().getMaterialId());
            plannedQuantities.add(rm.getPlannedQuantity());
        }
        createDto.setMaterialIds(materialIds);
        createDto.setPlannedQuantities(plannedQuantities);

        model.addAttribute("requestCreateDto", createDto);
        model.addAttribute("requestId", id);
        model.addAttribute("equipmentList", equipmentRepository.findAll());
        model.addAttribute("workers", userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("WORKER")))
                .collect(Collectors.toList()));
        model.addAttribute("materials", materialRepository.findAll());

        return "requests/edit";
    }



    @PostMapping("/edit/{id}")
    @Transactional
    public String updateRequest(@PathVariable Integer id,
                                @ModelAttribute RequestCreateDto createDto,
                                @AuthenticationPrincipal UserDetails userDetails) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Integer oldAssigneeId = request.getAssignedTo() != null
                ? request.getAssignedTo().getUserId()
                : null;

        request.setTitle(createDto.getTitle());
        request.setDescription(createDto.getDescription());
        request.setPriority(createDto.getPriority());
        request.setDeadline(createDto.getDeadline());

        if (createDto.getEquipmentId() != null) {
            Equipment equipment = equipmentRepository.findById(createDto.getEquipmentId())
                    .orElseThrow(() -> new RuntimeException("Equipment not found"));
            request.setEquipment(equipment);
        } else {
            request.setEquipment(null);
        }

        Integer newAssigneeId = createDto.getAssignedToId();
        boolean wasUnassigned = (oldAssigneeId == null);
        boolean isNowAssigned = (newAssigneeId != null);

        if (newAssigneeId != null) {
            User assignedTo = userRepository.findById(newAssigneeId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            request.setAssignedTo(assignedTo);
        } else {
            request.setAssignedTo(null);
        }

        boolean statusIsNew = request.getStatus().getCode().equals("NEW");

        if (wasUnassigned && isNowAssigned && statusIsNew) {
            RequestStatus assignedStatus = requestStatusRepository.findByCode("ASSIGNED")
                    .orElseThrow(() -> new RuntimeException("Status ASSIGNED not found"));
            request.setStatus(assignedStatus);


            StatusHistory history = new StatusHistory();
            history.setRequest(request);
            history.setStatus(assignedStatus);
            history.setUser(currentUser);
            history.setChangedAt(LocalDateTime.now());
            statusHistoryRepository.save(history);

            System.out.println("Статус изменён на ASSIGNED при назначении исполнителя");
        }

        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(request);

        requestMaterialRepository.deleteByRequest(request);

        if (createDto.getMaterialIds() != null && createDto.getPlannedQuantities() != null) {
            int size = Math.min(createDto.getMaterialIds().size(), createDto.getPlannedQuantities().size());
            for (int i = 0; i < size; i++) {
                Integer materialId = createDto.getMaterialIds().get(i);
                Integer quantity = createDto.getPlannedQuantities().get(i);

                if (materialId != null && quantity != null && quantity > 0) {
                    Material material = materialRepository.findById(materialId)
                            .orElseThrow(() -> new RuntimeException("Material not found"));

                    RequestMaterial rm = new RequestMaterial();
                    rm.setRequest(request);
                    rm.setMaterial(material);
                    rm.setPlannedQuantity(quantity);
                    requestMaterialRepository.save(rm);
                }
            }
        }

        return "redirect:/requests/" + id;
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

        if (request.getEquipment() != null) {
            dto.setEquipmentName(request.getEquipment().getName());
            dto.setEquipmentId(request.getEquipment().getEquipmentId());
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
                dto.setRowCssClass("overdue-row");
            } else if (request.getDeadline().isBefore(now.plusHours(24))) {
                dto.setRowCssClass("warning-row");
            }
        }
        System.out.println("Request " + request.getRequestId() + " deadline: " + request.getDeadline());
        System.out.println("  rowCssClass: " + dto.getRowCssClass());

        return dto;
    }



    @GetMapping("/delete/{id}")
    public String deleteRequest(@PathVariable Integer id,
                                @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean canDelete = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ADMIN") || r.getName().equals("DISPATCHER"));

        if (!canDelete) {
            return "redirect:/requests?accessDenied";
        }

        requestRepository.deleteById(id);
        return "redirect:/requests";
    }


    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Integer id,
                               @RequestParam String statusCode,
                               @AuthenticationPrincipal UserDetails userDetails) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String currentStatusCode = request.getStatus().getCode();
        String userRole = currentUser.getRoles().stream()
                .map(Role::getName)
                .findFirst()
                .orElse("");


        boolean canChange = userRole.equals("ADMIN") ||
                statusTransitionRepository.existsByFromStatusCodeAndToStatusCodeAndRoleId(
                        currentStatusCode, statusCode, userRole);

        if (!canChange) {
            return "redirect:/requests/" + id + "?error=forbidden";
        }

        RequestStatus newStatus = requestStatusRepository.findByCode(statusCode)
                .orElseThrow(() -> new RuntimeException("Status not found"));

        request.setStatus(newStatus);
        request.setUpdatedAt(LocalDateTime.now());
        requestRepository.save(request);

        StatusHistory history = new StatusHistory();
        history.setRequest(request);
        history.setStatus(newStatus);
        history.setUser(currentUser);
        history.setChangedAt(LocalDateTime.now());
        statusHistoryRepository.save(history);

        return "redirect:/requests/" + id;
    }

    @GetMapping("/{id}/history")
    public String viewHistory(@PathVariable Integer id, Model model,
                              @AuthenticationPrincipal UserDetails userDetails) {
        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        List<StatusHistory> history = statusHistoryRepository.findByRequestOrderByChangedAtDesc(request);

        model.addAttribute("request", request);
        model.addAttribute("history", history);

        return "requests/history";
    }

    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable Integer id,
                             @RequestParam String text,
                             @AuthenticationPrincipal UserDetails userDetails) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (text == null || text.trim().isEmpty()) {
            return "redirect:/requests/" + id + "?error=emptyComment";
        }

        Comment comment = new Comment();
        comment.setRequest(request);
        comment.setUser(user);
        comment.setText(text);
        comment.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        return "redirect:/requests/" + id + "?success=commentAdded";
    }

    @PostMapping("/{id}/close")
    public String closeRequest(@PathVariable Integer id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam(required = false) List<Integer> actualQuantities) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Проверка прав
        boolean isWorker = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("WORKER"));
        boolean isDispatcher = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("DISPATCHER") || r.getName().equals("ADMIN"));

        if (!isWorker && !isDispatcher) {
            return "redirect:/requests/" + id + "?error=accessDenied";
        }

        List<RequestMaterial> requestMaterials = requestMaterialRepository.findByRequest(request);

        // Проверяем, все ли материалы есть на складе
        for (int i = 0; i < requestMaterials.size(); i++) {
            RequestMaterial rm = requestMaterials.get(i);
            Integer actualQty = (actualQuantities != null && i < actualQuantities.size())
                    ? actualQuantities.get(i) : rm.getPlannedQuantity();

            if (rm.getMaterial().getQuantity() < actualQty) {
                return "redirect:/requests/" + id + "?error=notEnough&material=" + rm.getMaterial().getName();
            }
        }


        for (int i = 0; i < requestMaterials.size(); i++) {
            RequestMaterial rm = requestMaterials.get(i);
            Integer actualQty = (actualQuantities != null && i < actualQuantities.size())
                    ? actualQuantities.get(i) : rm.getPlannedQuantity();

            Material material = rm.getMaterial();
            material.setQuantity(material.getQuantity() - actualQty);
            materialRepository.save(material);

            rm.setActualQuantity(actualQty);
            requestMaterialRepository.save(rm);
        }


        RequestStatus completedStatus = requestStatusRepository.findByCode("COMPLETED")
                .orElseThrow(() -> new RuntimeException("Status COMPLETED not found"));
        request.setStatus(completedStatus);
        requestRepository.save(request);

        StatusHistory history = new StatusHistory();
        history.setRequest(request);
        history.setStatus(completedStatus);
        history.setUser(currentUser);
        statusHistoryRepository.save(history);

        return "redirect:/requests/" + id + "?success=closed";
    }

    @PostMapping("/{id}/work/add")
    public String addWork(@PathVariable Integer id,
                          @RequestParam String description,
                          @RequestParam Double hoursSpent,
                          @AuthenticationPrincipal UserDetails userDetails) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        User worker = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        RequestWork work = new RequestWork();
        work.setRequest(request);
        work.setWorker(worker);
        work.setDescription(description);
        work.setHoursSpent(hoursSpent);
        requestWorkRepository.save(work);

        return "redirect:/requests/" + id;
    }
    @PostMapping("/{id}/material/actual/add")
    public String addActualMaterial(@PathVariable Integer id,
                                    @RequestParam Integer materialId,
                                    @RequestParam Integer quantity,
                                    @AuthenticationPrincipal UserDetails userDetails) {

        Request request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Материал не найден"));

        // Проверка остатка на складе
        if (material.getQuantity() < quantity) {
            return "redirect:/requests/" + id + "?error=notEnoughMaterial";
        }

        // Списание со склада
        material.setQuantity(material.getQuantity() - quantity);
        materialRepository.save(material);

        // Сохранение в историю фактических материалов
        RequestMaterialActual actual = new RequestMaterialActual();
        actual.setRequest(request);
        actual.setMaterial(material);
        actual.setQuantity(quantity);
        actual.setAddedBy(user);
        requestMaterialActualRepository.save(actual);

        return "redirect:/requests/" + id;
    }
    @GetMapping("/{id}/material/actual/delete/{actualId}")
    public String deleteActualMaterial(@PathVariable Integer id,
                                       @PathVariable Long actualId,
                                       @AuthenticationPrincipal UserDetails userDetails) {

        RequestMaterialActual actual = requestMaterialActualRepository.findById(actualId)
                .orElseThrow(() -> new RuntimeException("Запись не найдена"));

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Только ADMIN и DISPATCHER могут удалять
        boolean canDelete = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ADMIN") || r.getName().equals("DISPATCHER"));

        if (!canDelete) {
            return "redirect:/requests/" + id + "?error=accessDenied";
        }

        // Возвращаем материал на склад
        Material material = actual.getMaterial();
        material.setQuantity(material.getQuantity() + actual.getQuantity());
        materialRepository.save(material);

        requestMaterialActualRepository.delete(actual);

        return "redirect:/requests/" + id;
    }


}