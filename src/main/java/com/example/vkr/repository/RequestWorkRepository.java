package com.example.vkr.repository;

import com.example.vkr.entity.Request;
import com.example.vkr.entity.RequestWork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestWorkRepository extends JpaRepository<RequestWork, Long> {
    List<RequestWork> findByRequestOrderByCreatedAtAsc(Request request);
}