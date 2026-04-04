Feature: Carrito API via Gateway

  Background:
    * url baseUrl

  Scenario: Crear carrito y agregar producto
    # Setup: crear categoria
    Given path '/api/v1/categorias'
    And request { nombre: 'Cat Carrito', descripcion: 'Test' }
    When method POST
    Then status 201
    * def catId = response.id

    # Setup: crear producto con stock
    Given path '/api/v1/productos'
    And request
      """
      {
        "nombre": "Teclado Mecanico",
        "descripcion": "Teclado RGB",
        "sku": "TEC-001",
        "precio": 1200.00,
        "moneda": "MXN",
        "stock": 15,
        "categoriaId": "#(catId)"
      }
      """
    When method POST
    Then status 201
    * def productoId = response.id

    # Crear carrito
    Given path '/api/v1/carritos'
    And param clienteId = 'a1b2c3d4-0000-0000-0000-000000000001'
    When method POST
    Then status 201
    And match response.id.valor == '#string'
    And match response.estado == 'ACTIVO'
    * def carritoId = response.id.valor

    # Agregar producto al carrito
    Given path '/api/v1/carritos/' + carritoId + '/productos'
    And request
      """
      {
        "productoId": "#(productoId)",
        "cantidad": 2
      }
      """
    When method POST
    Then status 200
    And match response.items == '#[1]'
    And match response.items[0].cantidad == 2

  Scenario: Crear carrito vacio
    Given path '/api/v1/carritos'
    And param clienteId = 'a1b2c3d4-0000-0000-0000-000000000002'
    When method POST
    Then status 201
    And match response.estado == 'ACTIVO'
    And match response.items == '#[0]'
