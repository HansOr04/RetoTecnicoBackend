# Sistema Bancario — Microservicios

Sistema de gestión bancaria compuesto por dos microservicios independientes construidos con Spring Boot 4, Java 17 y Maven. Cada servicio usa su propia base de datos H2 en memoria.

| Servicio | Puerto | Base de datos |
|---|---|---|
| ms-clientes | 8081 | clientesdb (H2) |
| ms-cuentas | 8082 | cuentasdb (H2) |

---

## Requisitos

- Java 17+
- Maven 3.8+
- Docker y Docker Compose (para ejecución en contenedores)

---

## Ejecución con Docker

```bash
# 1. Compilar ambos proyectos
cd ms-clientes && mvn clean package -DskipTests && cd ..
cd ms-cuentas  && mvn clean package -DskipTests && cd ..

# 2. Levantar ambos servicios
docker-compose up --build
```

ms-cuentas esperará a que ms-clientes pase su healthcheck antes de arrancar.

Para detener:
```bash
docker-compose down
```

---

## Ejecución en local

Abrir dos terminales:

```bash
# Terminal 1
cd ms-clientes
mvn spring-boot:run
```

```bash
# Terminal 2
cd ms-cuentas
mvn spring-boot:run
```

Consolas H2 (solo disponibles en perfil local):
- http://localhost:8081/h2-console  (JDBC URL: `jdbc:h2:mem:clientesdb`)
- http://localhost:8082/h2-console  (JDBC URL: `jdbc:h2:mem:cuentasdb`)

---

## Seguridad

- Las contraseñas de clientes se almacenan con hash **BCrypt** — nunca en texto plano.
- Ningún endpoint expone la contraseña en las respuestas (campo ausente en `ClienteResponseDTO`).
- La consola H2 está **deshabilitada** en el perfil Docker/producción (`application-docker.properties`).
- Stack traces ni detalles internos son expuestos en respuestas de error.
- Validación de entrada con `@Valid` en todos los endpoints que reciben body.

---

## Decisiones de diseño

- **clienteId en Cuenta es referencia lógica (String):** cada microservicio tiene su propia base de datos. La consistencia entre ms-clientes y ms-cuentas se garantiza a nivel de aplicación, no con FK de BD, siguiendo el patrón de microservicios.
- **Movimientos sin PUT/DELETE funcional en negocio:** los movimientos bancarios son registros históricos. Los endpoints `PUT /movimientos/{id}` y `DELETE /movimientos/{id}` existen para completar el CRUD requerido, pero en producción deben estar protegidos por autorización.
- **Interfaces de servicio (`IClienteService`, `ICuentaService`, `IMovimientoService`):** los controllers dependen de abstracciones, no de implementaciones concretas, facilitando extensibilidad y pruebas.
- **Excepciones de dominio propias:** `ClienteNoEncontradoException`, `SaldoInsuficienteException`, etc., permiten que el `GlobalExceptionHandler` mapee cada caso a su HTTP status sin depender de strings en los mensajes.

---

## Endpoints — ms-clientes (puerto 8081)

### Listar todos los clientes
```
GET http://localhost:8081/clientes
```

### Obtener cliente por clienteId
```
GET http://localhost:8081/clientes/{clienteId}
```

### Crear cliente
```
POST http://localhost:8081/clientes
Content-Type: application/json

{
  "clienteId": "jose.lema",
  "nombre": "Jose Lema",
  "genero": "M",
  "edad": 35,
  "identificacion": "1234567890",
  "direccion": "Otavalo sn y principal",
  "telefono": "098254785",
  "contrasena": "1234",
  "estado": true
}
```

### Actualizar cliente
```
PUT http://localhost:8081/clientes/{clienteId}
Content-Type: application/json

{ ... mismos campos que POST ... }
```

### Eliminar cliente
```
DELETE http://localhost:8081/clientes/{clienteId}
```

---

## Endpoints — ms-cuentas (puerto 8082)

