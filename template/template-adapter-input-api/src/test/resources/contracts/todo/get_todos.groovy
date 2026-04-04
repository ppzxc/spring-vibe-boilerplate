import org.springframework.cloud.contract.spec.Contract

Contract.make {
  description "Get all todos returns list with Total-Count header"
  request {
    method GET()
    url '/todos'
  }
  response {
    status OK()
    headers {
      header('Total-Count': '2')
      contentType(applicationJson())
    }
    body([
      [id: 1L, title: "Buy milk", completed: false,
       createdAt: "2026-01-01T00:00:00",
       updatedAt: "2026-01-01T00:00:00"],
      [id: 2L, title: "Buy eggs", completed: true,
       createdAt: "2026-01-01T00:00:00",
       updatedAt: "2026-01-01T00:00:00"]
    ])
  }
}
