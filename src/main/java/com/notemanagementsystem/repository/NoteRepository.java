package com.notemanagementsystem.repository;

import com.notemanagementsystem.model.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
    Page<Note> findByTitleContainingOrContentContaining(
            String title,
            String content,
            Pageable pageable
    );
}
