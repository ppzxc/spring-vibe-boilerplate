package io.github.ppzxc.template.adapter.input.api;

import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

public record UpdateTodoRequest(
    @Nullable @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
        String title,
    @Nullable Boolean completed) {}
