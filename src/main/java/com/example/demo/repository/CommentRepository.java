package com.example.demo.repository;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @EntityGraph(attributePaths = "author")
    List<Comment> findAllByPost_IdOrderByCreatedAtAsc(Long postId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.post = :post")
    void deleteAllByPost(Post post);
}
