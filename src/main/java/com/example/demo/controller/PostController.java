package com.example.demo.controller;

import com.example.demo.dto.CommonResponse;
import com.example.demo.dto.LikeDto;
import com.example.demo.dto.PageResponse;
import com.example.demo.dto.PostDetailDto;
import com.example.demo.dto.PostDto;
import com.example.demo.service.PostLikeService;
import com.example.demo.service.PostService;
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

@Tag(name = "Posts", description = "게시글 CRUD API")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController extends BaseController {

    private final PostService postService;
    private final PostLikeService postLikeService;

    @Operation(summary = "게시글 목록 조회")
    @GetMapping
    public ResponseEntity<CommonResponse<PageResponse<PostDetailDto>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return execute(HttpStatus.OK, () -> PageResponse.from(postService.findAllPaged(page, size)));
    }

    @Operation(summary = "게시글 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<PostDetailDto>> getOne(@PathVariable Long id) {
        return execute(HttpStatus.OK, () -> postService.findById(id));
    }

    @Operation(summary = "게시글 작성")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<CommonResponse<PostDetailDto>> create(
            @Valid @RequestBody PostDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return execute(HttpStatus.CREATED, () -> postService.create(dto, userDetails.getUsername()));
    }

    @Operation(summary = "게시글 수정")
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<PostDetailDto>> update(
            @PathVariable Long id,
            @Valid @RequestBody PostDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return execute(HttpStatus.OK, () -> postService.update(id, dto, userDetails.getUsername()));
    }

    @Operation(summary = "게시글 삭제")
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return executeVoid(() -> postService.delete(id, userDetails.getUsername()));
    }

    @Operation(summary = "좋아요 정보 조회")
    @GetMapping("/{id}/like")
    public ResponseEntity<CommonResponse<LikeDto>> getLikeInfo(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        return execute(HttpStatus.OK, () -> postLikeService.getLikeStatus(id, username));
    }

    @Operation(summary = "좋아요 토글")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping("/{id}/like")
    public ResponseEntity<CommonResponse<LikeDto>> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return execute(HttpStatus.OK, () -> postLikeService.toggle(id, userDetails.getUsername()));
    }
}
