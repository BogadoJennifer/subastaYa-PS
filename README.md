# SubastaYa

SubastaYa es una plataforma web de subastas en tiempo real y comercio electrónico que
busca modernizar las compras y ventas competitivas en línea.
El objetivo del equipo es crear un sistema robusto y escalable que permita a los usuarios participar
en subastas de manera eficiente y segura.

## Descripción

Este proyecto consiste en una aplicación desarrollada en **Java** utilizando **Spring Boot** y **arquitectura MVC** (https://developer.mozilla.org/es/docs/Glossary/MVC).

El objetivo es desarrollar una aplicación que permita un entorno basado en dos pilares criticos:

1. Confianza y Solvencia Económica

2. Juego Limpio (Fair Play en Subastas)

## Tecnologías utilizadas

* Java
* Git y GitHub
* Spring Boot
* Spring Data JPA e Hibernate
* React
* Gradle
* Spring WebSocket
* IntelliJ IDEA
* PostgreSQL
* flyway
* Bruno
* Mockito

## Arquitectura

El proyecto utiliza **arquitectura MVC**, SubastaYa tiene un frontend React y un backend Spring Boot. El backend se organiza principalmente
por capas técnicas: controladores, servicios, repositorios y entidades. Persiste información en PostgreSQL mediante JPA y utiliza Flyway para 
aplicar cambios de esquema.

Para una explicación más detallada de la arquitectura, consultar:
`backend/docs`

La raíz del repositorio separa los componentes de la aplicación:

```text
backend/   # API Spring Boot y su proyecto Gradle
frontend/  # aplicación React/Vite
docs/      # documentación técnica y colecciones de prueba
```

Los archivos Gradle de la raíz coordinan el módulo `backend`, para que el repositorio
pueda abrirse y ejecutarse como un único proyecto desde el IDE.

## Documentación

La documentación técnica del proyecto se encuentra dentro de la carpeta `docs`.

```text
docs/
├── architecture.md
├── api-spec.md
├── decisions/
└── collections/
```

### Arquitectura

`backend/docs`

Contiene la explicación de la arquitectura utilizada y de la organización de los componentes del sistema.

### API

`backend/docs`

Contiene la especificación de los endpoints de la API, sus parámetros, respuestas y posibles errores.

### Decisiones técnicas

`backend/docs`

Contiene las decisiones técnicas importantes tomadas durante el desarrollo del proyecto y los motivos que llevaron a cada decisión.
Basandonos en el patrón de RFC (Request for Comments) para documentar las decisiones técnicas.

### Bruno

`backend/docs`

Contiene las colecciones y configuraciones de Bruno utilizadas para probar la API.

## Requisitos

Para ejecutar el proyecto se necesita tener instalado:

* Java 21
* Spring Boot 4.1.1
* Spring Data JPA
* Spring MVC
* Gradle
* PostgreSQL
* Flyway
* Spring WebSocket
* Springdoc OpenAPI UI 3.1.1
* React (para el frontend)
* IntelliJ IDEA (opcional)

## Configuración

1. Clonar el repositorio.

2. Abrir la raíz del repositorio en IntelliJ IDEA (opcional) y recargar el proyecto Gradle. El módulo `backend` se importará automáticamente.

3. Configurar las variables de entorno necesarias.

4. Configurar la conexión con la base de datos.

5. Abrir una terminal y acceder a la carpeta del backend (`cd backend`). Ejecutar `gradlew.bat bootRun` en Windows, o `./gradlew bootRun` en macOS/Linux.

6. Abrir terminal y acceder a la carpeta del frontend (cd frontend).

7. Ejecutar el comando `npm run dev` para iniciar el servidor de desarrollo de React.

## Ejecución

Una vez iniciados ambos servidores, la aplicación web estará disponible en:

```text
http://localhost:5173/
```

La API continúa disponible en `http://localhost:8080/`. El proxy configurado en
`frontend/vite.config.js` conserva la comunicación del frontend con la API y el WebSocket.

## Autores

* Jennifer Bogado
* Braian Martín Leuno

