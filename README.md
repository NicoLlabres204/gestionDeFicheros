==================================================

SISTEMA DE GESTIÓN Y PROCESAMIENTO DE RESERVAS

==================================================



DESCRIPCIÓN GENERAL:

Este programa en Java permite gestionar, factorizar, filtrar y consultar reservas de vuelos a partir de archivos en formato CSV. El sistema procesa los datos originales, simplifica la información de los trayectos y facilita búsquedas avanzadas por consola.



--------------------------------------------------

ESTRUCTURA DE ARCHIVOS Y CARPETAS:

--------------------------------------------------

- ./data/reservas.csv                     -> Archivo de entrada con los datos originales. La estructura debe ser "origen (ISO)", "destino (ISO)", "compañia", "fecha", "nombre Pasajero"

- ./data/listaReservas/                   -> Directorio que contiene los resultados procesados.

- ./data/listaReservas/Reservas Factorizadas.csv -> Archivo principal generado con los datos optimizados.

- ./data/listaReservas/Reservas_Filtradas_*.csv  -> Archivos generados tras aplicar los diferentes filtros.



--------------------------------------------------

FUNCIONALIDADES PRINCIPALES:

--------------------------------------------------



1. Factorización de Reservas (`generarArchivo`):

   - Lee el archivo original `./data/reservas.csv`.

   - Genera un código aleatorio único (ID de 8 caracteres) para cada reserva.

   - Agrupa los parámetros del trayecto bajo una estructura simplificada: "Compañía-Origen-Destino-Fecha".

   - Guarda el resultado en `./data/listaReservas/Reservas Factorizadas.csv`.



2. Menú de Filtros (`generarFiltro`):

   - Muestra un menú interactivo para seleccionar el criterio de búsqueda deseado:

     * Opción 1: Por país (Código ISO, buscando coincidencias en el trayecto).

     * Opción 2: Por rango de fechas (Aplica un filtro temporal entre una fecha de inicio y fin).

     * Opción 3: Por nombre de pasajero (Busca coincidencias parciales o totales).

   - Genera automáticamente un nuevo archivo CSV con las coincidencias encontradas.



3. Consulta Directa por Código (`buscarCodigo`):

   - Permite introducir por consola el código de reserva exacto.

   - Lee el archivo factorizado y muestra directamente por pantalla los detalles de la reserva sin crear archivos nuevos.



--------------------------------------------------

REQUISITOS TÉCNICOS:

--------------------------------------------------

- Java 8 o superior (se requiere soporte para java.nio.file y java.time).
