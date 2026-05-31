package com.example.demo.controller;

import com.example.demo.dto.CommentDto;
import com.example.demo.dto.CommentDetailDto;
import com.example.demo.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comments", description = "댓글 API")
@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 목록 조회", description = "게시글의 댓글 목록을 반환합니다. 인증 불필요.")
    @GetMapping
    public List<CommentDetailDto> getComments(@PathVariable Long postId) {
        return commentService.findByPostId(postId);
    }

    @Operation(summary = "댓글 작성", description = "댓글을 작성합니다. JWT 인증 필요.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<CommentDetailDto> createComment(
            @PathVariable Long postId,
            @RequestBody CommentDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        CommentDetailDto response = commentService.create(postId, dto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. JWT 인증 필요, 작성자 본인만 가능.")
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        commentService.delete(commentId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}