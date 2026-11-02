package com.example.board.service;

import com.example.board.domain.Post;
import com.example.board.repository.CommentRepository;
import com.example.board.repository.PostRepository;
import com.example.board.web.form.PostForm;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class PostService {

    private final PostRepository posts;
    private final CommentRepository comments;

    public PostService(PostRepository posts, CommentRepository comments) {
        this.posts = posts;
        this.comments = comments;
    }

    @Transactional(readOnly = true)
    public List<Post> findAll() {
        return posts.findAllByOrderByGroupNoDescGroupOrderAsc();
    }

    @Transactional(readOnly = true)
    public Post get(Long id) {
        return posts.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "글을 찾을 수 없습니다."));
    }

    public Post view(Long id) {
        Post post = get(id);
        post.setViewCount(post.getViewCount() + 1);
        return post;
    }

    public int like(Long id) {
        Post post = get(id);
        post.setLikeCount(post.getLikeCount() + 1);
        return post.getLikeCount();
    }

    public Post create(PostForm form) {
        Post post = new Post();
        apply(post, form);
        post.setGroupOrder(0);
        post.setDepth(0);
        Post saved = posts.saveAndFlush(post);
        saved.setGroupNo(saved.getId());
        return saved;
    }

    public Post reply(Long parentId, PostForm form) {
        Post parent = get(parentId);
        posts.shiftOrders(parent.getGroupNo(), parent.getGroupOrder());
        Post post = new Post();
        apply(post, form);
        post.setGroupNo(parent.getGroupNo());
        post.setGroupOrder(parent.getGroupOrder() + 1);
        post.setDepth(parent.getDepth() + 1);
        post.setParentId(parent.getId());
        return posts.save(post);
    }

    public void update(Long id, PostForm form) {
        Post post = get(id);
        apply(post, form);
    }

    public void delete(Long id) {
        if (!posts.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "글을 찾을 수 없습니다.");
        }
        comments.deleteByPostId(id);
        posts.deleteById(id);
    }

    private void apply(Post post, PostForm form) {
        post.setTitle(form.getTitle().trim());
        post.setWriter(form.getWriter().trim());
        post.setContent(form.getContent().trim());
    }
}
