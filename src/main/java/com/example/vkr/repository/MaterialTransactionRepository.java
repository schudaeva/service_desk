package com.example.vkr.repository;

import com.example.vkr.entity.MaterialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialTransactionRepository extends JpaRepository<MaterialTransaction, Long> {
}