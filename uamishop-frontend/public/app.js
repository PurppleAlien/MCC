const GATEWAY = 'http://localhost:8090';

// ── Estado global ──────────────────────────────────────────────────────────
let todosLosProductos  = [];
let carritoActual      = null;
let productoSeleccionado = null;
let mapaProductos      = {};   // id → { nombre, sku, precio }

// ── Caché para evitar re-fetches innecesarios ──────────────────────────────
let catalogoCargado    = false;
let ordenesCargadas    = false;
let abortCtrl          = null; // AbortController activo

// ── Debounce timer para búsqueda ───────────────────────────────────────────
let searchTimer        = null;

// ══════════════════════════════════════════════════════════════════════════
// NAVEGACIÓN
// ══════════════════════════════════════════════════════════════════════════
function showSection(nombre) {
  // Cancelar cualquier request en vuelo al cambiar de sección
  if (abortCtrl) { abortCtrl.abort(); abortCtrl = null; }

  document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));

  // Desactivar TODOS los botones de nav (header + mobile)
  document.querySelectorAll('.nav-btn, .mobile-nav-btn').forEach(b => b.classList.remove('active'));

  const secEl = document.getElementById('section-' + nombre);
  if (secEl) secEl.classList.add('active');

  // Activar los botones que coincidan con la sección (header + mobile)
  document.querySelectorAll(`[data-section="${nombre}"]`)
    .forEach(b => b.classList.add('active'));

  if (nombre === 'catalogo') cargarProductos(false);
  if (nombre === 'ordenes')  cargarOrdenes(false);
}

// ══════════════════════════════════════════════════════════════════════════
// CATÁLOGO
// ══════════════════════════════════════════════════════════════════════════
async function cargarProductos(forzar = false) {
  // OPTIMIZACIÓN: no re-fetchear si ya tenemos datos y no se fuerza
  if (catalogoCargado && !forzar) {
    renderProductos(todosLosProductos);
    return;
  }

  const grid = document.getElementById('productos-grid');
  grid.innerHTML = buildSkeletonHTML(8);

  abortCtrl = new AbortController();
  const signal = abortCtrl.signal;

  try {
    const [productosRes, categoriasRes] = await Promise.all([
      fetch(`${GATEWAY}/api/v1/productos`, { signal }),
      fetch(`${GATEWAY}/api/v1/categorias`, { signal })
    ]);

    if (!productosRes.ok) throw new Error(`Error ${productosRes.status} al cargar productos`);
    todosLosProductos = await productosRes.json();

    if (categoriasRes.ok) {
      const categorias = await categoriasRes.json();
      const select = document.getElementById('prod-categoria');
      // Solo repoblar si cambió
      if (select.options.length <= 1) {
        select.innerHTML = '<option value="">-- Seleccionar Categoría --</option>';
        const frag = document.createDocumentFragment();
        categorias.forEach(c => {
          const opt = document.createElement('option');
          opt.value = c.id;
          opt.textContent = c.nombre;
          frag.appendChild(opt);
        });
        select.appendChild(frag);
      }
    }

    // Actualizar mapa de productos para lookups O(1)
    todosLosProductos.forEach(p => {
      mapaProductos[p.id] = { nombre: p.nombre, sku: p.sku, precio: p.precio?.cantidad };
    });

    catalogoCargado = true;
    renderProductos(todosLosProductos);
    abortCtrl = null;
  } catch (e) {
    if (e.name === 'AbortError') return; // Navegación, ignorar
    grid.innerHTML = `
      <div class="empty-panel fade-in">
        <div class="empty-icon">⚠️</div>
        <p>Error al cargar catálogo</p>
        <small>${e.message}</small>
        <button class="btn-outline btn-sm" style="margin-top:12px" onclick="cargarProductos(true)">
          Reintentar
        </button>
      </div>`;
    toast('Sin conexión con el servidor. Verifica que Docker esté corriendo.', 'error');
  }
}

function buildSkeletonHTML(n) {
  return `<div class="skeleton-grid">${'<div class="skeleton-card"></div>'.repeat(n)}</div>`;
}

