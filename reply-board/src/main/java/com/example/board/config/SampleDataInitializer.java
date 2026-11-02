package com.example.board.config;

import com.example.board.domain.Comment;
import com.example.board.domain.Post;
import com.example.board.repository.CommentRepository;
import com.example.board.service.PostService;
import com.example.board.web.form.PostForm;
import java.time.LocalDateTime;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("!test")
public class SampleDataInitializer implements ApplicationRunner {

    private final PostService posts;
    private final CommentRepository comments;

    public SampleDataInitializer(PostService posts, CommentRepository comments) {
        this.posts = posts;
        this.comments = comments;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!posts.findAll().isEmpty()) {
            return;
        }
        PostForm form = new PostForm();
        form.setTitle("게시판 글 제목");
        form.setWriter("foxman12");
        form.setContent("게시판 내용\n\n댓글은 REST API로 등록되어 화면이 새로고침되지 않습니다.");
        Post post = posts.create(form);
        post.setCreatedAt(LocalDateTime.of(2025, 10, 29, 16, 25));
        post.setViewCount(0);

        PostForm reply = new PostForm();
        reply.setTitle("RE: 게시판 글 제목");
        reply.setWriter("foxman12");
        reply.setContent("답변형 게시판의 답글입니다.");
        posts.reply(post.getId(), reply);

        saveComment(post.getId(), null, "Rest Api를 이용한 댓글 추가", LocalDateTime.of(2025, 10, 29, 16, 26));
        saveComment(post.getId(), null, "하나디 댓글 추가", LocalDateTime.of(2025, 10, 29, 16, 26));
    }

    private void saveComment(Long postId, Long parentId, String content, LocalDateTime createdAt) {
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setParentId(parentId);
        comment.setWriter("foxman12");
        comment.setContent(content);
        comment.setCreatedAt(createdAt);
        comments.save(comment);
    }
}
