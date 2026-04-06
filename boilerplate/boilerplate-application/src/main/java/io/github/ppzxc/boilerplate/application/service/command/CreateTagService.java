package io.github.ppzxc.boilerplate.application.service.command;

import io.github.ppzxc.boilerplate.application.port.input.command.CreateTagUseCase;
import io.github.ppzxc.boilerplate.application.port.output.command.SaveTagPort;
import io.github.ppzxc.boilerplate.domain.Tag;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateTagService implements CreateTagUseCase {

  private final SaveTagPort saveTagPort;

  @Override
  public Tag create(String name) {
    Tag tag = Tag.create(name);
    return saveTagPort.save(tag);
  }
}
