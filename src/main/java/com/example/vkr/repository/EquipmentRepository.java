package com.example.vkr.repository;

import com.example.vkr.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Integer> {
    List<Equipment> findByIntervalDaysNotNull();
    List<Equipment> findByType_TypeId(Integer typeId);
}