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

Consolas H2:
- http://localhost:8081/h2-console  (JDBC URL: `jdbc:h2:mem:clientesdb`)
- http://localhost:8082/h2-console  (JDBC URL: `jdbc:h2:mem:cuentasdb`)

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

{
  "clienteId": "jose.lema",
  "nombre": "Jose Lema",
  "genero": "M",
  "edad": 36,
  "identificacion": "1234567890",
  "direccion": "Otavalo sn y principal",
  "telefono": "098254785",
  "contrasena": "5678",
  "estado": true
}
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

{
  "numeroCuenta": "478758",
  "tipoCuenta": "Ahorro",
  "saldoInicial": 2000.0,
  "estado": true,
  "clienteId": "jose.lema"
}
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

### Reporte por cliente y rango de fechas
```
GET http://localhost:8082/reportes?clienteId=jose.lema&fechaInicio=2024-01-01&fechaFin=2024-01-31
```

---

## Casos de uso resueltos

Los siguientes escenarios replican los datos del enunciado del reto técnico.

### Configuración inicial de clientes

| Cliente | clienteId | Contraseña | Estado |
|---|---|---|---|
| Jose Lema | jose.lema | 1234 | true |
| Marianela Montalvo | marianela.montalvo | 5678 | true |
| Juan Osorio | juan.osorio | 1245 | true |

### Configuración inicial de cuentas

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
POST /movimientos  { "numeroCuenta": "478758", "tipoMovimiento": "Retiro",  "valor": 575.0 }
→ saldo resultante: 1425.00

POST /movimientos  { "numeroCuenta": "478758", "tipoMovimiento": "Deposito", "valor": 600.0 }
→ saldo resultante: 2025.00

POST /movimientos  { "numeroCuenta": "478758", "tipoMovimiento": "Retiro",  "valor": 2025.0 }
→ saldo resultante: 0.00
```

**Cuenta 225487 — Marianela Montalvo (Corriente, saldo inicial: 100.00)**
```
POST /movimientos  { "numeroCuenta": "225487", "tipoMovimiento": "Retiro", "valor": 600.0 }
→ 400 Bad Request: "Saldo no disponible"
```

**Cuenta 495878 — Marianela Montalvo (Ahorro, saldo inicial: 0.00)**
```
POST /movimientos  { "numeroCuenta": "495878", "tipoMovimiento": "Deposito", "valor": 150.0 }
→ saldo resultante: 150.00
```

**Cuenta 496825 — Juan Osorio (Ahorro, saldo inicial: 540.00)**
```
POST /movimientos  { "numeroCuenta": "496825", "tipoMovimiento": "Retiro", "valor": 540.0 }
→ saldo resultante: 0.00
```

**Cuenta 585545 — Jose Lema (Corriente, saldo inicial: 1000.00)**
```
POST /movimientos  { "numeroCuenta": "585545", "tipoMovimiento": "Deposito", "valor": 1000.0 }
→ saldo resultante: 2000.00

POST /movimientos  { "numeroCuenta": "585545", "tipoMovimiento": "Retiro",  "valor": 1500.0 }
→ saldo resultante: 500.00
```

### Reporte de estado de cuenta
```
GET /reportes?clienteId=jose.lema&fechaInicio=2024-01-01&fechaFin=2024-12-31
```

Respuesta esperada (ejemplo):
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

Todas las respuestas de error siguen el mismo formato:

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
| Cliente / Cuenta no encontrado | 404 |
| Error interno del servidor | 500 |
