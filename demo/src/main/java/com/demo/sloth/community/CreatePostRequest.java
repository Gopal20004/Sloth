package com.demo.sloth.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 5000) String body
) {
}
