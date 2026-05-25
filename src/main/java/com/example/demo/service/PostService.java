package com.example.demo.service;

import com.example.demo.dto.PostDto;
import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Post findById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found: " + id));
    }

    public Post create(PostDto dto, String username) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        if (username != null) {
            User author = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            post.setAuthor(author);
        }
        return postRepository.save(post);
    }

    public Post update(Long id, PostDto dto) {
        Post post = findById(id);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        return postRepository.save(post);
    }

    public void delete(Long id) {
        postRepository.deleteById(id);
    }
}