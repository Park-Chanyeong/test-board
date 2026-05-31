package com.example.demo.dto;

import com.example.demo.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostDetailDto {

    private final Long id;
    private final String title;
    private final String content;
    private final String authorUsername;
    private final LocalDateTime createdAt;
    private final int viewCount;

    private PostDetailDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.authorUsername = post.getAuthorUsername();
        this.createdAt = post.getCreatedAt();
        this.viewCount = post.getViewCount();
    }

    public static PostDetailDto from(Post post) {
        return new PostDetailDto(post);
    }
}