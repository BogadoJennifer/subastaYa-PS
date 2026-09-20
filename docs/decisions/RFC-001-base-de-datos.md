# RFC-001: Elección de base de datos

- **Estado:** Aceptado
- **Fecha:** 01/09/2026

##  Problema
Necesitamos seleccionar una base de datos para el proyecto.

Alternativas:
- PostgreSQL
- MySQL
- H2

## Decisión
Utilizar PostgreSQL.

## Motivación:
1. **Garantías ACID:** El modelo financiero exige aislamiento transaccional para evitar inconsistencias de saldo durante pujas concurrentes.
2. **Tipado y Precisión Monetaria:** Soporte nativo para el tipo `NUMERIC` / `DECIMAL`, mapeado con `java.math.BigDecimal` para prevenir errores de redondeo de punto flotante.
3. **Bloqueo a Nivel de Fila:** Permite el uso de cláusulas `SELECT ... FOR UPDATE` para sincronizar operaciones sobre una misma billetera

## Consecuencias:
Dependencia de PostgreSQL, necesidad de migraciones reproducibles y alineación entre entidades y DDL.
