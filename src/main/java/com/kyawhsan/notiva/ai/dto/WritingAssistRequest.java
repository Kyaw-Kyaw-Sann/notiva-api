package com.kyawhsan.notiva.ai.dto;

import com.kyawhsan.notiva.enums.WritingAction;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WritingAssistRequest(

        @NotNull(message = "Writing action is required")
        WritingAction action,

        @Size(max = 5000, message = "Selected text must not exceed 5000 characters")
        String selectedText,

        @Size(max = 1000, message = "Instruction must not exceed 1000 characters")
        String instruction

) {
}
