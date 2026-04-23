# Vistas Importadas desde Google Stitch
## Proyecto: App Concentrados Puente Tierra NATIVA
**Proyecto ID:** `3612217055800326374`  
**URL Stitch:** https://stitch.withgoogle.com/projects/3612217055800326374  
**Tipo:** Android Mobile (MOBILE, 390x884dp)  
**Tema:** "The Earthbound Editorial" — Terracota (#a33900) + Verde Oscuro (#1b6d24)

---

## Pantallas Descargadas

| Archivo | Pantalla | Descripción |
|---|---|---|
| `01_inicio.html` | Inicio - Concentrados Puente Tierra | Pantalla de inicio/home con categorías y productos destacados |
| `02_login.html` | Autenticación Actualizada | Login y registro de usuarios |
| `03_catalogo_productos.html` | Catálogo de Productos Actualizado | Lista de productos con filtros |
| `04_detalle_producto.html` | Detalle de Producto | Vista detallada de un producto |
| `05_carrito.html` | Carrito de Compras Actualizado | Carrito de compras con items |
| `06_checkout.html` | Checkout Actualizado | Proceso de pago y envío |
| `07_panel_pedidos.html` | Panel de Pedidos Actualizado | Panel de gestión de pedidos |
| `08_detalle_pedido.html` | Detalle de Pedido Detallado | Vista detallada de un pedido |
| `09_gestion_inventario.html` | Gestión de Inventario Actualizada | Gestión de stock e inventario |
| `10_gestion_productos.html` | Gestión de Productos | CRUD de productos (admin) |
| `11_gestion_usuarios.html` | Gestión de Usuarios | Administración de usuarios |
| `12_reportes_ventas.html` | Reportes de Ventas Actualizado | Reportes y estadísticas de ventas |
| `13_reportes_negocio.html` | Reportes de Negocio | KPIs y métricas de negocio |
| `14_dashboard_admin.html` | Dashboard Administrativo | Panel principal de administración |
| `15_mi_perfil.html` | Mi Perfil Actualizado | Perfil y configuración del usuario |

---

## Design System

- **Paleta primaria:** Terracota `#a33900` (El Suelo)
- **Paleta secundaria:** Verde oscuro `#1b6d24` (El Crecimiento)
- **Terciario:** Ámbar `#7f5300` (La Cosecha)
- **Fondo:** Blanco cálido `#faf9f5` (Hueso)
- **Tipografías:** Noto Serif (títulos) · Inter (cuerpo) · Manrope (etiquetas/botones)
- **Radio de borde:** Fully rounded (`rounded-xl` = 3rem en botones, `rounded-lg` = 2rem en cards)
- **Modo:** Light mode

## Notas para implementación Android

Los archivos HTML contienen el diseño completo con:
- Estilos CSS inline (Variables CSS con los tokens del design system)
- Estructura de componentes Material Design 3 adaptados
- Fuentes de Google Fonts (Noto Serif, Inter, Manrope)

Para usar en Android nativo (Jetpack Compose o XML):
1. Usar `WebView` para renderizar directamente los HTML (más rápido)
2. O convertir a layouts XML/Compose manualmente siguiendo los estilos CSS
