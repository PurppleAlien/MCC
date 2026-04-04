Feature: Ordenes API via Gateway

  Background:
    * url baseUrl

  Scenario: Crear y confirmar orden
    # Crear orden
    Given path '/api/v1/ordenes'
    And request
      """
      {
        "numeroOrden": "ORD-KAR-001",
        "clienteId": "a1b2c3d4-0000-0000-0000-000000000099",
        "carritoId": "c1d2e3f4-0000-0000-0000-000000000001",
        "items": [
          {
            "productoId": "a1b2c3d4-0000-0000-0000-000000000001",
            "nombreProducto": "Producto Test",
            "sku": "TST-001",
            "cantidad": 1,
            "precioUnitario": { "cantidad": 100.00, "moneda": "MXN" }
          }
        ],
        "direccionEnvio": {
          "calle": "Av. Universidad",
          "numero": "3000",
          "colonia": "Copilco",
          "ciudad": "CDMX",
          "estado": "CDMX",
          "pais": "Mexico",
          "codigoPostal": "04360",
          "nombreDestinatario": "Juan Perez",
          "telefono": "5551234567"
        }
      }
      """
    When method POST
    Then status 201
    And match response.id == '#string'
    And match response.estado == 'PENDIENTE'
    * def ordenId = response.id

    # Confirmar orden
    Given path '/api/v1/ordenes/' + ordenId + '/confirmar'
    And param usuario = 'operador1'
    When method PATCH
    Then status 200
    And match response.estado == 'CONFIRMADA'

  Scenario: Listar todas las ordenes
    Given path '/api/v1/ordenes'
    When method GET
    Then status 200
    And match response == '#array'

  Scenario: Flujo completo - crear, confirmar y pagar orden
    # Crear orden
    Given path '/api/v1/ordenes'
    And request
      """
      {
        "numeroOrden": "ORD-KAR-FULL-001",
        "clienteId": "a1b2c3d4-0000-0000-0000-000000000088",
        "carritoId": "c1d2e3f4-0000-0000-0000-000000000088",
        "items": [
          {
            "productoId": "a1b2c3d4-0000-0000-0000-000000000088",
            "nombreProducto": "Producto Full Test",
            "sku": "FUL-001",
            "cantidad": 2,
            "precioUnitario": { "cantidad": 250.00, "moneda": "MXN" }
          }
        ],
        "direccionEnvio": {
          "calle": "Calle Falsa",
          "numero": "123",
          "colonia": "Centro",
          "ciudad": "Guadalajara",
          "estado": "Jalisco",
          "pais": "Mexico",
          "codigoPostal": "44100",
          "nombreDestinatario": "Maria Lopez",
          "telefono": "3331234567"
        }
      }
      """
    When method POST
    Then status 201
    * def ordenId = response.id

    # Confirmar
    Given path '/api/v1/ordenes/' + ordenId + '/confirmar'
    And param usuario = 'operador1'
    When method PATCH
    Then status 200

    # Pagar
    Given path '/api/v1/ordenes/' + ordenId + '/pago'
    And param referencia = 'REF-KARATE-001'
    When method PATCH
    Then status 200
    And match response.estado == 'PAGO_PROCESADO'
