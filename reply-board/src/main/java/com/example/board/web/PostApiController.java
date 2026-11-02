package com.example.board.web;

import com.example.board.service.PostService;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostApiController {

    private final PostService posts;

    public PostApiController(PostService posts) {
        this.posts = posts;
    }

    @PostMapping("/api/posts/{id}/like")
    public Map<String, Integer> like(@PathVariable Long id) {
        return Map.of("likeCount", posts.like(id));
    }
}
