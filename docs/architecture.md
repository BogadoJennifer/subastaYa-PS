# Arquitectura del proyecto

## 1. Introducción

El proyecto utiliza el patrón arquitectónico **MVC (Model–View–Controller)** para organizar los diferentes componentes de la aplicación.

El objetivo de utilizar MVC es separar las responsabilidades del sistema en diferentes componentes, facilitando la organización, el mantenimiento y la evolución del código.

La arquitectura divide la aplicación principalmente en:

* **Model:** representa los datos y la lógica relacionada con ellos.
* **View:** representa la información que se muestra al usuario.
* **Controller:** recibe las solicitudes y coordina la interacción entre el modelo y la vista.

---

## 2. Patrón MVC

El patrón MVC divide la aplicación en tres componentes principales.

```text
                ┌───────────────┐
                │     Usuario   │
                └───────┬───────┘
                        │
                        ▼
                ┌───────────────┐
                │  Controller   │
                └───────┬───────┘
                        │
                        ▼
                ┌───────────────┐
                │     Model     │
                └───────┬───────┘
                        │
                        ▼
                ┌───────────────┐
                │     View      │
                └───────────────┘
```

La separación de responsabilidades permite que cada componente tenga una función específica dentro de la aplicación.

---

## 3. Componentes

### 3.1 Model

El **Model** representa los datos y las reglas relacionadas con el funcionamiento de la aplicación.

Dependiendo de las características del proyecto, puede contener:

* Entidades.
* Clases de dominio.
* Objetos utilizados para representar información.
* Repositorios.
* Lógica relacionada con los datos.

Por ejemplo, si el sistema administra usuarios, podría existir una clase:

```text
Usuario
```

que represente los datos de un usuario dentro del sistema.

---

### 3.2 View

La **View** es responsable de presentar la información al usuario.

En una aplicación web tradicional puede estar formada por páginas HTML, plantillas u otros componentes de presentación.

La View no debería contener la lógica principal del negocio. Su responsabilidad es mostrar la información recibida y permitir la interacción del usuario.

> Si la aplicación funciona exclusivamente como una API REST y no posee una interfaz gráfica propia, esta responsabilidad puede estar representada por las respuestas HTTP que consume un cliente externo.

---

### 3.3 Controller

El **Controller** recibe las solicitudes realizadas por el usuario o por un cliente externo y coordina la respuesta.

En una aplicación desarrollada con Spring Boot, los controladores pueden utilizar anotaciones como:

```java
@RestController
```

y definir diferentes endpoints mediante anotaciones como:

```java
@GetMapping
@PostMapping
@PutMapping
@DeleteMapping
```


El Controller recibe la solicitud, procesa los parámetros necesarios y coordina la operación correspondiente.

---

## 4. Flujo de una solicitud

Un flujo típico de una solicitud puede representarse de la siguiente manera:

```text
Cliente
   │
   │ HTTP Request
   ▼
Controller
   │
   │ solicita/procesa información
   ▼
Model
   │
   │ obtiene o modifica datos
   ▼
Controller
   │
   │ HTTP Response
   ▼
Cliente
```


## 5. Organización del proyecto

La organización concreta de los paquetes debe reflejar las responsabilidades definidas por MVC:

```text
src/
└── main/
    └── java/
        └── unaj.subastaya/
            ├── controller/
            ├── model/
            ├── repository/
            └── service/
```

### `controller/`

Contiene los controladores responsables de recibir y responder solicitudes.

### `model/`

Contiene las clases que representan los datos utilizados por la aplicación.

### `service/`

Contiene la lógica de aplicación o de negocio.

### `repository/`

Contiene los componentes encargados de acceder a los datos almacenados.


---

## 6. Responsabilidades

Para mantener una separación clara, cada componente debe concentrarse en su responsabilidad.

| Componente | Responsabilidad                           |
| ---------- | ----------------------------------------- |
| Controller | Recibir solicitudes y devolver respuestas |
| Model      | Representar los datos del sistema         |
| Service    | Procesar la lógica de la aplicación       |
| Repository | Acceder a los datos                       |
| View       | Presentar información al usuario          |


