# ComuniGest

ComuniGest es una aplicación web orientada a la gestión de conserjería de condominios. Permite administrar usuarios, perfiles, departamentos, residentes, turnos, visitas, encomiendas, incidencias y reportes operativos.

## Tecnologías utilizadas

### Frontend

- Angular 21
- TypeScript
- Formularios reactivos
- HttpClient
- CSS

### Backend

- Java 17
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Jakarta Validation
- Maven

### Base de datos

- H2 para desarrollo local.
- Compatibilidad preparada para MySQL.
- MySQL como base de datos definitiva del proyecto.

## Estructura del proyecto

```text
ComuniGest/
├── backend/
│   ├── src/main/java/
│   ├── src/main/resources/
│   └── pom.xml
├── frontend/
│   ├── src/app/
│   ├── src/environments/
│   └── package.json
├── comunigest_mysql_schema.sql
└── README.md
```

## Funcionalidades principales

- Inicio y cierre de sesión.
- Gestión de perfiles.
- Gestión de usuarios.
- Registro de departamentos.
- Registro de residentes.
- Inicio y cierre de turnos.
- Registro de ingreso y salida de visitas.
- Registro y entrega de encomiendas.
- Registro y cierre de incidencias.
- Consulta de reportes generales.

## Arquitectura

El backend utiliza una arquitectura por capas:

- `controller`
- `service`
- `repository`
- `entity`
- `dto`
- `config`

El frontend organiza sus componentes en las siguientes áreas:

- `core`
- `features`
- `environments`

La comunicación entre el frontend y el backend se realiza mediante una API REST.

## Requisitos previos

Para ejecutar el proyecto se requiere:

- Java 17
- Maven
- Node.js
- npm
- Angular CLI o ejecución mediante `npx`
- MySQL para la configuración definitiva

## Ejecución del backend

Desde la carpeta principal del proyecto:

```bash
cd backend
mvn spring-boot:run
```

El backend quedará disponible en:

```text
http://localhost:8080
```

La ruta base de la API es:

```text
http://localhost:8080/api
```

Durante el desarrollo local se utiliza una base de datos H2 almacenada en la carpeta `backend/data`.

Esta carpeta está excluida del repositorio mediante `.gitignore`.

La consola de H2 está disponible en:

```text
http://localhost:8080/h2-console
```

## Ejecución del frontend

Desde otra terminal:

```bash
cd frontend
npm install
npx ng serve
```

En Windows PowerShell puede utilizarse:

```powershell
npx.cmd ng serve
```

La aplicación quedará disponible en:

```text
http://localhost:4200
```

La URL de conexión con el backend está configurada en:

```text
frontend/src/environments/environment.ts
```

## Endpoints principales

### Autenticación

```text
POST /api/auth/login
```

### Perfiles

```text
GET    /api/perfiles
POST   /api/perfiles
PUT    /api/perfiles/{id}
DELETE /api/perfiles/{id}
```

### Usuarios

```text
GET    /api/usuarios
POST   /api/usuarios
PUT    /api/usuarios/{id}
DELETE /api/usuarios/{id}
```

### Departamentos

```text
GET    /api/departamentos
POST   /api/departamentos
PUT    /api/departamentos/{id}
DELETE /api/departamentos/{id}
```

### Residentes

```text
GET    /api/residentes
POST   /api/residentes
PUT    /api/residentes/{id}
DELETE /api/residentes/{id}
```

### Turnos

```text
GET    /api/turnos
POST   /api/turnos
PUT    /api/turnos/{id}
DELETE /api/turnos/{id}
PATCH  /api/turnos/{id}/cerrar
```

### Visitas

```text
GET    /api/visitas
POST   /api/visitas
PUT    /api/visitas/{id}
DELETE /api/visitas/{id}
PATCH  /api/visitas/{id}/salida
```

### Encomiendas

```text
GET    /api/encomiendas
POST   /api/encomiendas
PUT    /api/encomiendas/{id}
DELETE /api/encomiendas/{id}
PATCH  /api/encomiendas/{id}/entregar
```

### Incidencias

```text
GET    /api/incidencias
POST   /api/incidencias
PUT    /api/incidencias/{id}
DELETE /api/incidencias/{id}
PATCH  /api/incidencias/{id}/cerrar
```

### Reportes

```text
GET /api/reportes/resumen
```

## Estado actual del proyecto

Actualmente se encuentran operativas las siguientes funciones:

- Ejecución del backend en el puerto `8080`.
- Ejecución del frontend en el puerto `4200`.
- Comunicación entre Angular y Spring Boot.
- Inicio de sesión local.
- Gestión básica mediante operaciones CRUD.
- Consulta del resumen de reportes.
- Persistencia local mediante H2.

El proyecto contempla como próximos avances:

- Implementación definitiva con MySQL.
- Cifrado seguro de contraseñas.
- Autenticación mediante tokens válidos.
- Control de acceso según perfil.
- Mejora de formularios y tablas.
- Incorporación de validaciones adicionales.
- Ejecución de casos de prueba.

## Base de datos MySQL

El archivo:

```text
comunigest_mysql_schema.sql
```

contiene la estructura SQL diseñada para la implementación definitiva con MySQL.

La base de datos prevista es:

```text
comunigest_db
```

## Autora

Dayanna Neculqueo