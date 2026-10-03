package com.demo.sloth.server;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinServerRequest(@NotBlank @Size(max = 128) String code) {
}
