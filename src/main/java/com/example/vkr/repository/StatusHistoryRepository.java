package com.example.vkr.repository;

import com.example.vkr.entity.Request;
import com.example.vkr.entity.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByRequestOrderByChangedAtDesc(Request request);
}