### Listar todas las cuentas
```
GET http://localhost:8082/cuentas
```

### Obtener cuenta por número
```
GET http://localhost:8082/cuentas/{numeroCuenta}
```

### Obtener cuentas por clienteId
```
GET http://localhost:8082/cuentas/cliente/{clienteId}
```

### Crear cuenta
```
POST http://localhost:8082/cuentas
Content-Type: application/json

{
  "numeroCuenta": "478758",
  "tipoCuenta": "Ahorro",
  "saldoInicial": 2000.0,
  "estado": true,
  "clienteId": "jose.lema"
}
```

### Actualizar cuenta
```
PUT http://localhost:8082/cuentas/{numeroCuenta}
Content-Type: application/json

{ ... mismos campos que POST ... }
```

### Eliminar cuenta
```
DELETE http://localhost:8082/cuentas/{numeroCuenta}
```

### Listar todos los movimientos
```
GET http://localhost:8082/movimientos
```

### Registrar movimiento
```
POST http://localhost:8082/movimientos
Content-Type: application/json

{
  "numeroCuenta": "478758",
  "tipoMovimiento": "Retiro",
  "valor": 575.0
}
```

### Actualizar movimiento (proteger con auth en producción)
```
PUT http://localhost:8082/movimientos/{id}
```

### Eliminar movimiento (proteger con auth en producción)
```
DELETE http://localhost:8082/movimientos/{id}
```

### Reporte por cliente y rango de fechas
```
GET http://localhost:8082/reportes?clienteId=jose.lema&fechaInicio=2024-01-01&fechaFin=2024-01-31
```

---

## Casos de uso resueltos

### Clientes del enunciado

| Cliente | clienteId | Contraseña (plain) | Estado |
|---|---|---|---|
| Jose Lema | jose.lema | 1234 | true |
| Marianela Montalvo | marianela.montalvo | 5678 | true |
| Juan Osorio | juan.osorio | 1245 | true |

### Cuentas del enunciado

| Número | Tipo | Saldo inicial | Cliente |
|---|---|---|---|
| 478758 | Ahorro | 2000.00 | jose.lema |
| 225487 | Corriente | 100.00 | marianela.montalvo |
| 495878 | Ahorro | 0.00 | marianela.montalvo |
| 496825 | Ahorro | 540.00 | juan.osorio |
| 585545 | Corriente | 1000.00 | jose.lema |

### Movimientos y saldos resultantes

**Cuenta 478758 — Jose Lema (Ahorro, saldo inicial: 2000.00)**
```
POST /movimientos  { "numeroCuenta": "478758", "tipoMovimiento": "Retiro",   "valor": 575.0  } → saldo: 1425.00
POST /movimientos  { "numeroCuenta": "478758", "tipoMovimiento": "Deposito", "valor": 600.0  } → saldo: 2025.00
POST /movimientos  { "numeroCuenta": "478758", "tipoMovimiento": "Retiro",   "valor": 2025.0 } → saldo: 0.00
```

**Cuenta 225487 — Marianela Montalvo (Corriente, saldo inicial: 100.00)**
```
POST /movimientos  { "numeroCuenta": "225487", "tipoMovimiento": "Retiro", "valor": 600.0 }
→ 400 Bad Request: "Saldo no disponible"
```

**Cuenta 495878 — Marianela Montalvo (Ahorro, saldo inicial: 0.00)**
```
POST /movimientos  { "numeroCuenta": "495878", "tipoMovimiento": "Deposito", "valor": 150.0 } → saldo: 150.00
```

**Cuenta 496825 — Juan Osorio (Ahorro, saldo inicial: 540.00)**
```
POST /movimientos  { "numeroCuenta": "496825", "tipoMovimiento": "Retiro", "valor": 540.0 } → saldo: 0.00
```

