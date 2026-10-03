package com.demo.sloth.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateReplyRequest(@NotBlank @Size(max = 2000) String body) {
}
