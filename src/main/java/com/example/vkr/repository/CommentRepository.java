package com.example.vkr.repository;

import com.example.vkr.entity.Comment;
import com.example.vkr.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByRequestOrderByCreatedAtAsc(Request request);
}