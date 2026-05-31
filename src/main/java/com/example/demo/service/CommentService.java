package com.example.demo.service;

import com.example.demo.dto.CommentDetailDto;
import com.example.demo.dto.CommentDto;
import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 댓글 비즈니스 로직 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserRepository userRepository;

    /** 특정 게시글의 댓글 목록을 오래된 순으로 반환 */
    @Transactional(readOnly = true)
    public List<CommentDetailDto> findByPostId(Long postId) {
        return commentRepository.findAllByPost_IdOrderByCreatedAtAsc(postId)
                .stream().map(CommentDetailDto::from).toList();
    }

    /** 댓글 단건 조회 — 없으면 404 */
    @Transactional(readOnly = true)
    public Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다: " + id));
    }

    /** 댓글 작성 — 게시글 존재 여부 먼저 확인 */
    @Transactional
    public CommentDetailDto create(Long postId, CommentDto dto, String username) {
        Post post = postService.findByIdInternal(postId);
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다: " + username));
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setContent(dto.getContent());
        return CommentDetailDto.from(commentRepository.save(comment));
    }

    /** 댓글 삭제 — 작성자 본인이 아니면 403 */
    @Transactional
    public void delete(Long id, String username) {
        Comment comment = findById(id);
        if (comment.getAuthorUsername() == null || !comment.getAuthorUsername().equals(username)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }
        commentRepository.deleteById(id);
    }
}
