# Sistema de Gestión y Procesamiento de Reservas

Aplicación de consola desarrollada en **Java** y conectada a una base de datos **MySQL** para gestionar, importar, filtrar y consultar reservas de vuelos. El sistema incluye un control de acceso por roles (Usuario / Administrador) y un registro histórico automático de todas las operaciones realizadas.

---

## 📋 Requisitos Previos

Para poner en marcha este proyecto en tu equipo desde cero, solo necesitas tener instalado:

1. **Docker Desktop** (incluye Docker y Docker Compose) -> [Descargar aquí](https://www.docker.com/products/docker-desktop/).
2. **Java JDK 8 o superior** -> Asegúrate de contar con el conector JDBC de MySQL (`mysql-connector-j`) añadido a las dependencias de tu proyecto.
3. Un entorno de desarrollo como **IntelliJ IDEA**, **Eclipse** o **VS Code**.

---

## 🐳 Paso 1: Levantar la Base de Datos con Docker

No necesitas instalar ni configurar MySQL manualmente en tu ordenador. El proyecto incluye un entorno automatizado que prepara toda la base de datos con un solo comando:

1. Asegúrate de tener abiertos los servicios de **Docker Desktop** en tu equipo.
2. Abre una terminal en la carpeta donde se encuentran los archivos `compose.yaml` e `init.sql`.
3. Ejecuta el siguiente comando para construir y levantar la base de datos en segundo plano:
   ```bash
   docker compose up -d
   ```
4. Espera unos segundos mientras Docker inicia el servicio (el archivo `compose.yaml` incluye un *healthcheck* que verifica automáticamente cuando la base de datos está lista).

> **🔑 Credenciales creadas automáticamente al iniciar:**
> * **Usuario Administrador:** `admin`
> * **Contraseña:** `1234`

---

## 📂 Paso 2: Estructura de Carpetas del Proyecto

Asegúrate de respetar la siguiente organización de directorios dentro de tu proyecto para que la importación masiva desde CSV funcione correctamente:

```text
tu-proyecto/
│
├── data/
│   └── reservas.csv          <-- Archivo CSV de entrada con las reservas iniciales
│
├── compose.yaml              <-- Configuración del contenedor Docker de MySQL
├── init.sql                  <-- Script automático que crea tablas y el usuario admin
└── Main.java                 <-- Código fuente principal de la aplicación
```

### Formato del archivo `./data/reservas.csv`
El archivo CSV debe contener una cabecera en la primera línea y los datos separados por comas con el siguiente orden de columnas:

```csv
origen,destino,compañia,fecha,nombrePasajero
MAD,BCN,Iberia,2026-06-15,Juan Pérez
PMI,MAD,Air Europa,2026-07-20,María García
```

---

## 💻 Paso 3: Configurar y Ejecutar el Código Java

1. Abre el proyecto en tu IDE favorito.
2. Verifica que las constantes de conexión al inicio de la clase `Main` coincidan con los parámetros de tu contenedor Docker:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/sistema_reservas?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
   private static final String USER = "root";
   private static final String PASSWORD = "1234";
   ```
3. Ejecuta el archivo `Main.java`.

---

## 🎮 Guía de Uso de la Aplicación

Al arrancar el programa, dispondrás de **3 intentos** para iniciar sesión. Introduce el usuario `admin` y la contraseña `1234` para acceder con permisos completos.

### Opciones Generales (Todos los usuarios):
* **[1] Generar/Actualizar reservas desde CSV local:** Lee el archivo `./data/reservas.csv`, genera un identificador único de 8 caracteres para cada vuelo y vuelca todos los registros en la tabla `RESERVA` de MySQL.
* **[2] Filtrar reservas almacenadas en BD:** Permite buscar reservas aplicando tres criterios distintos:
  1. *Por aeropuerto:* Busca coincidencias tanto en el aeropuerto de origen como en el de destino (ej. `MAD`, `BCN`).
  2. *Por rango de fechas:* Muestra vuelos comprendidos entre dos fechas en formato `YYYY-MM-DD`.
  3. *Por nombre de pasajero:* Realiza una búsqueda parcial o exacta por nombre.
* **[3] Buscar reserva por código en BD:** Consulta los detalles completos de una reserva introduciendo su ID exacto.
* **[4] Revisar historial de cambios (BD):** Muestra los últimos 50 registros de auditoría (fecha, hora, usuario y acción realizada).

### Opciones Exclusivas de Administrador (`[ADMIN]`):
* **[5] Crear una reserva completa:** Inserta manualmente una nueva reserva en la base de datos.
* **[6] Modificar reserva existente:** Permite editar el origen, destino, fecha o pasajero de una reserva existente (puedes pulsar `Enter` para conservar el valor actual de cualquier campo).
* **[7] Eliminar reserva completa:** Borra definitivamente una reserva mediante su código identificador.
* **[8] Crear nuevo usuario:** Registra nuevas cuentas de acceso en la tabla `USUARIO`.

### Control de Sesión:
* **[9] Cambiar de usuario:** Permite iniciar sesión con una cuenta diferente sin cerrar el programa.
* **[0] Salir del programa:** Finaliza la ejecución de manera segura.

---

## 🛠️ Comandos Útiles de Docker

Si necesitas gestionar el contenedor de la base de datos desde tu terminal:

* **Comprobar que el contenedor está activo y saludable (`healthy`):**
  ```bash
  docker ps
  ```
* **Detener la base de datos sin perder los datos guardados:**
  ```bash
  docker compose stop
  ```
* **Apagar y eliminar el contenedor (conservando el volumen de datos):**
  ```bash
  docker compose down
  ```
* **Reiniciar la base de datos completamente desde cero (borrando datos y volviendo a ejecutar `init.sql`):**
  ```bash
  docker compose down -v && docker compose up -d
  ```