function renderProductos(productos) {
  const grid = document.getElementById('productos-grid');
  if (!productos.length) {
    grid.innerHTML = `
      <div class="empty-panel fade-in">
        <div class="empty-icon">🔍</div>
        <p>No se encontraron productos</p>
        <small>Intenta con otro término de búsqueda</small>
      </div>`;
    return;
  }

  // Usar DocumentFragment para reducir reflows
  const html = productos.map((p, i) => {
    const disponible = p.disponible !== false && p.stock > 0;
    const safe = p.nombre.replace(/\\/g, '\\\\').replace(/'/g, "\\'");
    const stockPct = Math.min(100, Math.round((p.stock / 20) * 100));
    const categoriaColor = stringToHue(p.sku.substring(0, 3));

    return `
    <div class="product-card fade-in" style="animation-delay:${i * 40}ms">
      <div class="product-card-accent" style="background:hsl(${categoriaColor},65%,55%)"></div>
      <div class="product-card-body">
        <div class="product-card-top">
          <span class="badge ${disponible ? 'badge-success' : 'badge-danger'}">
            ${disponible ? '● Disponible' : (p.stock === 0 ? '✕ Sin stock' : '✕ No disponible')}
          </span>
        </div>
        <h3 class="product-name">${p.nombre}</h3>
        <div class="product-sku">${p.sku}</div>
        ${p.descripcion ? `<p class="product-desc">${p.descripcion}</p>` : ''}
        <div class="product-price-row">
          <span class="product-price">$${formatPrice(p.precio?.cantidad)}</span>
          <span class="currency">MXN</span>
        </div>
        <div class="stock-bar-wrap">
          <div class="stock-bar-label">
            <span class="${p.stock <= 3 ? 'text-danger' : 'text-muted'}">
              ${p.stock <= 3 && p.stock > 0 ? '⚠ ' : ''}${p.stock} en stock
            </span>
          </div>
          <div class="stock-bar">
            <div class="stock-bar-fill ${p.stock <= 3 ? 'low' : ''}"
              style="width:${stockPct}%"></div>
          </div>
        </div>
        <button class="btn-add-cart ${!disponible ? 'disabled' : ''}"
          onclick="${disponible ? `abrirModalAgregar('${p.id}','${safe}',${p.precio?.cantidad},'${p.sku}')` : ''}"
          ${!disponible ? 'disabled' : ''}>
          <span class="btn-cart-icon">🛒</span>
          Agregar al carrito
        </button>
      </div>
    </div>`;
  }).join('');

  grid.innerHTML = `<div class="products-grid">${html}</div>`;
}

// Convierte una cadena (SKU prefix) a un ángulo de hue para colores únicos
function stringToHue(str) {
  let hash = 0;
  for (let i = 0; i < str.length; i++) hash = str.charCodeAt(i) + ((hash << 5) - hash);
  return Math.abs(hash) % 360;
}

// OPTIMIZACIÓN: debounce 250ms en búsqueda para no re-renderizar en cada tecla
function filtrarProductos() {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(() => {
    const q = document.getElementById('search-input').value.toLowerCase().trim();
    if (!q) { renderProductos(todosLosProductos); return; }
    const filtrados = todosLosProductos.filter(p =>
      p.nombre.toLowerCase().includes(q) || p.sku.toLowerCase().includes(q)
    );
    renderProductos(filtrados);
  }, 250);
}

function toggleCrearProducto() {
  const form = document.getElementById('form-crear-producto');
  const isHidden = form.classList.contains('hidden');
  form.classList.toggle('hidden');
  if (isHidden) {
    form.style.animation = 'slideDown 0.25s ease';
    document.getElementById('prod-nombre').focus();
  }
}

async function crearProducto() {
  const nombre      = document.getElementById('prod-nombre').value.trim();
  const sku         = document.getElementById('prod-sku').value.trim().toUpperCase();
  const precio      = parseFloat(document.getElementById('prod-precio').value);
  const stock       = parseInt(document.getElementById('prod-stock').value);
  const descripcion = document.getElementById('prod-descripcion').value.trim();
  const categoriaId = document.getElementById('prod-categoria').value;

  if (!nombre || !sku || isNaN(precio) || isNaN(stock) || !categoriaId) {
    toast('Completa todos los campos obligatorios', 'error'); return;
  }
  if (!/^[A-Z]{3}-\d{3}$/.test(sku)) {
    toast('SKU inválido. Formato: ABC-123 (3 letras + guión + 3 números)', 'error'); return;
  }
  if (precio <= 0) { toast('El precio debe ser mayor a 0', 'error'); return; }

  const btn = document.getElementById('btn-guardar-producto');
  setLoading(btn, true, 'Guardando...');

  try {
    const res = await fetch(`${GATEWAY}/api/v1/productos`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nombre, sku, descripcion, precio, moneda: 'MXN', stock, categoriaId })
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || err.detail || `Error ${res.status}`);
    }
    toast('✓ Producto creado', 'success');
    toggleCrearProducto();
    ['prod-nombre','prod-sku','prod-precio','prod-stock','prod-descripcion']
      .forEach(id => { document.getElementById(id).value = ''; });
    document.getElementById('prod-categoria').value = '';
    catalogoCargado = false; // Invalidar caché
    cargarProductos(true);
  } catch (e) {
    toast(e.message, 'error');
  } finally {
    setLoading(btn, false, '💾 Guardar Producto');
  }
}

