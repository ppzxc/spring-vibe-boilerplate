package io.github.ppzxc.template.adapter.input.api;

import org.jspecify.annotations.Nullable;

public record UpdateTodoRequest(@Nullable String title, @Nullable Boolean completed) {}
