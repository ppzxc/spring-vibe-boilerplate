import org.springframework.cloud.contract.spec.Contract

Contract.make {
  description "Create a new todo"
  request {
    method POST()
    url '/todos'
    headers {
      contentType(applicationJson())
    }
    body([title: "Buy milk"])
  }
  response {
    status CREATED()
    headers {
      header('Location': '/todos/1')
      contentType(applicationJson())
    }
    body([
      id        : 1L,
      title     : "Buy milk",
      completed : false,
      createdAt : "2026-01-01T00:00:00",
      updatedAt : "2026-01-01T00:00:00"
    ])
  }
}
