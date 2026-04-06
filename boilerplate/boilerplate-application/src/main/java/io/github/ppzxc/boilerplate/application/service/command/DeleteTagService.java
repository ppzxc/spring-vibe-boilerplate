package io.github.ppzxc.boilerplate.application.service.command;

import io.github.ppzxc.boilerplate.application.port.input.command.DeleteTagUseCase;
import io.github.ppzxc.boilerplate.application.port.output.command.DeleteTagPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteTagService implements DeleteTagUseCase {

  private final DeleteTagPort deleteTagPort;

  @Override
  public void delete(long id) {
    deleteTagPort.deleteById(id);
  }
}
