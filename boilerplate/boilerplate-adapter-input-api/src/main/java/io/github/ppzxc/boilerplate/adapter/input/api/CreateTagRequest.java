package io.github.ppzxc.boilerplate.adapter.input.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(@NotBlank @Size(max = 50) String name) {}
