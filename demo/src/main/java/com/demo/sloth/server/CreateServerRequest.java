package com.demo.sloth.server;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateServerRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 1000) String description
) {
}
