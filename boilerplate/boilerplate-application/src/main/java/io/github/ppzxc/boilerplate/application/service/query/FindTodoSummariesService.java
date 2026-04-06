package io.github.ppzxc.boilerplate.application.service.query;

import io.github.ppzxc.boilerplate.application.dto.TodoSummary;
import io.github.ppzxc.boilerplate.application.port.input.query.FindTodoSummariesQuery;
import io.github.ppzxc.boilerplate.application.port.output.query.LoadTodoSummariesPort;

import java.util.List;

public class FindTodoSummariesService implements FindTodoSummariesQuery {
    private final LoadTodoSummariesPort loadTodoSummariesPort;

    public FindTodoSummariesService(LoadTodoSummariesPort loadTodoSummariesPort) {
        this.loadTodoSummariesPort = loadTodoSummariesPort;
    }

    @Override
    public List<TodoSummary> findAllSummaries() {
        return loadTodoSummariesPort.loadAllSummaries();
    }
}