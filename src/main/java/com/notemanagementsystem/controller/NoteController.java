package com.notemanagementsystem.controller;

import com.notemanagementsystem.dto.NoteForm;
import com.notemanagementsystem.model.Note;
import com.notemanagementsystem.model.User;
import com.notemanagementsystem.repository.UserRepository;
import com.notemanagementsystem.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/notes")
public class NoteController {

    private final NoteService noteService;

    private final UserRepository userRepository;

    public NoteController(NoteService noteService, UserRepository userRepository) {
        this.noteService = noteService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 5);

        Page<Note> notePage = noteService.getNotes(keyword, pageable);

        model.addAttribute("notes", notePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", notePage.getTotalPages());
        model.addAttribute("keyword", keyword);

        return "note/list";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {

        Note note = noteService.findById(id);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);

        if (!noteService.canEdit(note, user)) {
            return "error/403";
        }

        noteService.delete(id);

        return "redirect:/notes";
    }

    private User getCurrentUser() {
        // giả lập, sau sẽ lấy từ Security
        return new User();
    }


    @GetMapping("/create")
    public String showCreateForm(Model model) {

        model.addAttribute("noteForm", new NoteForm());

        return "note/create";
    }



    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {

        Note note = noteService.findById(id);

        NoteForm form = new NoteForm();
        form.setTitle(note.getTitle());
        form.setContent(note.getContent());

        model.addAttribute("noteForm", form);
        model.addAttribute("noteId", id);

        return "note/edit";
    }


    @PostMapping("/edit/{id}")
    public String updateNote(@PathVariable Long id,
                             @ModelAttribute("noteForm") NoteForm form,
                             Model model) {

        Note note = noteService.findById(id);

        if (note == null) {
            return "error/404";
        }

        // validate
        if (form.getTitle() == null || form.getTitle().trim().isEmpty()) {
            model.addAttribute("error", "Title không được để trống");
            return "note/edit";
        }

        if (form.getContent() == null || form.getContent().trim().isEmpty()) {
            model.addAttribute("error", "Content không được để trống");
            return "note/edit";
        }

        // update
        note.setTitle(form.getTitle());
        note.setContent(form.getContent());
        note.setUpdatedAt(LocalDateTime.now());

        noteService.save(note);

        return "redirect:/notes";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("noteForm") NoteForm form,
                         BindingResult result,
                         Model model) {

        if (result.hasErrors()) {
            return "note/create";
        }

        Note note = new Note();
        note.setTitle(form.getTitle());
        note.setContent(form.getContent());
        note.setCreatedAt(LocalDateTime.now());

        noteService.save(note);

        return "redirect:/notes";
    }
}

