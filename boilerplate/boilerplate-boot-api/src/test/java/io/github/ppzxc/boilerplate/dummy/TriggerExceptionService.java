package io.github.ppzxc.boilerplate.dummy;

import org.springframework.stereotype.Service;

public class TriggerExceptionService implements TriggerExceptionUseCase {
    private final SaveDummyPort saveDummyPort;

    public TriggerExceptionService(SaveDummyPort saveDummyPort) {
        this.saveDummyPort = saveDummyPort;
    }

    @Override
    public void executeWithException() {
        saveDummyPort.save(new DummyDomain(1L, "First Insert"));
        throw new RuntimeException("Intentional rollback exception");
    }
}