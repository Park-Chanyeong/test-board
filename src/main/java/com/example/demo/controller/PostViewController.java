package com.example.demo.controller;

import com.example.demo.dto.PostDto;
import com.example.demo.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/web/posts")
@RequiredArgsConstructor
public class PostViewController {

    private final PostService postService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("posts", postService.findAll());
        return "posts/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("post", postService.findById(id));
        return "posts/detail";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("dto", new PostDto());
        return "posts/form";
    }

    @PostMapping
    public String create(@ModelAttribute PostDto dto, Authentication authentication) {
        String username = authentication != null ? authentication.getName() : null;
        postService.create(dto, username);
        return "redirect:/web/posts";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("post", postService.findById(id));
        return "posts/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute PostDto dto) {
        postService.update(id, dto);
        return "redirect:/web/posts/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        postService.delete(id);
        return "redirect:/web/posts";
    }
}