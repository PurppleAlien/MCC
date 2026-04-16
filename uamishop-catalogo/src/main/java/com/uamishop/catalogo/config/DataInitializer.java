package com.uamishop.catalogo.config;

import com.uamishop.catalogo.domain.Categoria;
import com.uamishop.catalogo.domain.CategoriaId;
import com.uamishop.catalogo.dto.ProductoRequest;
import com.uamishop.catalogo.repository.CategoriaJpaRepository;
import com.uamishop.catalogo.repository.ProductoJpaRepository;
import com.uamishop.catalogo.service.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Carga categorías y productos de ejemplo al arrancar si las tablas están vacías.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    // Prefijo para todas las URLs de Unsplash (fotos reales por categoría)
    private static final String UNS = "https://images.unsplash.com/photo-";
    private static final String UNS_PARAMS = "?auto=format&fit=crop&w=400&h=300";

    private static final List<String[]> CATEGORIAS_INICIALES = List.of(
        new String[]{ "Electrónica",  "Dispositivos electrónicos y accesorios"   },
        new String[]{ "Ropa",         "Prendas de vestir y accesorios de moda"   },
        new String[]{ "Hogar",        "Artículos para el hogar y decoración"     },
        new String[]{ "Deportes",     "Equipamiento y ropa deportiva"            },
        new String[]{ "Libros",       "Libros, revistas y material educativo"    },
        new String[]{ "Juguetes",     "Juguetes y juegos para niños"             },
        new String[]{ "Alimentos",    "Alimentos y bebidas"                      },
        new String[]{ "Herramientas", "Herramientas y ferretería"                }
    );

    private final CategoriaJpaRepository categoriaRepository;
    private final ProductoJpaRepository  productoRepository;
    private final ProductoService        productoService;

    public DataInitializer(CategoriaJpaRepository categoriaRepository,
                           ProductoJpaRepository  productoRepository,
                           ProductoService        productoService) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository  = productoRepository;
        this.productoService     = productoService;
    }

    @Override
    public void run(ApplicationArguments args) {
        // ── 1. Seed categorías ───────────────────────────────────────────────
        if (categoriaRepository.count() == 0) {
            for (String[] cat : CATEGORIAS_INICIALES) {
                categoriaRepository.save(new Categoria(CategoriaId.generar(), cat[0], cat[1], null));
            }
            log.info("Seed: {} categorías insertadas.", CATEGORIAS_INICIALES.size());
        } else {
            log.info("Categorías ya existentes, omitiendo seed de categorías.");
        }

        // ── 2. Seed productos ────────────────────────────────────────────────
        if (productoRepository.count() > 0) {
            log.info("Productos ya existentes, omitiendo seed de productos.");
            return;
        }

        Map<String, UUID> catMap = categoriaRepository.findAll().stream()
                .collect(Collectors.toMap(Categoria::getNombre, c -> c.getId().id()));

        int insertados = 0;
        for (Object[] p : buildProductos()) {
            String       nombre    = (String)   p[0];
            String       sku       = (String)   p[1];
            double       precio    = (double)   p[2];
            int          stock     = (int)      p[3];
            String       desc      = (String)   p[4];
            String       categoria = (String)   p[5];
            @SuppressWarnings("unchecked")
            List<String> imgs      = (List<String>) p[6];

            UUID catId = catMap.get(categoria);
            if (catId == null) {
                log.warn("Categoría '{}' no encontrada, saltando '{}'.", categoria, nombre);
                continue;
            }

            try {
                ProductoRequest req = new ProductoRequest();
                req.setNombre(nombre);
                req.setSku(sku);
                req.setPrecio(BigDecimal.valueOf(precio));
                req.setMoneda("MXN");
                req.setStock(stock);
                req.setDescripcion(desc);
                req.setCategoriaId(catId);
                req.setImagenesUrls(imgs);
                productoService.crearProducto(req);
                insertados++;
            } catch (Exception e) {
                log.warn("Error al crear producto '{}': {}", nombre, e.getMessage());
            }
        }
        log.info("Seed: {} productos insertados.", insertados);
    }

    private String img(String id) {
        return UNS + id + UNS_PARAMS;
    }

    /**
     * Productos de ejemplo con imágenes de Unsplash relevantes para cada artículo.
     * Columnas: nombre, sku, precio, stock, descripcion, categoria, List<imageUrl>
     */
    private List<Object[]> buildProductos() {
        return List.of(
            // ── Electrónica ──────────────────────────────────────────────────
            new Object[]{
                "Laptop Gaming Pro", "ELC-001", 18999.0, 5,
                "Potente laptop para gaming con GPU RTX 4070, 32 GB RAM y pantalla 144 Hz",
                "Electrónica",
                List.of(
                    img("1496181133206-80ce9b88a853"),  // laptop abierta en escritorio
                    img("1525547719346-f9c26c9dde5a")   // laptop de lado, iluminada
                )
            },
            new Object[]{
                "Smartphone Ultra X", "ELC-002", 12499.0, 12,
                "Smartphone de última generación con cámara 200 MP y batería de 5 000 mAh",
                "Electrónica",
                List.of(
                    img("1511707171634-5f897ff02aa9"),  // teléfono en mano
                    img("1592750475338-74b7b21085ab")   // smartphone pantalla encendida
                )
            },
            new Object[]{
                "Auriculares Bluetooth Pro", "ELC-003", 1899.0, 20,
                "Sonido envolvente 360° con cancelación activa de ruido y 30 h de batería",
                "Electrónica",
                List.of(
                    img("1505740420928-5e560c06d30e"),  // auriculares blancos sobre escritorio
                    img("1484704849700-f032d15ba578")   // auriculares negros
                )
            },
            new Object[]{
                "Teclado Mecánico RGB", "ELC-004", 1299.0, 14,
                "Switches Cherry MX Red, iluminación RGB por tecla y reposamuñecas magnético",
                "Electrónica",
                List.of(
                    img("1587829741301-dc798b83add3"),  // teclado mecánico con luces
                    img("1618384887929-16ec33fab9ef")   // teclado gaming RGB
                )
            },

            // ── Ropa ─────────────────────────────────────────────────────────
            new Object[]{
                "Chamarra de Mezclilla", "ROP-001", 899.0, 15,
                "Chamarra clásica 100 % mezclilla de alta calidad, disponible en azul y negro",
                "Ropa",
                List.of(
                    img("1551028719-00167b16eac5"),     // chamarra denim colgada
                    img("1576871337622-98d48d1cf531")   // chamarra denim puesta
                )
            },
            new Object[]{
                "Playera DryFit Deportiva", "ROP-002", 349.0, 40,
                "Playera técnica con tecnología DryFit para máximo rendimiento y ventilación",
                "Ropa",
                List.of(
                    img("1521572163474-6864f9cf17ab"),  // playera deportiva blanca
                    img("1589782182703-2aaa69037b5b")   // playera deportiva azul
                )
            },
            new Object[]{
                "Jeans Slim Fit Premium", "ROP-003", 599.0, 22,
                "Jeans de corte slim con tejido elástico 4D para mayor comodidad y movimiento",
                "Ropa",
                List.of(
                    img("1542272604-787c3835535d"),     // jeans doblados
                    img("1475178626620-a4d074967452")   // jeans puestos
                )
            },

            // ── Hogar ─────────────────────────────────────────────────────────
            new Object[]{
                "Cafetera Espresso Automática", "HOG-001", 2499.0, 8,
                "Cafetera automática con molinillo integrado, pantalla táctil y 15 bares de presión",
                "Hogar",
                List.of(
                    img("1495474472287-4d71bcdd2085"),  // cafetera espresso
                    img("1610889556528-9a770e32642b")   // cafetera preparando café
                )
            },
            new Object[]{
                "Set de Sartenes Antiadherentes", "HOG-002", 1599.0, 6,
                "Set de 5 sartenes con recubrimiento de titanio, aptas para inducción y lavavajillas",
                "Hogar",
                List.of(
                    img("1556909114-f6e7ad7d3136"),     // sartenes en cocina
                    img("1584568694244-14fbdf83bd30")   // sartén antiadherente
                )
            },

            // ── Deportes ─────────────────────────────────────────────────────
            new Object[]{
                "Tenis Running Air Max", "DEP-001", 1899.0, 18,
                "Tenis de alto rendimiento con amortiguación Air Max para running y trail",
                "Deportes",
                List.of(
                    img("1542291026-7eec264c27ff"),     // zapatillas deportivas rojas
                    img("1606107557195-0e29a4b5b4aa")   // tenis running blancos
                )
            },
            new Object[]{
                "Mochila Deportiva 35L", "DEP-002", 899.0, 25,
                "Mochila resistente al agua 35 L con soporte lumbar ergonómico y puerto USB",
                "Deportes",
                List.of(
                    img("1553062407-98eeb64c6a62"),     // mochila deportiva
                    img("1473188537789-3a6f3ad2073a")   // mochila en montaña
                )
            },
            new Object[]{
                "Bicicleta de Montaña 29", "DEP-003", 12999.0, 3,
                "Bicicleta todo terreno con suspensión delantera, frenos de disco y 21 velocidades",
                "Deportes",
                List.of(
                    img("1558618666-fcd25c85cd64"),     // bicicleta de montaña
                    img("1544191236-6954c0f32e63")      // bicicleta en sendero
                )
            },

            // ── Libros ────────────────────────────────────────────────────────
            new Object[]{
                "Clean Code - Robert Martin", "LIB-001", 499.0, 10,
                "La referencia definitiva para escribir código limpio, legible y mantenible",
                "Libros",
                List.of(
                    img("1544716278-ca5e3f4abd8c"),     // libro abierto con código
                    img("1456513080510-7bf3a84b82f8")   // libros de programación
                )
            },
            new Object[]{
                "Diseño de Sistemas de Software", "LIB-002", 549.0, 7,
                "Guía completa para diseñar sistemas distribuidos escalables en producción",
                "Libros",
                List.of(
                    img("1507003211169-0a1dd7228f2d"),  // libros de tecnología
                    img("1521185496955-15097b20c5fe")   // libros apilados
                )
            },

            // ── Juguetes ─────────────────────────────────────────────────────
            new Object[]{
                "Set LEGO Arquitectura NY", "JUG-001", 2199.0, 9,
                "Set coleccionable del skyline de Nueva York, 598 piezas para mayores de 16 años",
                "Juguetes",
                List.of(
                    img("1587654780291-39c9404d746b"),  // piezas de LEGO coloridas
                    img("1611996575749-79a3a250f948")   // construcción LEGO
                )
            },
            new Object[]{
                "Drone Mini FPV Pro", "JUG-002", 3499.0, 4,
                "Drone compacto con cámara 4K, gimbal de 3 ejes y autonomía de 25 minutos",
                "Juguetes",
                List.of(
                    img("1507582020474-9a35b7d455d9"),  // drone volando al atardecer
                    img("1534258936027-0f6f204bcef8")   // drone de cerca
                )
            },

            // ── Alimentos ─────────────────────────────────────────────────────
            new Object[]{
                "Café Oaxaqueño Premium 500g", "ALI-001", 279.0, 50,
                "Café de especialidad cultivado en las montañas de Oaxaca, tostado artesanal",
                "Alimentos",
                List.of(
                    img("1447933601403-0c6688de566e"),  // granos de café
                    img("1509042239860-f550ce710b93")   // taza de café
                )
            },

            // ── Herramientas ──────────────────────────────────────────────────
            new Object[]{
                "Set Desarmadores Profesional 12pzs", "HER-001", 459.0, 30,
                "Set 12 desarmadores con puntas magnéticas intercambiables y mangos ergonómicos",
                "Herramientas",
                List.of(
                    img("1530124566582-a618bc2615dc"),  // herramientas en mesa
                    img("1581244277943-fe229b5be4ef")   // desarmadores organizados
                )
            }
        );
    }
}
