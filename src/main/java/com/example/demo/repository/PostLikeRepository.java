package com.example.demo.repository;

import com.example.demo.entity.Post;
import com.example.demo.entity.PostLike;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    long countByPost_Id(Long postId);
    boolean existsByPost_IdAndUser_Username(Long postId, String username);

    boolean existsByPostAndUser(Post post, User user);
    long countByPost(Post post);

    @Modifying
    @Query("DELETE FROM PostLike l WHERE l.post = :post AND l.user = :user")
    void deleteByPostAndUser(Post post, User user);

    @Modifying
    @Query("DELETE FROM PostLike l WHERE l.post = :post")
    void deleteAllByPost(Post post);
}
