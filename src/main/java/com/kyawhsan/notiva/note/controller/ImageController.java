package com.kyawhsan.notiva.note.controller;

import com.kyawhsan.notiva.common.response.ApiResponse;
import com.kyawhsan.notiva.note.dto.DeleteImageRequest;
import com.kyawhsan.notiva.dto.ImageUploadResponse;
import com.kyawhsan.notiva.note.service.NoteImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final NoteImageService noteImageService;

    @PostMapping(value = "/notes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadNoteImage(
            @RequestPart("file")
            MultipartFile file) {
        ImageUploadResponse response = noteImageService.uploadImage(file);

        return ResponseEntity.ok(ApiResponse.success("Note image uploaded successfully", response));
    }

    @DeleteMapping("/notes")
    public ResponseEntity<ApiResponse<Void>> deleteNoteImage(
            @Valid
            @RequestBody
            DeleteImageRequest request) {
        noteImageService.deleteImage(request.publicId());

        return ResponseEntity.ok(ApiResponse.success("Note image deleted successfully"));
    }
}
