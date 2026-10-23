# Registro de cambios

Este archivo resume los últimos cambios realizados en SubastaYa.

## 18 a 23 de octubre de 2026

### Seguridad y autenticación

- Se incorporó autenticación basada en JWT y contraseñas cifradas con BCrypt.
- Se agregaron los endpoints `POST /api/auth/register` y `POST /api/auth/login`.
- El inicio de sesión devuelve los datos del usuario, su rol y un token JWT.
- Se agregó un filtro que valida el token Bearer y carga al usuario autenticado en cada solicitud.
- Se configuró CORS, se deshabilitó CSRF y se definieron rutas públicas, autenticadas y exclusivas para administradores.
- Las consultas públicas de subastas y el handshake de WebSocket permanecen accesibles sin autenticación.
- Se añadieron dependencias de Spring Security, OAuth2 Resource Server y JWT.
- Los errores de autenticación y validación ahora conservan el código y mensaje HTTP correspondiente.

### Usuarios y roles

- Se incorporaron los roles `USER` y `ADMIN` para los usuarios.
- Se agregó una configuración de arranque que crea o promociona dos administradores definidos mediante propiedades de configuración.

### Subastas y ofertas

- La publicación de subastas asigna automáticamente como vendedor al usuario autenticado.
- Las ofertas toman el postor desde la sesión autenticada.

### Billeteras

- Las operaciones de billetera ahora trabajan sobre la identidad autenticada:
  - `GET /api/wallets/me`
  - `POST /api/wallets/me/deposits`
  - `GET /api/wallets/me/transactions`

### Administración

- Se creó un panel de administración con métricas de usuarios, subastas activas, próximas y finalizadas.
- El panel muestra listados de subastas y usuarios registrados.

### Frontend

- Se agregaron las pantallas de registro e inicio de sesión.
- La sesión se guarda en `sessionStorage` y las llamadas autenticadas incorporan automáticamente el encabezado `Authorization: Bearer`.
- Se agregaron rutas para `/login`, `/register` y `/admin`, con protección de acceso para administradores.
- La navegación superior muestra el usuario y rol autenticados, permite cerrar sesión y ofrece accesos para iniciar sesión o crear una cuenta.
- Las pantallas de billetera, actividades, creación de subastas y pujas en vivo ahora utilizan la sesión autenticada.
- Se actualizaron los accesos del catálogo para utilizar las rutas personales de billetera y actividades.
- Si el usuario no está autenticado y quiere realizar una puja, deberá iniciar sesión o registrarse primero.

### Observabilidad y configuración

- Se incorporó un filtro HTTP que registra método, ruta, estado de respuesta y duración de cada solicitud.
- Para iniciar el backend, se debe definir la variable de entorno `JWT_SECRET`.

### Prueba manual

- Se puede comprobar el registro HTTP enviando una solicitud desde Bruno o desde el frontend mientras el backend está ejecutándose.
- Ejemplo de salida: `HTTP GET /api/auctions -> 200 (45 ms)`.
