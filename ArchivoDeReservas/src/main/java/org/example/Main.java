package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    static Scanner sc = new Scanner(System.in);

    @FunctionalInterface
    private interface CriterioFiltro {boolean cumple(String[] partes, String valor);}

    /*            * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /* ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /*    MAIN    * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */

    public static void main(String[] args) {
        boolean on = true;

        while(on == true){
            for (int i = 0; i < 5; i++) {System.out.println("");}
            System.out.println("#=====#=====#=====#=====#=====#=====#=====#=====#=====#");
            System.out.println("#=====#=====#==#  SISTEMA DE RESERVAS  #==#=====#=====#");
            System.out.println("#=====#=====#=====#=====#=====#=====#=====#=====#=====#");
            System.out.println("| ¡Bienvenido al sistema de filtrado de reservas!     |");
            System.out.println("|                                                     |");
            System.out.println("| En este simple programa podrás filtrar tu listado   |");
            System.out.println("| de reservas en un único archivo csv.                |");
            System.out.println("#=====#=====#=====#=====#=====#=====#=====#=====#=====#");
            System.out.println("| [1] Generar archivo formateado a partir de csv      |");
            System.out.println("| [2] Filtrar archivo formateado por tipo             |");
            System.out.println("| [3] Buscar archivo formateado por código            |");
            System.out.println("|                                                     |");
            System.out.println("| [0] Salir del programa                              |");
            System.out.println("#=====#=====#=====#=====#=====#=====#=====#=====#=====#");
            System.out.print("¿Qué quieres hacer? ");

            int opcion = -1;
            try {opcion = Integer.parseInt(sc.nextLine());}
            catch (NumberFormatException e) {System.err.println("Por favor, introduzca un número válido."); return;}

            switch(opcion){
                case 0: System.out.println("¡Gracias por usar el programa!"); on = false; break;
                case 1: generarArchivo(); break;
                case 2: generarFiltro(); break;
                case 3: buscarCodigo(); break;
                default: break;
            }
            System.out.println("#=====#=====#=====#=====#=====#=====#=====#=====#=====#");
        }
    }

    /*    MAIN    * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /* ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /*  funcion1  * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */

    public static void generarArchivo() {
        Path inputPath = Paths.get("./data/reservas.csv");
        Path outputDir = Paths.get("./data/listaReservas");
        Path outputPath = outputDir.resolve("Reservas Factorizadas.csv");

        try {
            Files.createDirectories(outputDir);

            try (BufferedReader reader = Files.newBufferedReader(inputPath);
                 BufferedWriter writer = Files.newBufferedWriter(outputPath)) {

                String linea;
                boolean esCabecera = true;

                while ((linea = reader.readLine()) != null) {
                    if (esCabecera) {
                        writer.write("CodigoReserva,Compania_Origen_Destino_Fecha,NombrePasajero");
                        writer.newLine();
                        esCabecera = false;
                        continue;
                    }

                    String[] partes = linea.split(",");
                    if (partes.length < 5) continue;

                    String origen = partes[0].trim();
                    String destino = partes[1].trim();
                    String compañía = partes[2].trim();
                    String fecha = partes[3].trim();
                    String nombrePasajero = partes[4].trim();

                    String codigoReserva = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                    String infoViaje = compañía + "-" + origen + "-" + destino + "-" + fecha;

                    writer.write(String.format("%s,%s,%s",
                            codigoReserva, infoViaje, nombrePasajero));
                    writer.newLine();
                }
            }
            System.out.println("Proceso completado. Archivo guardado en: " + outputPath);
        } catch (IOException e) {
            System.err.println("Error durante el procesamiento del archivo: " + e.getMessage());
        }
    }

    /*  funcion1  * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /* ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /*  funcion2  * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */

    public static void generarFiltro() {
        System.out.println("Seleccione el tipo de filtro que desea aplicar:");
        System.out.println("1. Por país (Código ISO)");
        System.out.println("2. Por rango de fechas");
        System.out.println("3. Por nombre de pasajero");
        System.out.println("");
        System.out.print("¿Qué quieres hacer? ");

        int opcion = -1;
        try {opcion = Integer.parseInt(sc.nextLine());}
        catch (NumberFormatException e) {System.err.println("Por favor, introduzca un número válido."); return;}

        switch (opcion) {
            case 1:
                System.out.print("Introduzca el código ISO del país (ej: ES, FR, US): "); String pais = sc.nextLine();
                filtrarPorPais(pais);
                break;
            case 2:
                System.out.print("Introduzca la fecha de inicio (YYYY-MM-DD): "); String fechaInicio = sc.nextLine();
                System.out.print("Introduzca la fecha de fin (YYYY-MM-DD): "); String fechaFin = sc.nextLine();
                filtrarPorRangoFechas(fechaInicio, fechaFin);
                break;
            case 3:
                System.out.print("Introduzca el nombre o parte del nombre del pasajero: "); String pasajero = sc.nextLine();
                filtrarPorPasajero(pasajero);
                break;
            default: System.out.println("Opción no válida."); break;
        }
    }

    /* -  -  -  - * -  -  -  - * -  -  -  - * -  -  -  - * -  -  -  - * -  -  -  - * -  -  -  - * -  -  -  - * -  -  -  - */

    public static void filtrarPorPasajero(String nombreBuscado) {
        ejecutarFiltro("Pasajero", nombreBuscado, (partes, valor) -> {
            String nombrePasajero = partes[2];
            return nombrePasajero.toLowerCase().contains(valor.toLowerCase());
        });
    }

    public static void filtrarPorPais(String codigoIso) {
        ejecutarFiltro("Pais", codigoIso, (partes, valor) -> {
            String infoViaje = partes[1];
            return infoViaje.contains("-" + valor.toUpperCase() + "-") || infoViaje.contains("-" + valor.toUpperCase());
        });
    }

    public static void filtrarPorRangoFechas(String fechaInicioStr, String fechaFinStr) {
        try {
            LocalDate inicio = LocalDate.parse(fechaInicioStr);
            LocalDate fin = LocalDate.parse(fechaFinStr);

            ejecutarFiltro("Fechas", fechaInicioStr + "_a_" + fechaFinStr, (partes, valor) -> {
                try {
                    String infoViaje = partes[1];
                    String fechaStr = infoViaje.substring(infoViaje.length() - 10);

                    LocalDate fechaViaje = LocalDate.parse(fechaStr);

                    return !fechaViaje.isBefore(inicio) && !fechaViaje.isAfter(fin);
                } catch (Exception e) {
                    return false;
                }
            });
        } catch (Exception e) {
            System.err.println("Error en el formato de las fechas proporcionadas. Use YYYY-MM-DD.");
        }
    }

    /* -  -  -  - * -  -  -  - * -  -  -  - * -  -  -  - * -  -  -  - * -  -  -  - * -  -  -  - * -  -  -  - * -  -  -  - */

    private static void ejecutarFiltro(String tipoFiltro, String valorFiltro, CriterioFiltro criterio) {
        Path inputPath = Paths.get("./data/listaReservas/Reservas Factorizadas.csv");
        Path outputPath = Paths.get("./data/listaReservas/Reservas_Filtradas_" + tipoFiltro + ".csv");

        try (BufferedReader reader = Files.newBufferedReader(inputPath);
             BufferedWriter writer = Files.newBufferedWriter(outputPath)) {

            String linea;
            boolean esCabecera = true;
            int contadorCoincidencias = 0;

            while ((linea = reader.readLine()) != null) {
                if (esCabecera) {
                    writer.write(linea);
                    writer.newLine();
                    esCabecera = false;
                    continue;
                }

                String[] partes = linea.split(",");
                if (partes.length < 3) continue;

                if (criterio.cumple(partes, valorFiltro)) {
                    writer.write(linea);
                    writer.newLine();
                    contadorCoincidencias++;
                }
            }
            System.out.println("\nFiltro completado. Se encontraron " + contadorCoincidencias + " registros.");
            System.out.println("Archivo guardado en: " + outputPath);

        } catch (IOException e) {
            System.err.println("Error al procesar el archivo filtrado: " + e.getMessage());
        }
    }

    /*  funcion2  * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /* ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /*  funcion3  * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */

    public static void buscarCodigo() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Introduzca el código de reserva a consultar: ");
        String codigoBuscado = scanner.nextLine().trim();

        Path inputPath = Paths.get("./data/listaReservas/Reservas Factorizadas.csv");

        if (!Files.exists(inputPath)) {
            System.err.println("No se encuentra el archivo 'Reservas Factorizadas.csv' en la ruta especificada.");
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(inputPath)) {
            String linea;
            boolean esCabecera = true;
            boolean encontrada = false;

            String cabecera = "";
            while ((linea = reader.readLine()) != null) {
                if (esCabecera) {
                    cabecera = linea;
                    esCabecera = false;
                    continue;
                }

                String[] partes = linea.split(",");
                if (partes.length > 0 && partes[0].trim().equalsIgnoreCase(codigoBuscado)) {
                    encontrada = true;

                    System.out.println("\n--- Reserva Encontrada ---");
                    System.out.println("Código de Reserva: " + partes[0]);
                    System.out.println("Detalles del Viaje (Compañía-Origen-Destino-Fecha): " + partes[1]);
                    System.out.println("Nombre del Pasajero: " + partes[2]);
                    break;
                }
            }

            if (!encontrada) {
                System.out.println("No se ha encontrado ninguna reserva con el código: " + codigoBuscado);
            }

        } catch (IOException e) {
            System.err.println("Error al leer el archivo de reservas: " + e.getMessage());
        }
    }

    /*  funcion3  * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /* ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /* ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
}