package io.github.ppzxc.boilerplate.domain;

/** Todo가 생성되었을 때 발행되는 도메인 이벤트. */
public record TodoCreatedEvent(long todoId, String title) {}
