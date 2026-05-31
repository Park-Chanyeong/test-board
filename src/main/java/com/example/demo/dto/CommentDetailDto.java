package com.example.demo.dto;

import com.example.demo.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentDetailDto {

    private final Long id;
    private final String content;
    private final String authorUsername;
    private final LocalDateTime createdAt;

    private CommentDetailDto(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.authorUsername = comment.getAuthorUsername();
        this.createdAt = comment.getCreatedAt();
    }

    public static CommentDetailDto from(Comment comment) {
        return new CommentDetailDto(comment);
    }
}