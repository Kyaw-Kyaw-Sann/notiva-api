package com.kyawhsan.notiva.note.controller;

import com.kyawhsan.notiva.common.response.ApiResponse;
import com.kyawhsan.notiva.note.dto.CreateNoteRequest;
import com.kyawhsan.notiva.note.dto.NoteResponse;
import com.kyawhsan.notiva.note.dto.UpdateNoteRequest;
import com.kyawhsan.notiva.note.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.kyawhsan.notiva.note.dto.NoteSearchRequest;
import com.kyawhsan.notiva.note.dto.NoteVersionResponse;
import com.kyawhsan.notiva.note.dto.NoteVersionSummaryResponse;
import com.kyawhsan.notiva.common.response.PagedResponse;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<ApiResponse<NoteResponse>> createNote(
            @Valid
            @RequestBody
            CreateNoteRequest request) {
        NoteResponse response = noteService.createNote(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Note created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NoteResponse>>> getNotes() {
        List<NoteResponse> response = noteService.getNotes();

        return ResponseEntity.ok(ApiResponse.success("Notes retrieved successfully", response));
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<ApiResponse<NoteResponse>> getNote(
            @PathVariable
            Long noteId) {
        NoteResponse response = noteService.getNote(noteId);

        return ResponseEntity.ok(ApiResponse.success("Note retrieved successfully", response));
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<ApiResponse<NoteResponse>> updateNote(
            @PathVariable
            Long noteId,
            @Valid
            @RequestBody
            UpdateNoteRequest request) {
        NoteResponse response = noteService.updateNote(noteId, request);

        return ResponseEntity.ok(ApiResponse.success("Note updated successfully", response));
    }

    @PatchMapping("/{noteId}/pin")
    public ResponseEntity<ApiResponse<NoteResponse>> pinNote(
            @PathVariable
            Long noteId) {
        NoteResponse response = noteService.pinNote(noteId);

        return ResponseEntity.ok(ApiResponse.success("Note pinned successfully", response));
    }

    @PatchMapping("/{noteId}/unpin")
    public ResponseEntity<ApiResponse<NoteResponse>> unpinNote(
            @PathVariable
            Long noteId) {
        NoteResponse response = noteService.unpinNote(noteId);

        return ResponseEntity.ok(ApiResponse.success("Note unpinned successfully", response));
    }

    @PatchMapping("/{noteId}/favorite")
    public ResponseEntity<ApiResponse<NoteResponse>> favoriteNote(
            @PathVariable
            Long noteId) {
        NoteResponse response = noteService.favoriteNote(noteId);

        return ResponseEntity.ok(ApiResponse.success("Note added to favorites", response));
    }

    @PatchMapping("/{noteId}/unfavorite")
    public ResponseEntity<ApiResponse<NoteResponse>> unfavoriteNote(
            @PathVariable
            Long noteId) {
        NoteResponse response = noteService.unfavoriteNote(noteId);

        return ResponseEntity.ok(ApiResponse.success("Note removed from favorites", response));
    }

    @GetMapping("/pinned")
    public ResponseEntity<ApiResponse<List<NoteResponse>>> getPinnedNotes() {
        List<NoteResponse> response = noteService.getPinnedNotes();

        return ResponseEntity
                .ok(ApiResponse.success("Pinned notes retrieved successfully", response));
    }

    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<NoteResponse>>> getFavoriteNotes() {
        List<NoteResponse> response = noteService.getFavoriteNotes();

        return ResponseEntity
                .ok(ApiResponse.success("Favorite notes retrieved successfully", response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<NoteResponse>>> searchNotes(
            @Valid
            @ModelAttribute
            NoteSearchRequest request) {
        PagedResponse<NoteResponse> response = noteService.searchNotes(request);

        return ResponseEntity.ok(ApiResponse.success("Notes searched successfully", response));
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<ApiResponse<Void>> moveNoteToTrash(
            @PathVariable
            Long noteId) {
        noteService.moveNoteToTrash(noteId);

        return ResponseEntity
                .ok(ApiResponse.success("Note moved to recycle bin successfully", null));
    }

    @GetMapping("/trash")
    public ResponseEntity<ApiResponse<List<NoteResponse>>> getTrashedNotes() {
        List<NoteResponse> response = noteService.getTrashedNotes();

        return ResponseEntity
                .ok(ApiResponse.success("Recycle bin notes retrieved successfully", response));
    }

    @GetMapping("/trash/{noteId}")
    public ResponseEntity<ApiResponse<NoteResponse>> getTrashedNote(
            @PathVariable
            Long noteId) {
        NoteResponse response = noteService.getTrashedNote(noteId);

        return ResponseEntity
                .ok(ApiResponse.success("Deleted note retrieved successfully", response));
    }

    @PatchMapping("/{noteId}/restore")
    public ResponseEntity<ApiResponse<NoteResponse>> restoreNote(
            @PathVariable
            Long noteId) {
        NoteResponse response = noteService.restoreNote(noteId);

        return ResponseEntity.ok(ApiResponse.success("Note restored successfully", response));
    }

    @DeleteMapping("/{noteId}/permanent")
    public ResponseEntity<ApiResponse<Void>> permanentlyDeleteNote(
            @PathVariable
            Long noteId) {
        noteService.permanentlyDeleteNote(noteId);

        return ResponseEntity
                .ok(ApiResponse.success("Note permanently deleted successfully", null));
    }

    @DeleteMapping("/trash")
    public ResponseEntity<ApiResponse<Integer>> emptyRecycleBin() {
        int deletedCount = noteService.emptyRecycleBin();

        return ResponseEntity
                .ok(ApiResponse.success("Recycle bin emptied successfully", deletedCount));
    }

    @GetMapping("/{noteId}/versions")
    public ResponseEntity<ApiResponse<List<NoteVersionSummaryResponse>>> getNoteVersions(
            @PathVariable
            Long noteId) {
        List<NoteVersionSummaryResponse> response = noteService.getNoteVersions(noteId);

        return ResponseEntity
                .ok(ApiResponse.success("Note versions retrieved successfully", response));
    }

    @GetMapping("/{noteId}/versions/{versionId}")
    public ResponseEntity<ApiResponse<NoteVersionResponse>> getNoteVersion(
            @PathVariable
            Long noteId,
            @PathVariable
            Long versionId) {
        NoteVersionResponse response = noteService.getNoteVersion(noteId, versionId);

        return ResponseEntity
                .ok(ApiResponse.success("Note version retrieved successfully", response));
    }

    @PostMapping("/{noteId}/versions/{versionId}/restore")
    public ResponseEntity<ApiResponse<NoteResponse>> restoreNoteVersion(
            @PathVariable
            Long noteId,
            @PathVariable
            Long versionId) {
        NoteResponse response = noteService.restoreNoteVersion(noteId, versionId);

        return ResponseEntity
                .ok(ApiResponse.success("Note version restored successfully", response));
    }
}