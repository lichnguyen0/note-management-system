package com.notemanagementsystem.service;

import com.notemanagementsystem.model.Note;
import com.notemanagementsystem.model.User;
import com.notemanagementsystem.repository.NoteRepository;
import com.notemanagementsystem.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;


    public NoteService(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    public Page<Note> getNotes(String keyword, Pageable pageable) {

        if (keyword != null && !keyword.trim().isEmpty()) {
            return noteRepository.findByTitleContainingOrContentContaining(
                    keyword, keyword, pageable
            );
        }

        return noteRepository.findAll(pageable);
    }

    public Note findById(Long id) {
        return noteRepository.findById(id).orElse(null);
    }


    public void delete(Long id) {
        noteRepository.deleteById(id);
    }

    // CHECK QUYỀN (QUAN TRỌNG)
    public boolean canEdit(Note note, User currentUser) {

        if (currentUser.getRole().equals("ROLE_ADMIN")) {
            return true;
        }

        return note.getUser().getId().equals(currentUser.getId());
    }

    public void save(Note note) {

        // ✅ VALIDATE
        if (note.getTitle() == null || note.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Title không được để trống");
        }

        if (note.getContent() == null || note.getContent().trim().isEmpty()) {
            throw new RuntimeException("Content không được để trống");
        }

        // 🔐 lấy user hiện tại
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByUsername(username);

        // 🔥 gán owner
        note.setUser(user);

        // 💾 lưu DB
        noteRepository.save(note);
    }



}
