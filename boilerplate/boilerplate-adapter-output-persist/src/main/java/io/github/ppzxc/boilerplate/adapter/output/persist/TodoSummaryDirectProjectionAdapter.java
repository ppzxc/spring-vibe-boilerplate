package io.github.ppzxc.boilerplate.adapter.output.persist;

import io.github.ppzxc.boilerplate.application.dto.TodoSummary;
import io.github.ppzxc.boilerplate.application.port.output.query.LoadTodoSummariesPort;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Component
public class TodoSummaryDirectProjectionAdapter implements LoadTodoSummariesPort {

    private final DSLContext dsl;

    public TodoSummaryDirectProjectionAdapter(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Override
    public List<TodoSummary> loadAllSummaries() {
        return dsl.select(field("id", Long.class), field("title", String.class))
                .from(table("todo"))
                .fetchInto(TodoSummary.class);
    }
}