// ══════════════════════════════════════════════════════════════════════════
// MODAL – AGREGAR AL CARRITO
// ══════════════════════════════════════════════════════════════════════════
function abrirModalAgregar(id, nombre, precio, sku) {
  if (!carritoActual) {
    toast('Primero carga tu carrito en la sección "Carrito"', 'info');
    showSection('carrito');
    return;
  }
  if (carritoActual.estado !== 'ACTIVO') {
    toast('Tu carrito ya no está activo. Crea uno nuevo.', 'error'); return;
  }
  productoSeleccionado = { id, nombre, precio: Number(precio), sku };
  document.getElementById('modal-producto-nombre').textContent = nombre;
  document.getElementById('modal-producto-precio').textContent = '$' + formatPrice(precio) + ' MXN';
  document.getElementById('modal-cantidad').value = 1;
  actualizarSubtotalModal();
  const modal = document.getElementById('modal-agregar');
  modal.classList.remove('hidden');
  modal.querySelector('.modal-box').style.animation = 'scaleIn 0.22s cubic-bezier(0.34,1.56,0.64,1)';
}

function cerrarModal() {
  document.getElementById('modal-agregar').classList.add('hidden');
  productoSeleccionado = null;
}

function cerrarModalIfOutside(event) {
  if (event.target === document.getElementById('modal-agregar')) cerrarModal();
}

function cambiarCantidad(delta) {
  const input = document.getElementById('modal-cantidad');
  input.value = Math.max(1, Math.min(99, (parseInt(input.value) || 1) + delta));
  actualizarSubtotalModal();
}

function actualizarSubtotalModal() {
  if (!productoSeleccionado) return;
  const cantidad = parseInt(document.getElementById('modal-cantidad').value) || 1;
  const subtotal = cantidad * productoSeleccionado.precio;
  const el = document.getElementById('modal-subtotal');
  if (el) {
    el.textContent = 'Subtotal: $' + formatPrice(subtotal) + ' MXN';
    el.classList.add('pulse');
    setTimeout(() => el.classList.remove('pulse'), 300);
  }
}

async function confirmarAgregar() {
  const cantidad = parseInt(document.getElementById('modal-cantidad').value);
  if (!productoSeleccionado || cantidad < 1) return;

  const prod = { ...productoSeleccionado }; // copia antes de cerrar modal
  const carritoId = carritoActual.id?.valor || carritoActual.id;
  const btn = document.getElementById('btn-confirmar-agregar');

  // ── UI OPTIMISTA: cerrar modal y mostrar item inmediatamente ──
  mapaProductos[prod.id] = { nombre: prod.nombre, sku: prod.sku, precio: prod.precio };

  // Añadir item temporalmente al carrito local para render inmediato
  const itemExistente = carritoActual.items?.find(i =>
    (i.productoId?.valor || i.productoId) === prod.id
  );
  if (itemExistente) {
    itemExistente.cantidad += cantidad;
  } else {
    if (!carritoActual.items) carritoActual.items = [];
    carritoActual.items.push({
      productoId: { valor: prod.id },
      cantidad,
      precioUnitario: { cantidad: prod.precio, moneda: 'MXN' }
    });
  }
  cerrarModal();
  renderCarrito();
  toast(`✓ ${prod.nombre} agregado`, 'success');

  // ── Petición real en background ──
  setLoading(btn, true, 'Agregando...');
  try {
    const res = await fetch(`${GATEWAY}/api/v1/carritos/${carritoId}/productos`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ productoId: prod.id, cantidad })
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || `Error ${res.status}`);
    }
    // Sincronizar con respuesta real del servidor
    carritoActual = await res.json();
    renderCarrito();
  } catch (e) {
    // Rollback: quitar el item optimista
    if (carritoActual.items) {
      carritoActual.items = carritoActual.items.filter(i =>
        (i.productoId?.valor || i.productoId) !== prod.id
      );
    }
    renderCarrito();
    toast('Error al agregar: ' + e.message, 'error');
  } finally {
    setLoading(btn, false, '🛒 Agregar al Carrito');
  }
}

