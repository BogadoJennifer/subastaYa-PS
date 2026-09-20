# Prueba manual de ofertas concurrentes con bloqueo optimista (Optimistic Locking)

## Identificación
- Código: CONC-01.
- Fecha: 19/09/2026
- Responsables: 
  -  Jennifer Bogado
  -  Braian Martín Leuno
- Herramientas: IntelliJ Debug, Bruno y pgAdmin.
- Resultado: aprobado.

## Objetivo:
Verificar que dos solicitudes que leen la misma versión de una subasta no confirmen ambas sus cambios y que la operación rechazada no deje ofertas, retenciones ni movimientos contables persistidos.

## Preparación:
| Dato | Valor |
|---|---|
| ID de subasta | 11 |
| Estado inicial | Active |
| Versión inicial | 0 |
| Precio base / mayor oferta previa | 10000 |
| Incremento mínimo | 1000 |
| Usuario A | 2 |
| Usuario B | 3 |
| Importe enviado por cada usuario | 11000 |
| Saldos iniciales de ambos | 11000 |

Aclaracion: los compradores eran distintos del vendedor y la subasta permaneció abierta durante la prueba.

## 1. Procedimiento realizado:

   - Ejecutamos el backend en modo Debug.
   - Colocamos un breakpoint antes de escrowService.processEscrow, después de validar el importe mínimo.
   - Configuramos el breakpoint para suspender únicamente el hilo.
   - Enviamos dos solicitudes desde Bruno al mismo recurso:
   - POST /api/auctions/{auctionId}/bids.
   - Confirmamos que ambas estaban detenidas con el mismo identificador y la misma versión de subasta.
   - Reanudamos la ejecución.
   - Registramos las respuestas HTTP.
   - Consultamos ofertas, billeteras y movimientos para comprobar el resultado persistido.

El debugger permitió coordinar dos transacciones sobre una misma versión. No fue necesario que las solicitudes llegaran exactamente al mismo tiempo.

## 2. Resultados esperados y observados

| Comprobación | Esperado | Observado |
|---|---|-----------|
| Versión leída por ambas solicitudes | Mismo valor | Si        |
| Solicitudes aceptadas | Una con `201 Created` | Si        |
| Solicitud rechazada | Una con `409 Conflict` | Si        |
| Ofertas nuevas persistidas | Una | Si        |
| Saldo total de compradores | Sin cambios por la puja | Si        |
| Retención del comprador aceptado | Aumenta según la oferta y situación inicial | Si        |
| Saldos del comprador rechazado | Sin cambios respecto del inicio | Si        |
| Movimientos de la solicitud rechazada | Ninguno persistido | Si        |
| Versión de la subasta | Avanza | Si        |

### Evidencia de 1er petición (201 Created)
![image.webp](../../../AppData/Local/Temp/image.webp)

### Evidencia de 2da petición (409 Conflict)
![2da peticion.webp](../../../OneDrive/Desktop/2da%20peticion.webp)


### Evidencia de puja de la 1er petición (201 Created)
![SQL Evidence Optimistic-Locking.webp](../../../OneDrive/Desktop/SQL%20Evidence%20Optimistic-Locking.webp)

## 3. Conclusiones:

La prueba CONC-01 fue satisfactoria: dos transacciones leyeron la misma versión de una subasta, solo una confirmó su oferta y la otra recibió HTTP 409. Las consultas posteriores confirmaron que la operación rechazada no dejó cambios monetarios ni ofertas persistidas.
Esta prueba verifica un escenario controlado de concurrencia. No constituye una prueba de carga ni demuestra por sí sola el comportamiento ante todas las combinaciones de pujas, billeteras compartidas y cierre automático.

**nota**: se pueden observar las 2 solicitudes de Bruno en `/docs/collections`.