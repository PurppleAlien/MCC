# Documentación Técnica — UAMIShop

## Índice

1. [Descripción del sistema](#1-descripción-del-sistema)
2. [Arquitectura de microservicios](#2-arquitectura-de-microservicios)
3. [Descripción de cada servicio](#3-descripción-de-cada-servicio)
4. [Base de datos](#4-base-de-datos)
5. [Mensajería con RabbitMQ](#5-mensajería-con-rabbitmq)
6. [API REST por servicio](#6-api-rest-por-servicio)
7. [Frontend](#7-frontend)
8. [Configuración Docker](#8-configuración-docker)
9. [Variables de entorno](#9-variables-de-entorno)
10. [Datos de ejemplo (seed)](#10-datos-de-ejemplo-seed)
11. [Problemas encontrados y soluciones](#11-problemas-encontrados-y-soluciones)

---

## 1. Descripción del sistema

UAMIShop es una plataforma de comercio electrónico implementada con **arquitectura de microservicios**. Cada microservicio es independiente, tiene su propia responsabilidad de dominio y se comunica con los demás a través de:

- **HTTP síncrono** vía API Gateway (para operaciones del frontend)
- **Mensajería asíncrona** vía RabbitMQ (para eventos entre servicios backend)

El sistema implementa los siguientes flujos principales:

```
Cliente → Ver catálogo → Agregar al carrito → Crear orden → Confirmar → Pagar → Enviar a proceso
```

---

## 2. Arquitectura de microservicios

### Diagrama de componentes

```
┌───────────────────────────────────────────────────────────────────┐
│                         RED DOCKER: uamishop-app                  │
│                                                                   │
│  ┌──────────────┐   HTTP :3000   ┌──────────────────────────┐    │
│  │   Navegador  │───────────────▶│   uamishop-frontend      │    │
│  │  (usuario)   │                │   nginx:alpine            │    │
│  └──────────────┘                │   Sirve HTML/JS/CSS       │    │
│                                  └──────────────────────────┘    │
│                                              │                    │
│                                  fetch → localhost:8090           │
│                                              │                    │
│                                  ┌───────────▼──────────────┐    │
│                                  │   uamishop-gateway        │    │
│                                  │   Spring Cloud Gateway    │    │
│                                  │   Puerto: 8090            │    │
│                                  │   CORS habilitado         │    │
│                                  └───────────┬──────────────┘    │
│                     ┌─────────────────────────┼─────────────┐    │
│                     │                         │             │    │
│          ┌──────────▼──────┐   ┌──────────────▼──┐   ┌──────▼──┐│
│          │uamishop-catalogo│   │uamishop-ordenes  │   │uamishop ││
│          │Puerto: 8081     │   │Puerto: 8082      │   │-ventas  ││
│          │Productos        │   │Órdenes           │   │8083     ││
│          │Categorías       │   │Ciclo de vida     │   │Carritos ││
│          │Imágenes         │   │                  │   │         ││
│          └────────┬────────┘   └────────┬─────────┘   └───┬─────┘│
│                   │                     │                  │      │
│                   └──────────┬──────────┘──────────────────┘      │
│                              ▼                                    │
│                  ┌───────────────────────┐                        │
│                  │  MySQL 8.0 (:3306)    │                        │
│                  │  Base: uamishop       │                        │
│                  │  Usuario: uamishop    │                        │
│                  └───────────────────────┘                        │
│                                                                   │
│                  ┌───────────────────────┐                        │
│                  │  RabbitMQ (:5672)     │                        │
│                  │  Management: :15672   │                        │
│                  │  guest / guest        │                        │
│                  └───────────────────────┘                        │
└───────────────────────────────────────────────────────────────────┘
```

### Flujo de arranque con healthchecks

```
mysql        ──healthcheck──▶ (healthy)
rabbitmq     ──healthcheck──▶ (healthy)
                                  │
              ┌───────────────────┼───────────────────┐
              ▼                   ▼                   ▼
         catalogo             ordenes              ventas
         (service_started)
              │
              ▼
           gateway
              │
              ▼
           frontend
```

---

## 3. Descripción de cada servicio

### 3.1 uamishop-catalogo (puerto 8081)

**Responsabilidad:** Gestión del catálogo de productos y categorías.

**Dominio (DDD):**
- `Producto` — entidad raíz con ID embebido (`ProductoId`), precio como value object (`Money`), imágenes como colección embebida (`Imagen`)
- `Categoria` — entidad con ID embebido (`CategoriaId`), soporte para jerarquía padre-hijo
- `ProductoEstadisticas` — entidad de estadísticas de ventas (actualizada por eventos RabbitMQ)

**Tecnologías:**
- Spring Boot 3.1 + Spring Data JPA
- Spring AMQP (RabbitMQ listener)
- springdoc-openapi (Swagger UI en `/swagger-ui.html`)
- Resilience4j (circuit breaker para llamadas a servicios externos)

**Eventos que consume:**
- `producto.agregado.carrito` → incrementa contador de veces agregado al carrito
- `producto.comprado` → decrementa stock y actualiza estadísticas de ventas

---

### 3.2 uamishop-ordenes (puerto 8082)

**Responsabilidad:** Creación y gestión del ciclo de vida de las órdenes de compra.

**Estados de una orden:**
```
PENDIENTE → CONFIRMADA → PAGO_PROCESADO → EN_PROCESO → ENVIADA → ENTREGADA
                                                    ↘
                                                  CANCELADA
```

**Endpoints principales:**
- `POST /api/v1/ordenes` — crea una orden a partir de un carrito
- `PATCH /api/v1/ordenes/{id}/confirmar` — confirma la orden
- `PATCH /api/v1/ordenes/{id}/pago` — registra el pago
- `PATCH /api/v1/ordenes/{id}/en-proceso` — marca en proceso de envío

**Eventos que publica:**
- `orden.creada` → notifica a otros servicios que se creó una orden

---

### 3.3 uamishop-ventas (puerto 8083)

**Responsabilidad:** Gestión de carritos de compra.

**Comportamiento:**
- Un cliente identificado por UUID puede tener un carrito activo
- `POST /api/v1/carritos?clienteId={uuid}` — crea un carrito nuevo o recupera el activo existente
- Al agregar un producto al carrito, publica el evento `producto.agregado.carrito`
- Al crear una orden desde el carrito, el carrito pasa a estado `EN_CHECKOUT`

**Integración con Catálogo:**
- Llama al servicio `uamishop-catalogo` para verificar disponibilidad de productos
- Usa Resilience4j Circuit Breaker para tolerar fallos del catálogo

---

### 3.4 uamishop-gateway (puerto 8090)

**Responsabilidad:** Punto de entrada único (API Gateway) para todas las peticiones del frontend.

**Rutas configuradas:**

| Ruta                      | Destino                          |
|---------------------------|----------------------------------|
| `/api/v1/productos/**`    | `http://uamishop-catalogo:8081`  |
| `/api/v1/categorias/**`   | `http://uamishop-catalogo:8081`  |
| `/api/v1/ordenes/**`      | `http://uamishop-ordenes:8082`   |
| `/api/v1/carritos/**`     | `http://uamishop-ventas:8083`    |

**CORS:** Configurado globalmente para aceptar cualquier origen (`*`), necesario para que el frontend en `:3000` pueda llamar al gateway en `:8090`.

---

### 3.5 uamishop-frontend (puerto 3000)

**Responsabilidad:** Interfaz de usuario SPA (Single Page Application) servida por nginx.

**Tecnología:** HTML5 + CSS3 + JavaScript vanilla (sin frameworks). Servido por nginx:alpine.

**Funcionalidades:**
- Catálogo de productos con búsqueda en tiempo real
- Formulario de creación de productos con soporte de hasta 5 imágenes
- Carrito de compra con actualizaciones optimistas (UI se actualiza antes de confirmar el servidor)
- Panel de órdenes con barra de progreso de estados
- Toasts de notificación para feedback al usuario

---

## 4. Base de datos

### Configuración

- **Motor:** MySQL 8.0
- **Base de datos:** `uamishop` (única, compartida por todos los servicios)
- **Usuario:** `uamishop` / `uamishop`
- **Root:** `root` / `root`
- **DDL:** `spring.jpa.hibernate.ddl-auto=update` (Hibernate crea/actualiza tablas automáticamente)

### Tablas principales (generadas por Hibernate)

| Tabla                  | Servicio    | Descripción                                      |
|------------------------|-------------|--------------------------------------------------|
| `producto`             | catalogo    | Productos del catálogo                           |
| `producto_imagenes`    | catalogo    | Imágenes de productos (colección embebida)       |
| `categoria`            | catalogo    | Categorías de productos                          |
| `producto_estadisticas`| catalogo    | Estadísticas de ventas por producto              |
| `orden`                | ordenes     | Órdenes de compra                                |
| `orden_item`           | ordenes     | Ítems dentro de cada orden                       |
| `carrito`              | ventas      | Carritos de compra activos                       |
| `carrito_item`         | ventas      | Productos dentro de cada carrito                 |

### Tipos de datos relevantes

- **IDs:** `UUID` almacenado como `VARCHAR(36)` o `BINARY(16)` según la implementación JPA
- **Precios:** `DECIMAL(19,2)` vía el value object `Money {cantidad, moneda}`
- **Imágenes:** Tabla separada `producto_imagenes` con join column `producto_id`

---

## 5. Mensajería con RabbitMQ

### Configuración

- **Host:** `rabbitmq` (nombre del contenedor Docker)
- **Puerto:** `5672`
- **Credenciales:** `guest` / `guest`
- **Management UI:** `http://localhost:15672`

### Eventos publicados y consumidos

```
uamishop-ventas ──────── producto.agregado.carrito ──────▶ uamishop-catalogo
                         (ProductoAgregadoAlCarritoEvent)   (actualiza estadísticas)

uamishop-ordenes ─────── orden.creada ───────────────────▶ uamishop-catalogo
                         (OrdenCreadaEvent)                 (decrementa stock,
                                                             actualiza ventas)
```

### Estructura de eventos

**ProductoAgregadoAlCarritoEvent:**
```json
{
  "productoId": "uuid",
  "clienteId":  "uuid",
  "cantidad":   1
}
```

**OrdenCreadaEvent:**
```json
{
  "ordenId":    "uuid",
  "items": [
    { "productoId": "uuid", "cantidad": 2 }
  ]
}
```

---

## 6. API REST por servicio

### Catálogo — `/api/v1` (vía gateway `:8090` o directo `:8081`)

```
GET    /api/v1/productos                    Lista todos los productos
GET    /api/v1/productos/{id}               Obtiene un producto por UUID
POST   /api/v1/productos                    Crea un producto
PUT    /api/v1/productos/{id}               Actualiza un producto
PATCH  /api/v1/productos/{id}/activar       Activa el producto
PATCH  /api/v1/productos/{id}/desactivar    Desactiva el producto
GET    /api/v1/productos/mas-vendidos       Lista los más vendidos
GET    /api/v1/productos/{id}/estadisticas  Estadísticas de un producto

GET    /api/v1/categorias                   Lista todas las categorías
GET    /api/v1/categorias/{id}              Obtiene una categoría
POST   /api/v1/categorias                   Crea una categoría
PUT    /api/v1/categorias/{id}              Actualiza una categoría
```

**Ejemplo — Crear producto:**
```bash
curl -X POST http://localhost:8090/api/v1/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre":      "Laptop Gaming Pro",
    "sku":         "ELC-001",
    "precio":      18999.00,
    "moneda":      "MXN",
    "stock":       5,
    "descripcion": "Laptop con GPU RTX 4070",
    "categoriaId": "uuid-de-categoria",
    "imagenesUrls": ["https://ejemplo.com/imagen.jpg"]
  }'
```

### Ventas — `/api/v1/carritos`

```
POST   /api/v1/carritos?clienteId={uuid}              Crear/recuperar carrito activo
POST   /api/v1/carritos/{id}/productos                Agregar producto
DELETE /api/v1/carritos/{id}/productos/{productoId}   Quitar producto
```

### Órdenes — `/api/v1/ordenes`

```
GET    /api/v1/ordenes                          Lista todas las órdenes
POST   /api/v1/ordenes                          Crear orden desde carrito
PATCH  /api/v1/ordenes/{id}/confirmar           Confirmar orden
PATCH  /api/v1/ordenes/{id}/pago?referencia=X  Registrar pago
PATCH  /api/v1/ordenes/{id}/en-proceso          Marcar en proceso
```

**Ejemplo — Crear orden:**
```bash
curl -X POST http://localhost:8090/api/v1/ordenes \
  -H "Content-Type: application/json" \
  -d '{
    "numeroOrden": "ORD-001",
    "clienteId":   "a1b2c3d4-0000-0000-0000-000000000001",
    "carritoId":   "uuid-del-carrito",
    "items": [
      {
        "productoId":      "uuid-producto",
        "nombreProducto":  "Laptop Gaming Pro",
        "sku":             "ELC-001",
        "cantidad":        1,
        "precioUnitario":  { "cantidad": 18999.00, "moneda": "MXN" }
      }
    ],
    "direccionEnvio": {
      "nombreDestinatario": "Juan Pérez",
      "calle":              "Av. Universidad 3000",
      "ciudad":             "Ciudad de México",
      "estado":             "CDMX",
      "pais":               "México",
      "codigoPostal":       "04360",
      "telefono":           "5551234567"
    }
  }'
```

---

## 7. Frontend

### Estructura de archivos

```
uamishop-frontend/public/
├── index.html    — estructura HTML, secciones: catálogo, carrito, órdenes
├── app.js        — toda la lógica de la aplicación (vanilla JS)
└── styles.css    — design system completo con variables CSS
```

### Secciones de la aplicación

| Sección  | Funcionalidad                                                                 |
|----------|-------------------------------------------------------------------------------|
| Catálogo | Listado de productos con fotos, búsqueda por nombre/SKU, crear producto       |
| Carrito  | Carga por UUID de cliente, agregar/quitar productos, checkout                  |
| Órdenes  | Listado de órdenes con barra de progreso, confirmar, pagar, enviar a proceso  |

### Formulario de creación de productos — campos

| Campo       | Tipo      | Validación                          |
|-------------|-----------|-------------------------------------|
| Nombre      | texto     | 3–100 caracteres, obligatorio       |
| SKU         | texto     | Formato `ABC-123` (regex), único    |
| Precio MXN  | número    | > 0, obligatorio                    |
| Stock       | entero    | ≥ 0, obligatorio                    |
| Descripción | texto     | máx. 500 caracteres                 |
| Categoría   | select    | obligatorio, cargado del API        |
| Imágenes    | URL/archivo | Hasta 5, URL pública obligatoria para guardar |

### Gestión de imágenes en el formulario

- Se pueden agregar hasta **5 imágenes** por producto (límite del dominio)
- Cada fila tiene: campo URL + botón subir archivo + botón quitar
- Al ingresar una URL, se muestra **preview en tiempo real**
- Al subir un archivo local, se muestra preview pero se advierte que **no se guardará en el servidor** (el backend solo acepta URLs `http://` o `https://`)
- Al guardar o cancelar, todas las filas se limpian automáticamente

---

## 8. Configuración Docker

### Orden de arranque

Los servicios esperan que las dependencias estén listas antes de iniciar, gracias a healthchecks:

```yaml
mysql:
  healthcheck:
    test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-proot"]
    interval: 10s
    retries: 10
    start_period: 30s

rabbitmq:
  healthcheck:
    test: ["CMD", "rabbitmq-diagnostics", "ping"]
    interval: 10s
    retries: 10
    start_period: 20s
```

Los microservicios backend usan `condition: service_healthy` para esperar a MySQL y RabbitMQ.

### Dockerfiles — Multi-stage builds

Todos los microservicios usan compilación en dos etapas:

```dockerfile
# Etapa 1: Compilar con Maven
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B      # capa de caché de dependencias
COPY src ./src
RUN mvn package -DskipTests -B

# Etapa 2: Solo el JRE + el JAR
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Ventajas:**
- La imagen final no incluye Maven ni el código fuente
- La capa de dependencias se cachea (los rebuilds son más rápidos si solo cambia el código)
- El código siempre se recompila dentro de Docker (no hay JARs locales obsoletos)

---

## 9. Variables de entorno

### Variables comunes a todos los microservicios

| Variable                  | Valor en Docker              | Default local              |
|---------------------------|------------------------------|----------------------------|
| `SPRING_DATASOURCE_URL`   | `jdbc:mysql://mysql:3306/uamishop?...` | `jdbc:mysql://localhost:3306/uamishop?...` |
| `SPRING_DATASOURCE_USERNAME` | `uamishop`              | `uamishop`                 |
| `SPRING_DATASOURCE_PASSWORD` | `uamishop`              | `uamishop`                 |
| `SPRING_RABBITMQ_HOST`    | `rabbitmq`                   | `localhost`                |

### Variables del gateway

| Variable       | Valor                           |
|----------------|---------------------------------|
| `CATALOGO_URL` | `http://uamishop-catalogo:8081` |
| `ORDENES_URL`  | `http://uamishop-ordenes:8082`  |
| `VENTAS_URL`   | `http://uamishop-ventas:8083`   |

### Variable exclusiva de ventas

| Variable          | Valor                           |
|-------------------|---------------------------------|
| `CATALOGO_API_URL`| `http://uamishop-catalogo:8081` |

---

## 10. Datos de ejemplo (seed)

Al primer arranque con la base de datos vacía, `DataInitializer.java` en el servicio `uamishop-catalogo` inserta automáticamente:

### Categorías (8)

| # | Nombre       | Descripción                              |
|---|--------------|------------------------------------------|
| 1 | Electrónica  | Dispositivos electrónicos y accesorios   |
| 2 | Ropa         | Prendas de vestir y accesorios de moda   |
| 3 | Hogar        | Artículos para el hogar y decoración     |
| 4 | Deportes     | Equipamiento y ropa deportiva            |
| 5 | Libros       | Libros, revistas y material educativo    |
| 6 | Juguetes     | Juguetes y juegos para niños             |
| 7 | Alimentos    | Alimentos y bebidas                      |
| 8 | Herramientas | Herramientas y ferretería                |

### Productos de ejemplo (18)

| SKU     | Nombre                        | Precio MXN | Stock | Categoría    |
|---------|-------------------------------|------------|-------|--------------|
| ELC-001 | Laptop Gaming Pro             | $18,999    | 5     | Electrónica  |
| ELC-002 | Smartphone Ultra X            | $12,499    | 12    | Electrónica  |
| ELC-003 | Auriculares Bluetooth Pro     | $1,899     | 20    | Electrónica  |
| ELC-004 | Teclado Mecánico RGB          | $1,299     | 14    | Electrónica  |
| ROP-001 | Chamarra de Mezclilla         | $899       | 15    | Ropa         |
| ROP-002 | Playera DryFit Deportiva      | $349       | 40    | Ropa         |
| ROP-003 | Jeans Slim Fit Premium        | $599       | 22    | Ropa         |
| HOG-001 | Cafetera Espresso Automática  | $2,499     | 8     | Hogar        |
| HOG-002 | Set de Sartenes Antiadherentes| $1,599     | 6     | Hogar        |
| DEP-001 | Tenis Running Air Max         | $1,899     | 18    | Deportes     |
| DEP-002 | Mochila Deportiva 35L         | $899       | 25    | Deportes     |
| DEP-003 | Bicicleta de Montaña 29       | $12,999    | 3     | Deportes     |
| LIB-001 | Clean Code - Robert Martin    | $499       | 10    | Libros       |
| LIB-002 | Diseño de Sistemas de Software| $549       | 7     | Libros       |
| JUG-001 | Set LEGO Arquitectura NY      | $2,199     | 9     | Juguetes     |
| JUG-002 | Drone Mini FPV Pro            | $3,499     | 4     | Juguetes     |
| ALI-001 | Café Oaxaqueño Premium 500g   | $279       | 50    | Alimentos    |
| HER-001 | Set Desarmadores Profesional  | $459       | 30    | Herramientas |

Cada producto incluye **2 imágenes** de [Unsplash](https://unsplash.com) con fotos reales y relevantes para el tipo de producto.

---

## 11. Problemas encontrados y soluciones

Durante el desarrollo e integración del sistema se identificaron y resolvieron los siguientes problemas:

### P1 — Clave YAML duplicada en `uamishop-ventas`

**Síntoma:** El servicio ventas fallaba al conectarse a la base de datos.

**Causa:** En `application.yml` existía el bloque `spring.datasource` dos veces. YAML descarta silenciosamente el primero, por lo que Spring Boot recibía un datasource sin `url`, `username` ni `password`.

**Solución:** Se consolidó la clave en un único bloque con todas las propiedades (URL, credenciales e Hikari pool) correctamente anidadas.

---

### P2 — Nombre de base de datos incorrecto para catalogo

**Síntoma:** `uamishop-catalogo` no podía conectarse a MySQL con error "Unknown database".

**Causa:** `docker-compose.yml` tenía `SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/uamishop_catalogo?...` pero MySQL solo crea la base de datos `uamishop`.

**Solución:** Se corrigió a `uamishop` en la variable de entorno del servicio catalogo.

---

### P3 — Ventas no podía comunicarse con Catálogo

**Síntoma:** Las operaciones del carrito que requerían información de productos fallaban silenciosamente o el circuit breaker se abría inmediatamente.

**Causa:** El servicio ventas usa `${CATALOGO_API_URL:http://localhost:8081}`. Sin la variable de entorno, el default `localhost:8081` apuntaba a sí mismo, no al contenedor catalogo.

**Solución:** Se agregó `CATALOGO_API_URL: http://uamishop-catalogo:8081` al servicio ventas en `docker-compose.yml`.

---

### P4 — Servicios iniciaban antes que MySQL/RabbitMQ

**Síntoma:** Los microservicios fallaban en el primer arranque y necesitaban varios reinicios.

**Causa:** `depends_on` sin `condition: service_healthy` solo espera que el contenedor arranque, no que MySQL acepte conexiones.

**Solución:** Se añadieron healthchecks a `mysql` y `rabbitmq`, y se cambió `depends_on` a `condition: service_healthy` para todos los servicios backend.

---

### P5 — Dockerfile del gateway usaba `apk add maven`

**Síntoma:** La imagen del gateway fallaba al compilar o producía errores por versión de Maven incorrecta.

**Causa:** El Dockerfile usaba `eclipse-temurin:17-jdk-alpine` como base e instalaba Maven con `apk add --no-cache maven`, que instala una versión desactualizada y sin caché de dependencias.

**Solución:** Se cambió la base del stage de build a `maven:3.9-eclipse-temurin-17`, consistente con los demás microservicios, y se añadió `mvn dependency:go-offline` para caché de dependencias.

---

### P6 — Catálogo con valores hardcodeados (sin variables de entorno)

**Síntoma:** El env var del docker-compose (que apuntaba a `uamishop_catalogo`) sobreescribía el valor correcto en application.yml.

**Causa:** La URL, usuario y contraseña de la base de datos y el host de RabbitMQ estaban hardcodeados en `application.yml` sin soporte para variables de entorno.

**Solución:** Se reemplazaron con la sintaxis `${VAR:default}` para que docker-compose pueda inyectar valores correctos y el servicio también funcione en desarrollo local.

---

*Documentación generada para el proyecto UAMIShop — Ingeniería de Software*
