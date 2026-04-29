package com.example.vkr.repository;

import com.example.vkr.entity.Request;
import com.example.vkr.entity.RequestStatus;
import com.example.vkr.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Integer> {

    List<Request> findByStatus(RequestStatus status);
    List<Request> findByAssignedTo(User user);
    List<Request> findByCreatedBy(User user);


    // Поиск по заголовку или описанию
    @Query("SELECT r FROM Request r WHERE " +
            "LOWER(r.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Request> searchByTitleOrDescription(@Param("search") String search);

    // Фильтр по оборудованию
    List<Request> findByEquipment_EquipmentId(Integer equipmentId);

    // Фильтр по диапазону дат
    List<Request> findByDeadlineBetween(LocalDateTime start, LocalDateTime end);

    // Фильтр по статусу и оборудованию
    List<Request> findByStatusAndEquipment_EquipmentId(RequestStatus status, Integer equipmentId);

    // Сортировка
    List<Request> findAll(Sort sort);
}