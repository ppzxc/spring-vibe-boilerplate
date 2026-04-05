package io.github.ppzxc.boilerplate.application.port.input.query;

import io.github.ppzxc.boilerplate.domain.Tag;
import java.util.List;

public interface FindTagQuery {

  Tag findById(long id);

  List<Tag> findAll();
}
