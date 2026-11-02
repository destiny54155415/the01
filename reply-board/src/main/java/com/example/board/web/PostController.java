package com.example.board.web;

import com.example.board.domain.Post;
import com.example.board.service.CommentService;
import com.example.board.service.PostService;
import com.example.board.web.form.PostForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PostController {

    private final PostService posts;
    private final CommentService comments;

    public PostController(PostService posts, CommentService comments) {
        this.posts = posts;
        this.comments = comments;
    }

    @GetMapping("/")
    public String list(Model model) {
        model.addAttribute("posts", posts.findAll());
        return "posts/list";
    }

    @GetMapping("/posts/new")
    public String createForm(Model model) {
        model.addAttribute("postForm", new PostForm());
        model.addAttribute("mode", "new");
        return "posts/form";
    }

    @PostMapping("/posts")
    public String create(@Valid @ModelAttribute("postForm") PostForm form,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "new");
            return "posts/form";
        }
        Post saved = posts.create(form);
        return "redirect:/posts/" + saved.getId();
    }

    @GetMapping("/posts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Post post = posts.view(id);
        model.addAttribute("post", post);
        model.addAttribute("commentCount", comments.count(id));
        return "posts/detail";
    }

    @GetMapping("/posts/{id}/reply")
    public String replyForm(@PathVariable Long id, Model model) {
        Post parent = posts.get(id);
        PostForm form = new PostForm();
        form.setTitle("RE: " + parent.getTitle());
        form.setWriter(parent.getWriter());
        model.addAttribute("postForm", form);
        model.addAttribute("mode", "reply");
        model.addAttribute("parent", parent);
        return "posts/form";
    }

    @PostMapping("/posts/{id}/reply")
    public String reply(@PathVariable Long id,
                        @Valid @ModelAttribute("postForm") PostForm form,
                        BindingResult bindingResult,
                        Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "reply");
            model.addAttribute("parent", posts.get(id));
            return "posts/form";
        }
        Post saved = posts.reply(id, form);
        return "redirect:/posts/" + saved.getId();
    }

    @GetMapping("/posts/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Post post = posts.get(id);
        PostForm form = new PostForm();
        form.setTitle(post.getTitle());
        form.setWriter(post.getWriter());
        form.setContent(post.getContent());
        model.addAttribute("postForm", form);
        model.addAttribute("mode", "edit");
        model.addAttribute("postId", id);
        return "posts/form";
    }

    @PostMapping("/posts/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("postForm") PostForm form,
                       BindingResult bindingResult,
                       Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mode", "edit");
            model.addAttribute("postId", id);
            return "posts/form";
        }
        posts.update(id, form);
        return "redirect:/posts/" + id;
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        posts.delete(id);
        redirectAttributes.addFlashAttribute("message", "글을 삭제했습니다.");
        return "redirect:/";
    }
}
