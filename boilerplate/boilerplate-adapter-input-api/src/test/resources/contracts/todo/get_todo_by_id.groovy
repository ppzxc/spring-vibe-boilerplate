import org.springframework.cloud.contract.spec.Contract

Contract.make {
  description "Get a single todo by ID"
  request {
    method GET()
    url '/todos/1'
  }
  response {
    status OK()
    headers {
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
