package com.example.vkr.repository;

import com.example.vkr.entity.Request;
import com.example.vkr.entity.RequestMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RequestMaterialRepository extends JpaRepository<RequestMaterial, Long> {
    List<RequestMaterial> findByRequest(Request request);
    void deleteByRequest(Request request);
}