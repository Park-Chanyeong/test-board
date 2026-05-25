package com.example.demo.controller;

import com.example.demo.dto.PostDto;
import com.example.demo.entity.Post;
import com.example.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public List<Post> getAll() {
        return postService.findAll();
    }

    @GetMapping("/{id}")
    public Post getOne(@PathVariable Long id) {
        return postService.findById(id);
    }

    @PostMapping
    public Post create(@RequestBody PostDto dto,
                       @AuthenticationPrincipal UserDetails userDetails) {
        return postService.create(dto, userDetails.getUsername());
    }

    @PutMapping("/{id}")
    public Post update(@PathVariable Long id,
                       @RequestBody PostDto dto,
                       @AuthenticationPrincipal UserDetails userDetails) {
        Post post = postService.findById(id);
        checkOwnership(post, userDetails.getUsername());
        return postService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        Post post = postService.findById(id);
        checkOwnership(post, userDetails.getUsername());
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void checkOwnership(Post post, String username) {
        if (post.getAuthor() == null || !post.getAuthor().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다.");
        }
    }
}