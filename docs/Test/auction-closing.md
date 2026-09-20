# CLOSE-01 — Cierre automático y auditoría de subastas

## Identificación

- Fecha: 20-09-2026.
- Responsables: Bogado Jennifer, Braian Leuno .
- Commit probado: Si.
- Herramientas: IntelliJ, PostgreSQL y pgAdmin.
- Base de prueba: `subastaya_seed_check`.
- Resultado: aprobado en ejecución manual.

## Objetivo

Verificar que el worker cierre una subasta con ganador y otra sin ofertas, liquide los fondos correspondientes y registre los cambios de estado en auditoría sin duplicar operaciones.

## Preparación

Se utilizó una base nueva, configurada en `application.properties`. Flyway creó las tablas y el seeder cargó los datos.

Escenarios:

- **Ford 2006:** subasta activa con fecha de finalización pasada; oferta ganadora de $120.000 del comprador 2, respaldada por una retención del mismo importe.
- **Toy:** subasta activa vencida y sin ofertas.
- **Nintendo:** subasta activa cuyo comprador líder es el comprador 1, con $45.000 retenidos.

## Procedimiento

1. Iniciar el backend con la base de prueba.
2. Esperar la ejecución del worker.
3. Consultar estados y compradores de Ford y Toy.
4. Consultar los saldos de las billeteras.
5. Consultar los movimientos asociados a Ford.
6. Consultar los eventos de auditoría de ambas subastas.
7. Esperar otros 20 segundos y repetir las consultas para comprobar que no se duplican cierres ni pagos.

## Resultados observados

### Subastas

| Subasta | Estado final | Comprador |
|---|---|---|
| Ford 2006 | FINISHED | buyer2@test.com |
| Toy | UNSOLD | Sin comprador |

### Saldos posteriores al cierre de Ford

Verificados antes del cierre de Nintendo y sin operaciones adicionales.

| Usuario | Total | Retenido | Disponible |
|---|---:|---:|---:|
| buyer1@test.com | 150000 | 45000 | 105000 |
| buyer2@test.com | 80000 | 0 | 80000 |
| vendor@test.com | 120000 | 0 | 120000 |
| noFunds@test.com | 500 | 0 | 500 |

### Movimientos de Ford

| Usuario | Tipo | Importe |
|---|---|---:|
| buyer2@test.com | HOLD | 120000 |
| buyer2@test.com | PAYMENT | -120000 |
| vendor@test.com | CHARGE | 120000 |

### Auditoría

- Ford: evento `STATE_CHANGE`, de `ACTIVE` a `FINISHED`.
- Toy: evento `STATE_CHANGE`, de `ACTIVE` a `UNSOLD`.
- Ambos eventos tienen `user_id` nulo porque fueron ejecutados por el worker.

Al repetir las consultas no se observaron pagos ni cierres duplicados.

## Conclusión y alcance

El cierre con ganador liquidó los fondos y asignó el comprador correctamente. El cierre sin ofertas dejó la subasta desierta. Ambos cambios quedaron registrados en auditoría y la garantía de Nintendo permaneció intacta.
