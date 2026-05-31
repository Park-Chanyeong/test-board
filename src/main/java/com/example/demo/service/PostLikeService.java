package com.example.demo.service;

import com.example.demo.dto.LikeDto;
import com.example.demo.entity.Post;
import com.example.demo.entity.PostLike;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.repository.PostLikeRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostService postService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public LikeDto getLikeStatus(Long postId, String username) {
        long count = postLikeRepository.countByPost_Id(postId);
        if (username == null) return new LikeDto(count, false);
        boolean liked = postLikeRepository.existsByPost_IdAndUser_Username(postId, username);
        return new LikeDto(count, liked);
    }

    @Transactional
    public LikeDto toggle(Long postId, String username) {
        Post post = postService.findByIdInternal(postId);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + username));

        boolean liked;
        if (postLikeRepository.existsByPostAndUser(post, user)) {
            postLikeRepository.deleteByPostAndUser(post, user);
            liked = false;
        } else {
            PostLike like = new PostLike();
            like.setPost(post);
            like.setUser(user);
            postLikeRepository.save(like);
            liked = true;
        }
        return new LikeDto(postLikeRepository.countByPost(post), liked);
    }
}