// ══════════════════════════════════════════════════════════════════════════
// CARRITO
// ══════════════════════════════════════════════════════════════════════════
async function crearOCargarCarrito() {
  const clienteId = document.getElementById('cliente-id').value.trim();
  if (!clienteId) { toast('Ingresa un ID de cliente', 'error'); return; }
  if (!/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(clienteId)) {
    toast('Debe ser un UUID válido', 'error'); return;
  }

  const btn = document.querySelector('.cliente-panel .btn-primary');
  setLoading(btn, true, 'Cargando...');

  try {
    const res = await fetch(`${GATEWAY}/api/v1/carritos?clienteId=${clienteId}`, { method: 'POST' });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || `Error ${res.status}`);
    }
    carritoActual = await res.json();
    renderCarrito();
    toast('✓ Carrito listo', 'success');
  } catch (e) {
    toast(e.message, 'error');
  } finally {
    setLoading(btn, false, 'Cargar Carrito');
  }
}

async function eliminarItemCarrito(productoId) {
  if (!carritoActual) return;
  const carritoId = carritoActual.id?.valor || carritoActual.id;

  // Feedback inmediato: animar el item antes del request
  const itemEl = document.querySelector(`[data-producto-id="${productoId}"]`);
  if (itemEl) { itemEl.style.opacity = '0.4'; itemEl.style.pointerEvents = 'none'; }

  try {
    const res = await fetch(
      `${GATEWAY}/api/v1/carritos/${carritoId}/productos/${productoId}`,
      { method: 'DELETE' }
    );
    if (!res.ok) throw new Error('Error al eliminar producto');
    carritoActual = await res.json();
    renderCarrito();
    toast('Producto eliminado del carrito', 'success');
  } catch (e) {
    if (itemEl) { itemEl.style.opacity = '1'; itemEl.style.pointerEvents = ''; }
    toast(e.message, 'error');
  }
}

function renderCarrito() {
  document.getElementById('carrito-info').classList.remove('hidden');
  document.getElementById('carrito-empty').classList.add('hidden');

  document.getElementById('carrito-id-display').textContent =
    carritoActual.id?.valor || carritoActual.id;

  const estadoBadge = document.getElementById('carrito-estado');
  estadoBadge.textContent = carritoActual.estado;
  estadoBadge.className = 'badge ' + badgeClase(carritoActual.estado);

  const items = carritoActual.items || [];
  updateCarritoBadge(items.length);

  const itemsDiv = document.getElementById('carrito-items');
  if (!items.length) {
    itemsDiv.innerHTML = `
      <div class="carrito-vacio-msg">
        <span>🛒</span>
        <p>El carrito está vacío</p>
        <small>Agrega productos desde el Catálogo</small>
      </div>`;
  } else {
    itemsDiv.innerHTML = items.map(item => {
      const pid    = item.productoId?.valor || item.productoId;
      const info   = mapaProductos[pid] || {};
      const nombre = info.nombre || `Producto ${String(pid).substring(0, 8)}...`;
      const sku    = info.sku || '';
      const precio = item.precioUnitario?.cantidad || 0;
      const sub    = (item.cantidad || 1) * precio;
      return `
      <div class="carrito-item" data-producto-id="${pid}">
        <div class="item-avatar" style="background:hsl(${stringToHue(sku.substring(0,3))},60%,90%)">
          <span style="color:hsl(${stringToHue(sku.substring(0,3))},60%,35%);font-size:1.1rem">
            ${nombre.charAt(0).toUpperCase()}
          </span>
        </div>
        <div class="item-info">
          <h4>${nombre}</h4>
          <span class="item-sku">${sku}</span>
          <span class="item-qty">${item.cantidad} × $${formatPrice(precio)} MXN</span>
        </div>
        <div class="item-right">
          <span class="item-price">$${formatPrice(sub)}</span>
          <button class="btn-remove" onclick="eliminarItemCarrito('${pid}')" title="Quitar">
            <svg width="12" height="12" viewBox="0 0 12 12" fill="currentColor">
              <path d="M1 1l10 10M11 1L1 11" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
      </div>`;
    }).join('');
  }

  const total = items.reduce((s, i) => s + (i.cantidad || 1) * (i.precioUnitario?.cantidad || 0), 0);
  document.getElementById('carrito-total').textContent = '$' + formatPrice(total) + ' MXN';

  const checkoutBtn = document.querySelector('.btn-checkout');
  if (checkoutBtn) {
    const ok = items.length > 0 && carritoActual.estado === 'ACTIVO';
    checkoutBtn.disabled = !ok;
  }
}

