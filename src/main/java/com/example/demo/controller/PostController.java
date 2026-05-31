package com.example.demo.controller;

import com.example.demo.dto.PostDto;
import com.example.demo.dto.PostDetailDto;
import com.example.demo.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Posts", description = "게시글 CRUD API")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 목록 조회", description = "전체 게시글 목록을 페이지 단위로 반환")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @GetMapping
    public Page<PostDetailDto> getAll(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size) {
        return postService.findAllPaged(page, size);
    }

    @Operation(summary = "게시글 단건 조회", description = "ID로 특정 게시글을 조회합니다. 인증 불필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
    })
    @GetMapping("/{id}")
    public PostDetailDto getOne(@Parameter(description = "게시글 ID", example = "1") @PathVariable Long id) {
        return postService.findById(id);
    }

    @Operation(summary = "게시글 작성", description = "새 게시글을 작성합니다. JWT 인증 필요.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<PostDetailDto> create(
            @RequestBody PostDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        PostDetailDto response = postService.create(dto, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다. JWT 인증 필요, 작성자 본인만 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PutMapping("/{id}")
    public PostDetailDto update(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long id,
            @RequestBody PostDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return postService.update(id, dto, userDetails.getUsername());
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다. JWT 인증 필요, 작성자 본인만 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게시글")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "게시글 ID", example = "1") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        postService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}