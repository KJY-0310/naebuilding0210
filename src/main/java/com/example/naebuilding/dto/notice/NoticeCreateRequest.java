package com.example.naebuilding.dto.notice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoticeCreateRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 120, message = "제목은 120자 이하여야 합니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String body
) {}
