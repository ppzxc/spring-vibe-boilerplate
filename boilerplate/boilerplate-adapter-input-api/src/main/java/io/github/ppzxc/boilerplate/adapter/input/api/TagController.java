package io.github.ppzxc.boilerplate.adapter.input.api;

import io.github.ppzxc.boilerplate.application.port.input.command.CreateTagUseCase;
import io.github.ppzxc.boilerplate.application.port.input.command.DeleteTagUseCase;
import io.github.ppzxc.boilerplate.application.port.input.query.FindTagQuery;
import io.github.ppzxc.boilerplate.domain.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Tag CRUD API")
@Validated
@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

  private final CreateTagUseCase createTagUseCase;
  private final DeleteTagUseCase deleteTagUseCase;
  private final FindTagQuery findTagQuery;

  @Operation(summary = "Create a new tag")
  @ApiResponse(responseCode = "201", description = "Tag created")
  @PostMapping
  ResponseEntity<TagResponse> create(@Valid @RequestBody CreateTagRequest request) {
    Tag tag = createTagUseCase.create(request.name());
    TagResponse response = TagResponse.from(tag);
    return ResponseEntity.created(URI.create("/tags/" + tag.getId())).body(response);
  }

  @Operation(summary = "Get tag by ID")
  @ApiResponse(responseCode = "200", description = "Tag found")
  @ApiResponse(responseCode = "404", description = "Tag not found")
  @GetMapping("/{id}")
  ResponseEntity<TagResponse> findById(@Positive @PathVariable long id) {
    Tag tag = findTagQuery.findById(id);
    return ResponseEntity.ok(TagResponse.from(tag));
  }

  @Operation(summary = "Get all tags")
  @ApiResponse(responseCode = "200", description = "Tags retrieved")
  @GetMapping
  ResponseEntity<List<TagResponse>> findAll() {
    List<Tag> tags = findTagQuery.findAll();
    List<TagResponse> responses = tags.stream().map(TagResponse::from).toList();
    return ResponseEntity.ok(responses);
  }

  @Operation(summary = "Delete a tag")
  @ApiResponse(responseCode = "204", description = "Tag deleted")
  @DeleteMapping("/{id}")
  ResponseEntity<Void> delete(@Positive @PathVariable long id) {
    deleteTagUseCase.delete(id);
    return ResponseEntity.noContent().build();
  }
}
