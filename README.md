# ComuniGest

ComuniGest es una aplicación web para la gestión administrativa y operativa de la conserjería de condominios. El sistema permite controlar usuarios, perfiles, departamentos, residentes, turnos, visitas, encomiendas, incidencias y reportes, con acceso diferenciado según el tipo de usuario.

## Tecnologías utilizadas

### Frontend

- Angular 21
- TypeScript
- Formularios reactivos
- HttpClient
- Guards de navegación
- Interceptor HTTP
- CSS personalizado

### Backend

- Java 17
- Spring Boot 3.3.5
- Spring Web
- Spring Data JPA
- Jakarta Validation
- Spring Security
- BCrypt
- JWT con JJWT 0.12.6
- Maven

### Base de datos

- H2 para desarrollo y demostración local
- Esquema preparado para MySQL
- Archivo `comunigest_mysql_schema.sql`
- Persistencia local almacenada en `backend/data`

## Estructura del proyecto

```text
ComuniGest/
├── backend/
│   ├── src/main/java/
│   ├── src/main/resources/
│   ├── src/test/java/
│   ├── data/
│   └── pom.xml
├── frontend/
│   ├── src/app/
│   ├── src/environments/
│   └── package.json
├── comunigest_mysql_schema.sql
└── README.md
```

La carpeta `backend/data` contiene la base de datos H2 local y se encuentra excluida del repositorio mediante `.gitignore`.

## Funcionalidades principales

- Inicio y cierre de sesión
- Autenticación mediante JWT
- Contraseñas cifradas con BCrypt
- Control de acceso según perfil
- Gestión de usuarios
- Catálogo fijo de perfiles
- Gestión de departamentos
- Gestión de residentes
- Inicio y cierre de turnos
- Registro de ingreso y salida de visitas
- Registro y entrega de encomiendas
- Registro, gestión y resolución de incidencias
- Consulta de reportes operativos
- Validaciones en frontend y backend
- Protección de rutas y endpoints
- Rechazo de tokens inválidos, manipulados o vencidos
- Revocación de acceso para usuarios inactivos

## Perfiles de usuario

### Administrador

El perfil `ADMINISTRADOR` dispone de acceso a:

- Reportes
- Perfiles
- Usuarios
- Departamentos
- Residentes
- Historial de turnos
- Historial de visitas
- Historial de encomiendas
- Gestión y resolución de incidencias

El administrador puede consultar las operaciones registradas, pero no puede iniciar turnos ni ejecutar acciones propias del conserje.

### Conserje

El perfil `CONSERJE` dispone de acceso a:

- Inicio y cierre de su propio turno
- Registro de ingreso y salida de visitas
- Registro de recepción y entrega de encomiendas
- Registro de incidencias
- Consultas operativas autorizadas

El conserje no puede acceder a los módulos administrativos de usuarios, perfiles, reportes, departamentos ni residentes.

## Arquitectura

### Backend

El backend utiliza una arquitectura por capas:

- `controller`: exposición de la API REST
- `service`: lógica de negocio y validaciones
- `repository`: acceso a datos mediante Spring Data JPA
- `entity`: representación de las entidades
- `dto`: objetos de solicitud y respuesta
- `config`: inicialización y configuración del sistema
- `security`: autenticación JWT y autorización por perfiles

### Frontend

El frontend se organiza en:

- `core`: autenticación, guards, interceptor y utilidades JWT
- `features`: módulos funcionales
- `environments`: configuración de conexión con la API

La comunicación entre Angular y Spring Boot se realiza mediante una API REST.

## Seguridad

ComuniGest incorpora las siguientes medidas de seguridad:

- Cifrado de contraseñas mediante BCrypt
- Migración idempotente de contraseñas antiguas
- Autenticación mediante JWT firmado
- Sesiones con una duración de 8 horas
- API configurada como `STATELESS`
- Validación de firma y vencimiento del token
- Validación del estado activo del usuario en cada solicitud
- Autorización mediante `ROLE_ADMINISTRADOR` y `ROLE_CONSERJE`
- Identidad obtenida desde `SecurityContext`
- Prevención de suplantación mediante identificadores enviados desde el cliente
- Respuestas diferenciadas para errores `401` y `403`
- CORS habilitado para `http://localhost:4200`
- Consola H2 habilitada solo para desarrollo local
- Contraseñas y hashes excluidos de las respuestas al frontend

## Configuración JWT

El secreto JWT puede configurarse mediante la variable de entorno:

```text
COMUNIGEST_JWT_SECRET
```

Ejemplo en Windows PowerShell:

```powershell
$env:COMUNIGEST_JWT_SECRET="reemplazar-por-un-secreto-seguro"
```

El proyecto incluye un valor demostrativo para facilitar la ejecución académica local. Para un entorno productivo debe definirse un secreto seguro mediante variable de entorno.

Características del token:

- Emisor: `ComuniGest`
- Duración: 8 horas
- Identificación del usuario
- Perfil del usuario
- Fecha de emisión
- Fecha de vencimiento
- Validación de firma
- Validación del estado activo de la cuenta

## Requisitos previos

Para ejecutar el proyecto se requiere:

- Java 17
- Maven
- Node.js
- npm
- Angular CLI o ejecución mediante `npx`

MySQL solo es necesario para realizar la migración definitiva desde H2.

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

## Ejecución del frontend

Desde otra terminal:

```bash
cd frontend
npm install
npx ng serve
```

En Windows PowerShell:

```powershell
npx.cmd ng serve
```

