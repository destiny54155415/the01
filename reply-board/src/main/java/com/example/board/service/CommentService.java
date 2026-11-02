package com.example.board.service;

import com.example.board.domain.Comment;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.PostRepository;
import com.example.board.web.dto.CommentRequest;
import com.example.board.web.dto.CommentResponse;
import com.example.board.web.dto.CommentUpdateRequest;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CommentService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm");

    private final CommentRepository comments;
    private final PostRepository posts;

    public CommentService(CommentRepository comments, PostRepository posts) {
        this.comments = comments;
        this.posts = posts;
    }

    @Transactional(readOnly = true)
    public long count(Long postId) {
        return comments.countByPostId(postId);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> list(Long postId, String order) {
        requirePost(postId);
        List<Comment> all = comments.findByPostIdOrderByCreatedAtAscIdAsc(postId);
        Map<Long, CommentResponse> nodes = new HashMap<>();
        for (Comment comment : all) {
            nodes.put(comment.getId(), toResponse(comment));
        }

        List<CommentResponse> roots = new ArrayList<>();
        for (Comment comment : all) {
            CommentResponse node = nodes.get(comment.getId());
            if (comment.getParentId() == null || !nodes.containsKey(comment.getParentId())) {
                roots.add(node);
                continue;
            }
            nodes.get(comment.getParentId()).getReplies().add(node);
        }

        Comparator<CommentResponse> comparator = Comparator.comparing(CommentResponse::getCreatedAt)
                .thenComparing(CommentResponse::getId);
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        roots.sort(comparator);
        return roots;
    }

    public CommentResponse create(Long postId, CommentRequest request) {
        requirePost(postId);
        if (request.getParentId() != null) {
            Comment parent = comments.findById(request.getParentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "답글을 달 댓글이 없습니다."));
            if (!parent.getPostId().equals(postId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이 글의 댓글이 아닙니다.");
            }
        }
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setParentId(request.getParentId());
        comment.setWriter(request.getWriter().trim());
        comment.setContent(request.getContent().trim());
        return toResponse(comments.save(comment));
    }

    public CommentResponse update(Long id, CommentUpdateRequest request) {
        Comment comment = get(id);
        comment.setContent(request.getContent().trim());
        return toResponse(comment);
    }

    public void delete(Long id) {
        Comment comment = get(id);
        List<Comment> all = comments.findByPostIdOrderByCreatedAtAscIdAsc(comment.getPostId());
        Set<Long> ids = new HashSet<>();
        collect(comment.getId(), all, ids);
        comments.deleteAllById(ids);
    }

    private void collect(Long id, List<Comment> all, Set<Long> ids) {
        ids.add(id);
        for (Comment comment : all) {
            if (id.equals(comment.getParentId())) {
                collect(comment.getId(), all, ids);
            }
        }
    }

    private Comment get(Long id) {
        return comments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."));
    }

    private void requirePost(Long postId) {
        if (!posts.existsById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "글을 찾을 수 없습니다.");
        }
    }

    private CommentResponse toResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setParentId(comment.getParentId());
        response.setWriter(comment.getWriter());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt().format(FORMATTER));
        return response;
    }
}