function updateCarritoBadge(count) {
  // Actualizar badge en header nav y en mobile nav
  ['carrito-badge', 'mobile-carrito-badge'].forEach(id => {
    const badge = document.getElementById(id);
    if (!badge) return;
    badge.textContent = count;
    badge.classList.toggle('hidden', count === 0);
    if (count > 0) {
      badge.style.animation = 'none';
      requestAnimationFrame(() => {
        badge.style.animation = 'popIn 0.3s cubic-bezier(0.34,1.56,0.64,1)';
      });
    }
  });
}

async function checkout() {
  if (!carritoActual?.items?.length) { toast('El carrito está vacío', 'error'); return; }
  if (carritoActual.estado !== 'ACTIVO') { toast('El carrito no está activo', 'error'); return; }

  const clienteId  = document.getElementById('cliente-id').value.trim();
  const carritoId  = carritoActual.id?.valor || carritoActual.id;
  const numeroOrden = 'ORD-' + Date.now();
  const btn = document.querySelector('.btn-checkout');
  setLoading(btn, true, '⏳ Procesando...');

  const body = {
    numeroOrden, clienteId, carritoId,
    items: carritoActual.items.map(item => {
      const pid  = item.productoId?.valor || item.productoId;
      const info = mapaProductos[pid] || {};
      return {
        productoId: pid,
        nombreProducto: info.nombre || 'Producto',
        sku: info.sku || 'PRD-001',
        cantidad: item.cantidad,
        precioUnitario: item.precioUnitario
      };
    }),
    direccionEnvio: {
      nombreDestinatario: 'Cliente UAMIShop',
      calle: 'Av. Universidad 3000, Copilco',
      ciudad: 'Ciudad de Mexico',
      estado: 'CDMX',
      pais: 'Mexico',
      codigoPostal: '04360',
      telefono: '5551234567'
    }
  };

  try {
    const res = await fetch(`${GATEWAY}/api/v1/ordenes`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || err.detail || `Error ${res.status}`);
    }
    const orden = await res.json();
    toast('🎉 ¡Orden creada! ' + orden.numeroOrden, 'success');

    carritoActual = null;
    updateCarritoBadge(0);
    document.getElementById('carrito-info').classList.add('hidden');
    document.getElementById('carrito-empty').classList.remove('hidden');
    ordenesCargadas = false; // Invalidar caché de órdenes
    showSection('ordenes');
  } catch (e) {
    toast(e.message, 'error');
    setLoading(btn, false, 'Crear Orden →');
  }
}

// ══════════════════════════════════════════════════════════════════════════
// ÓRDENES
// ══════════════════════════════════════════════════════════════════════════
// Pasos del flujo de órdenes (para la barra de progreso)
const ORDEN_PASOS = ['PENDIENTE', 'CONFIRMADA', 'PAGO_PROCESADO', 'EN_PROCESO', 'ENVIADA', 'ENTREGADA'];

