package com.example.vkr.repository;

import com.example.vkr.entity.Request;
import com.example.vkr.entity.RequestMaterialActual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RequestMaterialActualRepository extends JpaRepository<RequestMaterialActual, Long> {
    List<RequestMaterialActual> findByRequest(Request request);
}