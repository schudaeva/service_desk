package com.example.vkr.repository;

import com.example.vkr.entity.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RequestTypeRepository extends JpaRepository<RequestType, Integer> {
    List<RequestType> findByIsActiveTrue();
}