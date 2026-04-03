package io.github.ppzxc.template.adapter.input.api;

import jakarta.validation.constraints.NotBlank;

public record CreateTodoRequest(@NotBlank String title) {}
