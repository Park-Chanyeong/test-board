package com.example.demo.service;

import com.example.demo.dto.PostDetailDto;
import com.example.demo.dto.PostDto;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<PostDetailDto> findAllPaged(int page, int size) {
        return postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(PostDetailDto::from);
    }

    @Transactional
    public PostDetailDto findById(Long id) {
        Post post = findByIdInternal(id);
        post.setViewCount(post.getViewCount() + 1);
        return PostDetailDto.from(post);
    }

    @Transactional(readOnly = true)
    public Post findByIdInternal(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다: " + id));
    }

    @Transactional
    public PostDetailDto create(PostDto dto, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + username));
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setAuthor(author);
        return PostDetailDto.from(postRepository.save(post));
    }

    @Transactional
    public PostDetailDto update(Long id, PostDto dto, String username) {
        Post post = findByIdInternal(id);
        verifyAuthor(post, username);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        return PostDetailDto.from(postRepository.save(post));
    }

    @Transactional
    public void delete(Long id, String username) {
        Post post = findByIdInternal(id);
        verifyAuthor(post, username);
        postRepository.deleteById(id);
    }

    private void verifyAuthor(Post post, String username) {
        if (post.getAuthor() == null || !post.getAuthor().getUsername().equals(username)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }
    }
}
