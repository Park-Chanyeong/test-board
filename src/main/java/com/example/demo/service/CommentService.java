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

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostService postService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CommentDetailDto> findByPostId(Long postId) {
        return commentRepository.findAllByPost_IdOrderByCreatedAtAsc(postId)
                .stream().map(CommentDetailDto::from).toList();
    }

    @Transactional(readOnly = true)
    public Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다: " + id));
    }

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

    @Transactional
    public void delete(Long id, String username) {
        Comment comment = findById(id);
        if (comment.getAuthorUsername() == null || !comment.getAuthorUsername().equals(username)) {
            throw new AccessDeniedException("권한이 없습니다.");
        }
        commentRepository.deleteById(id);
    }
}
