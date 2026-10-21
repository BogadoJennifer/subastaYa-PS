# Registro de cambios

Este archivo resume los cambios realizados en SubastaYa.

## [Sin publicar] - 2026-10-18

### Agregado
- Se incorporó un filtro HTTP para registrar las solicitudes recibidas por el backend en la consola.
- Cada registro muestra el método HTTP, la ruta, el código de respuesta y el tiempo de procesamiento.

### Configuración
- Para iniciar el backend, se debe definir la variable de entorno `JWT_SECRET`.
- La clave debe mantenerse privada y no incluirse en el repositorio.

### Prueba manual
- Se puede comprobar el registro enviando una solicitud desde Bruno o desde el frontend mientras el backend está ejecutándose.
- Ejemplo de salida: `HTTP GET /api/auctions -> 200 (45 ms)`.