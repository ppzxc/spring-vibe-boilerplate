package io.github.ppzxc.boilerplate.adapter.input.api;

import jakarta.validation.constraints.NotBlank;

public record CreateTodoRequest(@NotBlank String title) {}
