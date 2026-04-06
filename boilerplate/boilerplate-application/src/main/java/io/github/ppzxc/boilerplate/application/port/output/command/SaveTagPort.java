package io.github.ppzxc.boilerplate.application.port.output.command;

import io.github.ppzxc.boilerplate.domain.Tag;

public interface SaveTagPort {

  Tag save(Tag tag);
}
