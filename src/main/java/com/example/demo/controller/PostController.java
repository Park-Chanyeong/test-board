package com.example.demo.controller;

import com.example.demo.dto.PostDto;
import com.example.demo.entity.Post;
import com.example.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public Post create(@RequestBody PostDto dto) {
        return postService.create(dto);
    }

    @PutMapping("/{id}")
    public Post update(@PathVariable Long id, @RequestBody PostDto dto) {
        return postService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }
}