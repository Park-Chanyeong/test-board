package com.example.demo.service;

import com.example.demo.dto.PostDto;
import com.example.demo.dto.PostDetailDto;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

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

    public Post findByIdInternal(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다: " + id));
    }

    public PostDetailDto create(PostDto dto, String username) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        if (username != null) {
            User author = userRepository.findByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + username));
            post.setAuthor(author);
        }
        return PostDetailDto.from(postRepository.save(post));
    }

    public PostDetailDto update(Long id, PostDto dto, String username) {
        Post post = findByIdInternal(id);
        verifyAuthor(post, username);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        return PostDetailDto.from(postRepository.save(post));
    }

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
