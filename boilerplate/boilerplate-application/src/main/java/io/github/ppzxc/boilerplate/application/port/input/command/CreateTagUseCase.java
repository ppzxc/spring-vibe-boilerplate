package io.github.ppzxc.boilerplate.application.port.input.command;

import io.github.ppzxc.boilerplate.domain.Tag;

public interface CreateTagUseCase {

  Tag create(String name);
}