La aplicación quedará disponible en:

```text
http://localhost:4200
```

La conexión con el backend está configurada en:

```text
frontend/src/environments/environment.ts
```

## Base de datos H2

Durante el desarrollo y la demostración local se utiliza una base de datos H2 persistente.

Los archivos se almacenan en:

```text
backend/data
```

La consola H2 está disponible en:

```text
http://localhost:8080/h2-console
```

La carpeta `backend/data` no se incluye en el repositorio.

Al ejecutar el proyecto con una base nueva, el backend inicializa los perfiles y las cuentas académicas necesarias para la evaluación.

## Rutas principales de la API

### Autenticación

```text
POST /api/auth/login
```

El inicio de sesión es público. El resto de la API requiere un JWT válido.

### Perfiles

```text
GET /api/perfiles
GET /api/perfiles/{id}
```

Acceso exclusivo para `ADMINISTRADOR`.

Los perfiles forman un catálogo fijo de solo lectura. Las operaciones de creación, modificación y eliminación están bloqueadas con HTTP `405`.

### Usuarios

Ruta base:

```text
/api/usuarios
```

Acceso exclusivo para `ADMINISTRADOR`.

Permite consultar, crear, editar, activar y desactivar usuarios.

### Departamentos

Ruta base:

```text
/api/departamentos
```

El administrador puede gestionar departamentos. El conserje dispone únicamente de las consultas auxiliares necesarias para registrar operaciones.

### Residentes

Ruta base:

```text
/api/residentes
```

Acceso administrativo para la gestión de residentes.

### Turnos

Ruta base:

```text
/api/turnos
```

Permite iniciar y cerrar el turno propio del conserje, consultar turnos e informar el historial al administrador.

### Visitas

Ruta base:

```text
/api/visitas
```

Permite registrar ingresos, registrar salidas, consultar visitas activas y revisar el historial administrativo.

### Encomiendas

```text
POST  /api/encomiendas/recepcion
GET   /api/encomiendas
GET   /api/encomiendas/pendientes
GET   /api/encomiendas/mes-actual
PATCH /api/encomiendas/{id}/entregar
```

Las operaciones de recepción y entrega requieren un conserje autenticado con turno abierto.

### Incidencias

```text
POST  /api/incidencias/registro
GET   /api/incidencias
GET   /api/incidencias/mes-actual
PATCH /api/incidencias/{id}/iniciar-gestion
PATCH /api/incidencias/{id}/resolver
```

El conserje registra incidencias con turno abierto. El administrador puede iniciar su gestión y resolverlas.

### Reportes

```text
GET /api/reportes/resumen
```

Acceso exclusivo para `ADMINISTRADOR`.

El resumen contiene:

- residentes activos
- turnos abiertos
- visitas dentro
- encomiendas pendientes
- incidencias abiertas

## Pruebas

El backend incluye pruebas automatizadas para:

- migración de contraseñas a BCrypt
- creación y cambio de contraseñas
- conservación de contraseña al editar sin una nueva clave
- inicio de sesión correcto
- rechazo de credenciales incorrectas
- rechazo de usuarios inactivos
- generación y validación de JWT
- token ausente
- token inválido
- token manipulado
- token vencido
- control de permisos por perfil
- bloqueo de rutas administrativas
- revocación de acceso para usuarios inactivos
- prevención de suplantación mediante `usuarioId`
- inicio y cierre de turnos
- gestión y resolución de incidencias

Para ejecutar las pruebas:

```bash
cd backend
mvn test
```

Para compilar el backend:

```bash
cd backend
mvn package
```

Para compilar el frontend:

```bash
cd frontend
npm run build
```

En Windows PowerShell:

```powershell
npm.cmd run build
```

## Estado actual del proyecto

Actualmente se encuentran implementados:

- Backend operativo en el puerto `8080`
- Frontend operativo en el puerto `4200`
- Comunicación mediante API REST
- Persistencia local mediante H2
- Inicio y cierre de sesión
- Contraseñas cifradas con BCrypt
- Autenticación mediante JWT
- Control de acceso en backend
- Perfiles diferenciados
- Navegación protegida
- Gestión de usuarios
- Gestión de departamentos
- Gestión de residentes
- Turnos
- Visitas
- Encomiendas
- Incidencias
- Reportes
- Validaciones en frontend y backend
- Pruebas automatizadas de autenticación y seguridad
- Esquema SQL preparado para MySQL

## Mejoras futuras

- Migración definitiva desde H2 a MySQL
- Despliegue en hosting
- Configuración HTTPS
- Recuperación de contraseña mediante correo electrónico
- Implementación de refresh tokens
- Uso de cookies `HttpOnly`
- Limitación de intentos de inicio de sesión
- Rate limiting
- Registro de auditoría
- Ampliación de las pruebas automatizadas
- Mejoras visuales y de experiencia de usuario

## Base de datos MySQL

El archivo:

```text
comunigest_mysql_schema.sql
```

contiene la estructura SQL preparada para MySQL.

La base de datos prevista es:

```text
comunigest_db
```

El esquema incluye:

- perfiles
- usuarios
- departamentos
- residentes
- turnos
- visitas
- encomiendas
- incidencias
- restricciones
- relaciones
- cuentas académicas iniciales con contraseñas BCrypt

## Acceso de evaluación

Las credenciales correspondientes a los perfiles `ADMINISTRADOR` y `CONSERJE` se encuentran en el informe de entrega de la evaluación.

## Autora

Dayanna Neculqueo