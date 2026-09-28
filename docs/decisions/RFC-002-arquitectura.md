# RFC-002 — Arquitectura

- **Estado:** Aceptado
- **Fecha:** 05/09/2026
- **Ámbito:** Organización general del backend y frontend de SubastaYa.

##  Problema

El sistema necesita separar responsabilidades entre interfaz, API, lógica de negocio y persistencia.

Además, algunas funcionalidades requieren actualización en tiempo real, especialmente las salas de subastas.

##  Decisión

Se utilizará una arquitectura por capas:

```text
Frontend React
      |
      v
Controllers / API
      |
      v
Services
      |
      v
Repositories
      |
      v
JPA / Base de datos
```

El frontend se implementa con React.

El backend se implementa con Spring Boot.

##  Responsabilidades

### Controller

Responsable de:

- recibir solicitudes HTTP;
- validar aspectos básicos del contrato HTTP;
- delegar operaciones al Service;
- construir la respuesta HTTP.

El Controller no debe contener lógica de negocio compleja.

### Service

Responsable de:

- reglas de negocio;
- validaciones funcionales;
- operaciones transaccionales;
- coordinación entre repositorios;
- actualización de entidades relacionadas.


##  Consecuencias

La separación permite:

- probar la lógica de negocio independientemente de los Controllers;
- reducir duplicación;
- cambiar la persistencia sin modificar el contrato HTTP;
- mantener una estructura clara para el crecimiento del proyecto.

Como contrapartida, aumenta la cantidad de clases y capas que deben mantenerse coordinadas.
