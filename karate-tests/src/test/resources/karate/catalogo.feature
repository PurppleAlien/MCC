Feature: Catalogo API via Gateway

  Background:
    * url baseUrl

  Scenario: Crear categoria
    Given path '/api/v1/categorias'
    And request { nombre: 'Electronica', descripcion: 'Productos electronicos' }
    When method POST
    Then status 201
    And match response.id == '#string'
    And match response.nombre == 'Electronica'
    * def categoriaId = response.id

  Scenario: Listar categorias
    Given path '/api/v1/categorias'
    When method GET
    Then status 200
    And match response == '#array'

  Scenario: Crear producto en catalogo
    # Primero crear categoria
    Given path '/api/v1/categorias'
    And request { nombre: 'Electronica Test', descripcion: 'Test' }
    When method POST
    Then status 201
    * def catId = response.id

    # Crear producto
    Given path '/api/v1/productos'
    And request
      """
      {
        "nombre": "Laptop Pro",
        "descripcion": "Laptop de alta gama",
        "sku": "LAP-001",
        "precio": 15000.00,
        "moneda": "MXN",
        "stock": 10,
        "categoriaId": "#(catId)"
      }
      """
    When method POST
    Then status 201
    And match response.id == '#string'
    And match response.nombre == 'Laptop Pro'
    And match response.stock == 10
    * def productoId = response.id

  Scenario: Obtener producto por ID
    # Setup: crear categoria y producto
    Given path '/api/v1/categorias'
    And request { nombre: 'Cat Obtener', descripcion: 'Test' }
    When method POST
    Then status 201
    * def catId = response.id

    Given path '/api/v1/productos'
    And request
      """
      {
        "nombre": "Mouse Gamer",
        "descripcion": "Mouse de alta precision",
        "sku": "MOU-001",
        "precio": 500.00,
        "moneda": "MXN",
        "stock": 20,
        "categoriaId": "#(catId)"
      }
      """
    When method POST
    Then status 201
    * def prodId = response.id

    # Obtener por ID
    Given path '/api/v1/productos/' + prodId
    When method GET
    Then status 200
    And match response.id == prodId
    And match response.nombre == 'Mouse Gamer'
