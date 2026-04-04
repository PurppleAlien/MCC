package com.uamishop.catalogo.config;

import com.uamishop.catalogo.domain.Categoria;
import com.uamishop.catalogo.domain.CategoriaId;
import com.uamishop.catalogo.repository.CategoriaJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Carga categorías iniciales al arrancar el servicio si la tabla está vacía.
 * Para agregar o modificar categorías, editar la lista CATEGORIAS_INICIALES.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    // ── MODIFICAR AQUÍ para agregar o cambiar categorías ────────────────────
    private static final List<String[]> CATEGORIAS_INICIALES = List.of(
        // { nombre, descripcion }
        new String[]{ "Electrónica",  "Dispositivos electrónicos y accesorios"   },
        new String[]{ "Ropa",         "Prendas de vestir y accesorios de moda"   },
        new String[]{ "Hogar",        "Artículos para el hogar y decoración"     },
        new String[]{ "Deportes",     "Equipamiento y ropa deportiva"            },
        new String[]{ "Libros",       "Libros, revistas y material educativo"    },
        new String[]{ "Juguetes",     "Juguetes y juegos para niños"             },
        new String[]{ "Alimentos",    "Alimentos y bebidas"                      },
        new String[]{ "Herramientas", "Herramientas y ferretería"                }
    );
    // ────────────────────────────────────────────────────────────────────────

    private final CategoriaJpaRepository categoriaRepository;

    public DataInitializer(CategoriaJpaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (categoriaRepository.count() > 0) {
            log.info("Categorías ya existentes, omitiendo seed.");
            return;
        }
        for (String[] cat : CATEGORIAS_INICIALES) {
            categoriaRepository.save(new Categoria(CategoriaId.generar(), cat[0], cat[1], null));
        }
        log.info("Seed: {} categorías insertadas.", CATEGORIAS_INICIALES.size());
    }
}
