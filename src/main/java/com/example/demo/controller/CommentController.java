package com.example.demo.controller;

import com.example.demo.dto.CommentDetailDto;
import com.example.demo.dto.CommentDto;
import com.example.demo.dto.CommonResponse;
import com.example.demo.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
public class CommentController extends BaseController {

    private final CommentService commentService;

    @Operation(summary = "댓글 목록 조회")
    @GetMapping
    public ResponseEntity<CommonResponse<List<CommentDetailDto>>> getComments(@PathVariable Long postId) {
        return execute(HttpStatus.OK, () -> commentService.findByPostId(postId));
    }

    @Operation(summary = "댓글 작성")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<CommonResponse<CommentDetailDto>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return execute(HttpStatus.CREATED, () -> commentService.create(postId, dto, userDetails.getUsername()));
    }

    @Operation(summary = "댓글 삭제")
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<CommonResponse<Void>> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return executeVoid(() -> commentService.delete(commentId, userDetails.getUsername()));
    }
}
