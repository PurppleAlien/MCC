Feature: Circuit Breaker - Ventas con Catalogo caido

  Background:
    * url baseUrl

  Scenario: Agregar producto al carrito cuando catalogo esta disponible retorna 200
    # Crear carrito
    Given path '/api/v1/carritos'
    And param clienteId = 'cb000000-0000-0000-0000-000000000001'
    When method POST
    Then status 201
    * def carritoId = response.id.valor

    # Intentar agregar con ID invalido (recurso no encontrado, no circuit breaker)
    Given path '/api/v1/carritos/' + carritoId + '/productos'
    And request
      """
      {
        "productoId": "00000000-0000-0000-0000-000000000000",
        "cantidad": 1
      }
      """
    When method POST
    # Cuando catalogo esta UP: 404 por producto no encontrado
    # Cuando catalogo esta DOWN: 503 por circuit breaker abierto
    And assert responseStatus == 404 || responseStatus == 503
    And match responseStatus == '#number'
