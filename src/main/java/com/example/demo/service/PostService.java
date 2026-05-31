package com.example.demo.service;

import com.example.demo.dto.PostDetailDto;
import com.example.demo.dto.PostDto;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostLikeRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 게시글 CRUD 비즈니스 로직 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;

    /** 최신순 페이징 목록 조회 */
    @Transactional(readOnly = true)
    public Page<PostDetailDto> findAllPaged(int page, int size) {
        return postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size))
                .map(PostDetailDto::from);
    }

    /** 단건 조회 + 조회수 1 증가 */
    @Transactional
    public PostDetailDto findById(Long id) {
        Post post = findByIdInternal(id);
        post.setViewCount(post.getViewCount() + 1); // dirty checking으로 UPDATE 자동 실행
        return PostDetailDto.from(post);
    }

    /** 내부용 엔티티 조회 — 다른 서비스에서 Post 객체가 필요할 때 재사용 */
    @Transactional(readOnly = true)
    public Post findByIdInternal(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다: " + id));
    }

    /** 게시글 작성 */
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

    /** 게시글 수정 — save() 없이 dirty checking으로 UPDATE 처리 */
    @Transactional
    public PostDetailDto update(Long id, PostDto dto, String username) {
        Post post = findByIdInternal(id);
        verifyAuthor(post, username);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        return PostDetailDto.from(post);
    }

    /**
     * 게시글 삭제.
     */
    @Transactional
    public void delete(Long id, String username) {
        Post post = findByIdInternal(id);
        verifyAuthor(post, username);
        postLikeRepository.deleteAllByPost(post);
        commentRepository.deleteAllByPost(post);
        postRepository.deleteById(id);
    }

    /** 작성자 본인 여부 확인 — 불일치 시 403 */
    private void verifyAuthor(Post post, String username) {
        if (post.getAuthor() == null || !post.getAuthor().getUsername().equals(username)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }
    }
}
