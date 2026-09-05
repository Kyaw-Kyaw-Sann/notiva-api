package com.kyawhsan.notiva.ai.dto;

import com.kyawhsan.notiva.ai.enums.WritingAction;

public record WritingAssistResponse(

        WritingAction action,

        String content,

        String model,

        AiUsageResponse usage

) {
}