**Cuenta 585545 — Jose Lema (Corriente, saldo inicial: 1000.00)**
```
POST /movimientos  { "numeroCuenta": "585545", "tipoMovimiento": "Deposito", "valor": 1000.0 } → saldo: 2000.00
POST /movimientos  { "numeroCuenta": "585545", "tipoMovimiento": "Retiro",   "valor": 1500.0 } → saldo: 500.00
```

### Reporte de estado de cuenta
```
GET /reportes?clienteId=jose.lema&fechaInicio=2024-01-01&fechaFin=2024-12-31
```

Respuesta (ejemplo):
```json
[
  {
    "fecha": "2024-01-10T10:00:00",
    "cliente": "jose.lema",
    "numeroCuenta": "478758",
    "tipoCuenta": "Ahorro",
    "saldoInicial": 2000.0,
    "estado": true,
    "movimiento": -575.0,
    "saldoDisponible": 1425.0
  }
]
```

---

## Manejo de errores

```json
{
  "timestamp": "2024-01-10T10:00:00.000",
  "status": 400,
  "message": "Saldo no disponible",
  "errors": {}
}
```

| Situación | Status |
|---|---|
| Campos inválidos en el body | 400 |
| Saldo insuficiente para retiro | 400 |
| Tipo de movimiento inválido | 400 |
| Formato de fecha inválido | 400 |
| Cliente / Cuenta no encontrado | 404 |
| Cliente / Cuenta ya registrado | 409 |
| Cuenta con movimientos (al eliminar) | 409 |
| Modificación concurrente (optimistic lock) | 409 |
| Error interno del servidor | 500 |

---

## Consideraciones de arquitectura para producción

### Transaccionalidad
Todos los métodos de escritura están anotados con `@Transactional`.
El registro de movimientos (`registrar()`) es atómico: si la persistencia
del movimiento falla, el cambio de saldo en la cuenta se revierte automáticamente.

### Escalabilidad
- **Paginación:** los endpoints de lista deben implementar `Pageable` para
  evitar cargar datasets completos en memoria.
- **Índices de BD:** los campos `clienteId` (Cuenta), `numeroCuenta` (Cuenta)
  y `clienteId+fecha` (Movimiento) tienen índices declarados con `@Index` para
  consultas eficientes con volumen de datos real.
- **Pool de conexiones:** HikariCP debe configurarse con `maximumPoolSize`
  apropiado según la carga esperada.

### Concurrencia
La entidad `Cuenta` implementa optimistic locking con `@Version` para
prevenir condiciones de carrera en retiros simultáneos sobre la misma cuenta.
Hibernate lanza `OptimisticLockException` si dos transacciones concurrentes
modifican el mismo registro, retornando HTTP 409.

### Resiliencia
En producción se recomienda:
- Circuit breaker (Resilience4j) para llamadas entre microservicios
- Retry automático con backoff exponencial
- Timeouts configurados en clientes HTTP

### Comunicación entre microservicios
Actualmente ms-cuentas referencia `clienteId` como string lógico sin
validación en ms-clientes. La evolución arquitectural propuesta es:
- **Corto plazo:** llamada HTTP síncrona con `RestClient` al crear una cuenta
- **Largo plazo:** eventos asíncronos con RabbitMQ/Kafka para consistencia eventual
  (patrón Saga) garantizando que no existan cuentas con `clienteId`s huérfanos.

### Seguridad de API (trabajo pendiente)
Los endpoints actualmente no requieren autenticación. En producción:
- Spring Security + JWT para autenticación stateless
- Roles: `ADMIN` (CRUD completo), `USER` (solo lectura y movimientos propios)
- Rate limiting para prevenir abuso de endpoints

### Tipos de datos monetarios
Los campos de dinero usan `BigDecimal` con escala de 2 decimales (`precision=15, scale=2`)
para evitar errores de precisión en aritmética de punto flotante binario.
Ejemplo: `100.10 - 0.10 = 100.00` exacto con `BigDecimal`; con `Double` daría `99.99999999999999`.
