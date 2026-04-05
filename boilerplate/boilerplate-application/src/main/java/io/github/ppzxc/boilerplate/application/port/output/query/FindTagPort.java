package io.github.ppzxc.boilerplate.application.port.output.query;

import io.github.ppzxc.boilerplate.domain.Tag;
import java.util.List;
import java.util.Optional;

public interface FindTagPort {

  Optional<Tag> findById(long id);

  List<Tag> findAll();
}
