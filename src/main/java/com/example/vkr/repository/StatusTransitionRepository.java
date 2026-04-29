package com.example.vkr.repository;

import com.example.vkr.entity.StatusTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StatusTransitionRepository extends JpaRepository<StatusTransition, Long> {

    // Найти все разрешённые переходы из текущего статуса для определённой роли
    List<StatusTransition> findByFromStatusCodeAndRoleId(String fromStatusCode, String roleId);

    // Проверить, разрешён ли конкретный переход
    boolean existsByFromStatusCodeAndToStatusCodeAndRoleId(String fromStatusCode,
                                                           String toStatusCode,
                                                           String roleId);
}