async function cargarOrdenes(forzar = false) {
  // OPTIMIZACIÓN: no re-fetchear si ya tenemos datos
  if (ordenesCargadas && !forzar) return;

  const lista = document.getElementById('ordenes-list');
  lista.innerHTML = '<div class="empty-state"><div class="spinner"></div> Cargando órdenes...</div>';

  abortCtrl = new AbortController();
  try {
    const res = await fetch(`${GATEWAY}/api/v1/ordenes`, { signal: abortCtrl.signal });
    if (!res.ok) throw new Error(`Error ${res.status}`);
    const ordenes = await res.json();
    ordenesCargadas = true;
    abortCtrl = null;

    if (!ordenes.length) {
      lista.innerHTML = `
        <div class="empty-panel fade-in">
          <div class="empty-icon">📦</div>
          <p>No hay órdenes registradas</p>
          <small>Crea una orden desde el Carrito</small>
        </div>`;
      return;
    }

    lista.innerHTML = ordenes.map((o, i) => {
      const total = (o.items || []).reduce((s, it) =>
        s + (it.cantidad || 1) * (it.precioUnitario?.cantidad || 0), 0);
      const pasoActual = ORDEN_PASOS.indexOf(o.estado);
      return `
      <div class="orden-card fade-in" style="animation-delay:${i * 50}ms">
        <div class="orden-header">
          <div class="orden-title-group">
            <h3>${o.numeroOrden}</h3>
            <span class="badge ${badgeClase(o.estado)}">${o.estado.replace(/_/g, ' ')}</span>
          </div>
          <span class="orden-total">$${formatPrice(total)} <span class="currency">MXN</span></span>
        </div>

        ${pasoActual >= 0 ? `
        <div class="orden-progress">
          ${ORDEN_PASOS.map((paso, idx) => `
            <div class="progress-step ${idx < pasoActual ? 'done' : ''} ${idx === pasoActual ? 'active' : ''}">
              <div class="progress-dot"></div>
              <span>${paso.replace(/_/g, ' ')}</span>
            </div>
          `).join('')}
        </div>` : ''}

        <div class="orden-meta">
          <span><strong>ID:</strong> <code>${o.id?.substring(0,13)}…</code></span>
          <span><strong>Items:</strong> ${o.items?.length || 0}</span>
        </div>

        ${o.items?.length ? `
        <div class="orden-items">
          ${o.items.map(it => `
            <span class="orden-item-chip">${it.nombreProducto || it.sku} ×${it.cantidad}</span>
          `).join('')}
        </div>` : ''}

        <div class="orden-actions">
          ${o.estado === 'PENDIENTE' ? `
            <button class="btn-action btn-action-confirm" onclick="confirmarOrden('${o.id}')">
              <span>✓</span> Confirmar Orden
            </button>` : ''}
          ${o.estado === 'CONFIRMADA' ? `
            <button class="btn-action btn-action-pay" onclick="pagarOrden('${o.id}')">
              <span>💳</span> Procesar Pago
            </button>` : ''}
          ${o.estado === 'PAGO_PROCESADO' ? `
            <button class="btn-action btn-action-process" onclick="marcarEnProceso('${o.id}')">
              <span>📦</span> Enviar a Proceso
            </button>` : ''}
        </div>
      </div>`;
    }).join('');
  } catch (e) {
    if (e.name === 'AbortError') return;
    lista.innerHTML = `
      <div class="empty-panel fade-in">
        <div class="empty-icon">⚠️</div>
        <p>Error al cargar órdenes</p>
        <small>${e.message}</small>
        <button class="btn-outline btn-sm" style="margin-top:12px" onclick="cargarOrdenes(true)">Reintentar</button>
      </div>`;
  }
}

async function confirmarOrden(id) {
  const btn = event.currentTarget;
  // UI optimista: cambiar badge inmediatamente
  actualizarBadgeOrden(id, 'CONFIRMADA', btn);
  try {
    const res = await fetch(`${GATEWAY}/api/v1/ordenes/${id}/confirmar?usuario=operador1`, { method: 'PATCH' });
    if (!res.ok) { const e = await res.json().catch(()=>{}); throw new Error(e?.message || 'Error'); }
    toast('✓ Orden confirmada', 'success');
    ordenesCargadas = false;
    cargarOrdenes(true);
  } catch (e) {
    toast(e.message, 'error');
    ordenesCargadas = false;
    cargarOrdenes(true); // recargar para revertir estado
  }
}

