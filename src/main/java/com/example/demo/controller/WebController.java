package com.example.demo.controller;

import com.example.demo.dto.CommentDto;
import com.example.demo.dto.LikeDto;
import com.example.demo.dto.PostDto;
import com.example.demo.dto.RegisterDto;
import com.example.demo.entity.Post;
import com.example.demo.exception.AppException;
import com.example.demo.service.AuthService;
import com.example.demo.service.CommentService;
import com.example.demo.service.PostLikeService;
import com.example.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final PostService postService;
    private final CommentService commentService;
    private final PostLikeService postLikeService;
    private final AuthService authService;

    // ── redirect ──────────────────────────────────────────────

    @GetMapping("/")
    public String home() {
        return "redirect:/web/posts";
    }

    // ── auth ──────────────────────────────────────────────────

    @GetMapping("/web/login")
    public String loginForm(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        return "auth/login";
    }

    @GetMapping("/web/register")
    public String registerForm(Model model) {
        model.addAttribute("dto", new RegisterDto());
        return "auth/register";
    }

    @PostMapping("/web/register")
    public String register(@ModelAttribute RegisterDto dto, Model model) {
        try {
            authService.register(dto);
            return "redirect:/web/login?registered";
        } catch (AppException e) {
            model.addAttribute("error", "이미 존재하는 사용자입니다.");
            model.addAttribute("dto", dto);
            return "auth/register";
        }
    }

    // ── posts ─────────────────────────────────────────────────

    @GetMapping("/web/posts")
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        model.addAttribute("posts", postService.findAllPaged(page, size));
        return "posts/list";
    }

    @GetMapping("/web/posts/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("post", postService.findById(id));
        model.addAttribute("comments", commentService.findByPostId(id));
        model.addAttribute("commentDto", new CommentDto());
        String username = resolveUsername(authentication);
        LikeDto likeStatus = postLikeService.getLikeStatus(id, username);
        model.addAttribute("likeCount", likeStatus.getCount());
        model.addAttribute("hasLiked", likeStatus.isLiked());
        return "posts/detail";
    }

    @GetMapping("/web/posts/new")
    public String newForm(Model model) {
        model.addAttribute("dto", new PostDto());
        return "posts/form";
    }

    @PostMapping("/web/posts")
    public String create(@ModelAttribute PostDto dto, Authentication authentication) {
        if (!isAuthenticated(authentication)) return "redirect:/web/login";
        postService.create(dto, authentication.getName());
        return "redirect:/web/posts";
    }

    @GetMapping("/web/posts/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, Authentication authentication) {
        Post post = postService.findByIdInternal(id);
        if (!isOwner(post, authentication)) return "redirect:/web/posts/" + id;
        model.addAttribute("post", post);
        return "posts/edit";
    }

    @PostMapping("/web/posts/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute PostDto dto, Authentication authentication) {
        postService.update(id, dto, authentication.getName());
        return "redirect:/web/posts/" + id;
    }

    @PostMapping("/web/posts/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication) {
        postService.delete(id, authentication.getName());
        return "redirect:/web/posts";
    }

    @PostMapping("/web/posts/{id}/comments")
    public String addComment(@PathVariable Long id, @ModelAttribute CommentDto dto, Authentication authentication) {
        if (!isAuthenticated(authentication)) return "redirect:/web/login";
        commentService.create(id, dto, authentication.getName());
        return "redirect:/web/posts/" + id + "#comments";
    }

    @PostMapping("/web/posts/{id}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long id, @PathVariable Long commentId, Authentication authentication) {
        commentService.delete(commentId, authentication.getName());
        return "redirect:/web/posts/" + id + "#comments";
    }

    @PostMapping("/web/posts/{id}/like")
    public String toggleLike(@PathVariable Long id, Authentication authentication) {
        if (!isAuthenticated(authentication)) return "redirect:/web/login";
        postLikeService.toggle(id, authentication.getName());
        return "redirect:/web/posts/" + id;
    }

    // ── helpers ───────────────────────────────────────────────

    private boolean isAuthenticated(Authentication auth) {
        return auth != null && auth.isAuthenticated();
    }

    private boolean isOwner(Post post, Authentication auth) {
        return isAuthenticated(auth)
                && post.getAuthorUsername() != null
                && post.getAuthorUsername().equals(auth.getName());
    }

    private String resolveUsername(Authentication auth) {
        return isAuthenticated(auth) ? auth.getName() : null;
    }
}
