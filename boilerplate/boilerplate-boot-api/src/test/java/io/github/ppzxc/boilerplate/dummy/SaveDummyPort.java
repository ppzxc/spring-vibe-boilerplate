package io.github.ppzxc.boilerplate.dummy;

public interface SaveDummyPort {
  void save(DummyDomain domain);

  int count();

  void clear();
}
