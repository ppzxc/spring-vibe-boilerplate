package io.github.ppzxc.template.adapter.input.api;

import io.github.ppzxc.template.application.port.input.command.CreateTodoUseCase;
import io.github.ppzxc.template.application.port.input.command.DeleteTodoUseCase;
import io.github.ppzxc.template.application.port.input.command.UpdateTodoUseCase;
import io.github.ppzxc.template.application.port.input.query.FindTodoQuery;
import io.github.ppzxc.template.domain.Todo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Todos", description = "Todo CRUD API")
@Validated
@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
public class TodoController {

  private final CreateTodoUseCase createTodoUseCase;
  private final UpdateTodoUseCase updateTodoUseCase;
  private final DeleteTodoUseCase deleteTodoUseCase;
  private final FindTodoQuery findTodoQuery;

  @Operation(summary = "Create a new todo")
  @ApiResponse(responseCode = "201", description = "Todo created")
  @PostMapping
  ResponseEntity<TodoResponse> create(@Valid @RequestBody CreateTodoRequest request) {
    Todo todo = createTodoUseCase.create(request.title());
    TodoResponse response = TodoResponse.from(todo);
    return ResponseEntity.created(URI.create("/todos/" + todo.getId())).body(response);
  }

  @Operation(summary = "Get todo by ID")
  @ApiResponse(responseCode = "200", description = "Todo found")
  @ApiResponse(responseCode = "404", description = "Todo not found")
  @GetMapping("/{id}")
  ResponseEntity<TodoResponse> findById(@Positive @PathVariable long id) {
    Todo todo = findTodoQuery.findById(id);
    return ResponseEntity.ok(TodoResponse.from(todo));
  }

  @Operation(summary = "Get all todos")
  @ApiResponse(responseCode = "200", description = "Todos retrieved")
  @GetMapping
  ResponseEntity<List<TodoResponse>> findAll() {
    List<Todo> todos = findTodoQuery.findAll();
    List<TodoResponse> responses = todos.stream().map(TodoResponse::from).toList();
    HttpHeaders headers = new HttpHeaders();
    headers.set("Total-Count", String.valueOf(responses.size()));
    return ResponseEntity.ok().headers(headers).body(responses);
  }

  @Operation(summary = "Update a todo")
  @ApiResponse(responseCode = "200", description = "Todo updated")
  @ApiResponse(responseCode = "404", description = "Todo not found")
  @PatchMapping("/{id}")
  ResponseEntity<TodoResponse> update(
      @Positive @PathVariable long id, @Valid @RequestBody UpdateTodoRequest request) {
    Todo todo = updateTodoUseCase.update(id, request.title(), request.completed());
    return ResponseEntity.ok(TodoResponse.from(todo));
  }

  @Operation(summary = "Delete a todo")
  @ApiResponse(responseCode = "204", description = "Todo deleted")
  @DeleteMapping("/{id}")
  ResponseEntity<Void> delete(@Positive @PathVariable long id) {
    deleteTodoUseCase.delete(id);
    return ResponseEntity.noContent().build();
  }
}