async function pagarOrden(id) {
  const ref = 'REF-' + Date.now();
  const btn = event.currentTarget;
  actualizarBadgeOrden(id, 'PAGO_PROCESADO', btn);
  try {
    const res = await fetch(`${GATEWAY}/api/v1/ordenes/${id}/pago?referencia=${ref}`, { method: 'PATCH' });
    if (!res.ok) { const e = await res.json().catch(()=>{}); throw new Error(e?.message || 'Error'); }
    toast('✓ Pago procesado: ' + ref, 'success');
    ordenesCargadas = false;
    cargarOrdenes(true);
  } catch (e) {
    toast(e.message, 'error');
    ordenesCargadas = false;
    cargarOrdenes(true);
  }
}

async function marcarEnProceso(id) {
  const btn = event.currentTarget;
  actualizarBadgeOrden(id, 'EN_PROCESO', btn);
  try {
    const res = await fetch(`${GATEWAY}/api/v1/ordenes/${id}/en-proceso`, { method: 'PATCH' });
    if (!res.ok) { const e = await res.json().catch(()=>{}); throw new Error(e?.message || 'Error'); }
    toast('✓ Orden en proceso', 'success');
    ordenesCargadas = false;
    cargarOrdenes(true);
  } catch (e) {
    toast(e.message, 'error');
    ordenesCargadas = false;
    cargarOrdenes(true);
  }
}

// Actualiza visualmente el badge de una orden sin esperar al servidor
function actualizarBadgeOrden(id, nuevoEstado, btn) {
  const card = btn?.closest('.orden-card');
  if (card) {
    const badge = card.querySelector('.badge');
    if (badge) {
      badge.textContent = nuevoEstado.replace(/_/g, ' ');
      badge.className = 'badge ' + badgeClase(nuevoEstado);
    }
    btn.disabled = true;
    btn.textContent = '⏳ Procesando...';
  }
}

// ══════════════════════════════════════════════════════════════════════════
// UTILIDADES
// ══════════════════════════════════════════════════════════════════════════
function formatPrice(n) {
  if (n == null) return '0.00';
  return Number(n).toLocaleString('es-MX', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function badgeClase(estado) {
  const map = {
    ACTIVO:'badge-success', EN_CHECKOUT:'badge-warning', COMPLETADO:'badge-success',
    ABANDONADO:'badge-danger', PENDIENTE:'badge-info', CONFIRMADA:'badge-info',
    PAGO_PROCESADO:'badge-success', EN_PROCESO:'badge-warning', ENVIADA:'badge-warning',
    ENTREGADA:'badge-success', CANCELADA:'badge-danger', CERRADO:'badge-danger'
  };
  return map[estado] || 'badge-info';
}

// Utilidad: poner/quitar estado de carga en un botón
function setLoading(btn, loading, label) {
  if (!btn) return;
  btn.disabled = loading;
  btn.textContent = label;
}

function toast(msg, tipo = 'info') {
  const container = document.getElementById('toast-container');
  const icons = { success: '✓', error: '✕', info: 'ℹ' };
  const t = document.createElement('div');
  t.className = `toast toast-${tipo}`;
  t.innerHTML = `<span class="toast-icon">${icons[tipo] || 'ℹ'}</span><span>${msg}</span>`;
  container.appendChild(t);

  // Forzar reflow para que la animación arranque correctamente
  t.getBoundingClientRect();
  t.classList.add('show');

  setTimeout(() => {
    t.classList.remove('show');
    t.classList.add('hide');
    setTimeout(() => t.remove(), 350);
  }, 3600);
}

// ══════════════════════════════════════════════════════════════════════════
// INIT
// ══════════════════════════════════════════════════════════════════════════
window.addEventListener('DOMContentLoaded', () => {
  cargarProductos(true); // Primera carga forzada

  // Tecla Escape cierra el modal
  document.addEventListener('keydown', e => {
    if (e.key === 'Escape') cerrarModal();
  });
});
