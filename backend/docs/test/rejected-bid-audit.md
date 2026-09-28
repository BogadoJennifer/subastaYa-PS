# AUD-01 — Auditoría de una puja inferior al mínimo

## Identificación

- Fecha: 20/09/2026.
- Responsable: 
  - Jennifer Bogado
  - Braian Martín Leuno
- Commit probado: aprobado.
- Herramientas: Bruno y pgAdmin.
- Base de datos: PostgreSQL(subastaya_seed_check)
- Resultado: aprobado para respuesta HTTP y registro de auditoría.

## Objetivo

Verificar que una puja inferior al importe mínimo sea rechazada y que el intento quede registrado en `audit_logs`.

## Preparación

- Backend en ejecución.
- Subasta en estado `ACTIVE`, dentro de su ventana temporal.
- Comprador existente y diferente del vendedor.
- Importe mínimo permitido superior a $50000.

Registrar los datos utilizados:

| Dato | Valor |
|---|---|
| ID de subasta |  1 |
| ID del comprador |  2 |
| Importe enviado |  1 |
| Importe mínimo permitido | 50000 |

## Procedimiento

1. Identificar una subasta activa.
2. Desde Bruno, enviar `POST /api/auctions/{auctionId}/bids` con el comprador seleccionado y un importe de $1.
3. Comprobar la respuesta HTTP.
4. Consultar `audit_logs`, filtrando por `action = 'REJECTED_BID'`.
5. Identificar el evento correspondiente mediante la subasta, el comprador y la fecha.

## Resultados observados

| Comprobación | Resultado |
|---|---|
| Respuesta HTTP | `400 Bad Request` |
| Mensaje | La oferta minima es $50000 |
| Evento registrado | `REJECTED_BID` |
| Motivo en `detalle_json` | `BID_BELOW_MINIMUM` |
| Referencia del evento |AuctionId = 1, BidderId = 2 |
| Datos del rechazo | importe enviado= 1 , importe mínimo = 50000 |

## Decisión técnica relacionada

Los rechazos se registran mediante `AuditLogService` en una transacción independiente (`REQUIRES_NEW`). Esto permite conservar el evento aunque la transacción de la puja sea revertida.

La auditoría registra el intento rechazado; no representa una oferta aceptada ni un movimiento contable.

## Conclusión y alcance

La solicitud inferior al mínimo fue rechazada y el evento correspondiente quedó persistido en auditoría.

Esta prueba verifica la respuesta HTTP y la trazabilidad del rechazo por monto insuficiente. No acredita por sí sola el rechazo por concurrencia, otros motivos de rechazo ni el rollback económico ante fallos.

La ausencia de nuevas ofertas y de cambios en billeteras o movimientos debe marcarse como verificada únicamente si se compararon esos datos antes y después de la solicitud.

## Evidencia

- Captura de la solicitud y respuesta en Bruno (400 Bad Request).

![image.webp](../../../../OneDrive/Desktop/image.webp)

- Captura de la consulta SQL en pgAdmin, mostrando el evento `REJECTED_BID` registrado en `audit_logs`.

![SQL evidence.webp](../../../../OneDrive/Desktop/SQL%20evidence.webp)