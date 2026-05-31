package com.example.demo.controller;

import com.example.demo.dto.CommonResponse;
import com.example.demo.dto.LikeDto;
import com.example.demo.service.PostLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Likes", description = "좋아요 API")
@RestController
@RequestMapping("/api/posts/{postId}/like")
@RequiredArgsConstructor
public class PostLikeController extends BaseController {

    private final PostLikeService postLikeService;

    @Operation(summary = "좋아요 정보 조회", description = "좋아요 수와 현재 유저의 좋아요 여부를 반환합니다. 인증 불필요.")
    @GetMapping
    public ResponseEntity<CommonResponse<LikeDto>> getLikeInfo(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        return execute(HttpStatus.OK, () -> postLikeService.getLikeStatus(postId, username));
    }

    @Operation(summary = "좋아요 토글", description = "좋아요를 누르거나 취소합니다. JWT 인증 필요.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<CommonResponse<LikeDto>> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return execute(HttpStatus.OK, () -> postLikeService.toggle(postId, userDetails.getUsername()));
    }
}
