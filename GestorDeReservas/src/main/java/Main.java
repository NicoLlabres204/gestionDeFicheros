import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.UUID;

public class Main {
    static Scanner sc = new Scanner(System.in);
    static String usuarioActual = "";

    private static final String URL = "jdbc:mysql://localhost:3306/sistema_reservas?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "contraBBDD";

    /*            * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /* ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /*    MAIN    * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */

    public static void main(String[] args) {
        System.out.println("#=====#=====#=====#=====#=====#=====#=====#=====#=====#");
        System.out.println("#=====#=====#==#  CONTROL DE ACCESO    #==#=====#=====#");
        System.out.println("#=====#=====#=====#=====#=====#=====#=====#=====#=====#");

        boolean autenticado = false;
        int intentos = 3;

        while (intentos > 0 && !autenticado) {
            autenticado = validarUsuario();
            if (!autenticado) {
                intentos--;
                if (intentos > 0) {
                    System.out.println("Intentos restantes: " + intentos + "\n");
                }
            }
        }

        if (!autenticado) {
            System.out.println("\nSe han agotado los intentos. Saliendo del programa.");
            return;
        }

        boolean on = true;
        while(on == true){
            for (int i = 0; i < 3; i++) {System.out.println("");}
            System.out.println("#=====#=====#=====#=====#=====#=====#=====#=====#=====#");
            System.out.println("#=====#=====#==#  SISTEMA DE RESERVAS  #==#=====#=====#");
            System.out.println("#=====#=====#=====#=====#=====#=====#=====#=====#=====#");
            System.out.println("| ¡Bienvenido al sistema de filtrado de reservas!     |");
            System.out.println("| Usuario actual: " + usuarioActual + (esAdmin() ? "[ADMIN]" : "       ") + "                        |");
            System.out.println("#=====#=====#=====#=====#=====#=====#=====#=====#=====#");
            System.out.println("| [1] Generar/Actualizar reservas desde CSV local     |");
            System.out.println("| [2] Filtrar reservas almacenadas en BD              |");
            System.out.println("| [3] Buscar reserva por código en BD                 |");
            System.out.println("| [4] Revisar historial de cambios (BD)               |");

            if (esAdmin()) {
                System.out.println("| [5] [ADMIN] Crear una reserva completa              |");
                System.out.println("| [6] [ADMIN] Modificar reserva existente             |");
                System.out.println("| [7] [ADMIN] Eliminar reserva completa               |");
                System.out.println("| [8] [ADMIN] Crear nuevo usuario                     |");
            }

            System.out.println("| [9] Cambiar de usuario / Iniciar sesión de nuevo    |");
            System.out.println("| [0] Salir del programa                              |");
            System.out.println("#=====#=====#=====#=====#=====#=====#=====#=====#=====#");
            System.out.print("¿Qué quieres hacer? ");

            int opcion = -1;
            try {opcion = Integer.parseInt(sc.nextLine());}
            catch (NumberFormatException e) {System.err.println("Por favor, introduzca un número válido."); continue;}

            switch(opcion){
                case 0: System.out.println("¡Gracias por usar el programa!"); on = false; break;
                case 1: migrarCsvAReservasSql(); break;
                case 2: generarFiltroSql(); break;
                case 3: buscarCodigoSql(); break;
                case 4: revisarHistorialSql(); break;
                case 5: if (esAdmin()) crearReservaSql(); else System.out.println("Opción no válida."); break;
                case 6: if (esAdmin()) modificarReservaSql(); else System.out.println("Opción no válida."); break;
                case 7: if (esAdmin()) eliminarReservaSql(); else System.out.println("Opción no válida."); break;
                case 8: if (esAdmin()) crearUsuarioSql(); else System.out.println("Opción no válida."); break;
                case 9: if (validarUsuario()) {System.out.println("Sesión cambiada correctamente."); break;}
                default: System.out.println("Opción no válida."); break;
            }
            System.out.println("#=====#=====#=====#=====#=====#=====#=====#=====#=====#");
        }
    }

    /*    MAIN    * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /* ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /*  funcion1  * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */

    public static void migrarCsvAReservasSql() {
        Path inputPath = Paths.get("./data/reservas.csv");
        if (!Files.exists(inputPath)) {
            System.err.println("No se encuentra el archivo './data/reservas.csv'.");
            return;
        }

        String sql = "INSERT INTO RESERVA (idReserva, pasajero, aeropuertoOrigen, aeropuertoDestino, fecha) VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE pasajero = VALUES(pasajero), aeropuertoOrigen = VALUES(aeropuertoOrigen), aeropuertoDestino = VALUES(aeropuertoDestino), fecha = VALUES(fecha)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             BufferedReader reader = Files.newBufferedReader(inputPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String linea;
            boolean esCabecera = true;
            int contador = 0;

            while ((linea = reader.readLine()) != null) {
                if (esCabecera) {
                    esCabecera = false;
                    continue;
                }

                String[] partes = linea.split(",");
                if (partes.length < 5) continue;

                String origen = partes[0].trim();
                String destino = partes[1].trim();
                String fecha = partes[3].trim();
                String nombrePasajero = partes[4].trim();

                String idReserva = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                pstmt.setString(1, idReserva);
                pstmt.setString(2, nombrePasajero);
                pstmt.setString(3, origen);
                pstmt.setString(4, destino);
                pstmt.setDate(5, Date.valueOf(fecha));
                pstmt.executeUpdate();
                contador++;
            }

            registrarHistorial("Importación masiva de " + contador + " reservas desde CSV a MySQL.");
            System.out.println("¡Proceso completado! Se han volcado " + contador + " registros a la base de datos.");

        } catch (Exception e) {
            System.err.println("Error al migrar reservas a MySQL: " + e.getMessage());
        }
    }

    /*  funcion1  * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /* ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /*  funcion2  * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */

    public static void generarFiltroSql() {
        System.out.println("Seleccione el tipo de filtro que desea aplicar:");
        System.out.println("1. Por aeropuerto (Origen o Destino)");
        System.out.println("2. Por rango de fechas");
        System.out.println("3. Por nombre de pasajero");
        System.out.print("¿Qué quieres hacer? ");

        int opcion;
        try {
            opcion = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Número no válido.");
            return;
        }

        String sqlWhere = "";
        String valorParametro1 = "";
        String valorParametro2 = "";
        String tipoFiltro = "";

        switch (opcion) {
            case 1:
                System.out.print("Introduzca el código del aeropuerto (ej: MAD, BCN): ");
                valorParametro1 = sc.nextLine().trim().toUpperCase();
                sqlWhere = "aeropuertoOrigen = ? OR aeropuertoDestino = ?";
                tipoFiltro = "Aeropuerto_" + valorParametro1;
                ejecutarFiltroDobleSql(sqlWhere, valorParametro1, valorParametro1, tipoFiltro);
                return;
            case 2:
                System.out.print("Introduzca la fecha de inicio (YYYY-MM-DD): ");
                String inicio = sc.nextLine().trim();
                System.out.print("Introduzca la fecha de fin (YYYY-MM-DD): ");
                String fin = sc.nextLine().trim();
                sqlWhere = "fecha BETWEEN ? AND ?";
                tipoFiltro = "Fechas_" + inicio + "_a_" + fin;
                ejecutarFiltroFechasSql(sqlWhere, inicio, fin, tipoFiltro);
                return;
            case 3:
                System.out.print("Introduzca el nombre o parte del pasajero: ");
                valorParametro1 = sc.nextLine().trim();
                sqlWhere = "pasajero LIKE ?";
                valorParametro1 = "%" + valorParametro1 + "%";
                tipoFiltro = "Pasajero";
                ejecutarFiltroSimpleSql(sqlWhere, valorParametro1, tipoFiltro);
                return;
            default:
                System.out.println("Opción no válida.");
                return;
        }
    }

    /* -  -  -  - * -  -  -  - * -  -  -  - * -  -  -  - * -  -  -  - * -  -  -  - * -  -  -  - * -  -  -  - * -  -  -  - */

    private static void ejecutarFiltroSimpleSql(String condicionWhere, String parametro, String tipoFiltro) {
        String sql = "SELECT idReserva, pasajero, aeropuertoOrigen, aeropuertoDestino, fecha FROM RESERVA WHERE " + condicionWhere;
        int contador = 0;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, parametro);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n--- RESULTADOS DEL FILTRO ---");
                while (rs.next()) {
                    System.out.printf("%s, %s, %s -> %s (%s)%n",
                            rs.getString("idReserva"),
                            rs.getString("pasajero"),
                            rs.getString("aeropuertoOrigen"),
                            rs.getString("aeropuertoDestino"),
                            rs.getDate("fecha").toString());
                    contador++;
                }
            }
            registrarHistorial("Filtro aplicado en BD (" + tipoFiltro + "). Coincidencias: " + contador);
            System.out.println("\nFiltro completado. Se encontraron " + contador + " registros.");

        } catch (SQLException e) {
            System.err.println("Error al consultar la base de datos: " + e.getMessage());
        }
    }

    private static void ejecutarFiltroDobleSql(String condicionWhere, String param1, String param2, String tipoFiltro) {
        String sql = "SELECT idReserva, pasajero, aeropuertoOrigen, aeropuertoDestino, fecha FROM RESERVA WHERE " + condicionWhere;
        int contador = 0;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, param1);
            pstmt.setString(2, param2);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n--- RESULTADOS DEL FILTRO ---");
                while (rs.next()) {
                    System.out.printf("%s, %s, %s -> %s (%s)%n",
                            rs.getString("idReserva"),
                            rs.getString("pasajero"),
                            rs.getString("aeropuertoOrigen"),
                            rs.getString("aeropuertoDestino"),
                            rs.getDate("fecha").toString());
                    contador++;
                }
            }
            registrarHistorial("Filtro aplicado en BD (" + tipoFiltro + "). Coincidencias: " + contador);
            System.out.println("\nFiltro completado. Se encontraron " + contador + " registros.");

        } catch (SQLException e) {
            System.err.println("Error al consultar la base de datos: " + e.getMessage());
        }
    }

    private static void ejecutarFiltroFechasSql(String condicionWhere, String inicio, String fin, String tipoFiltro) {
        String sql = "SELECT idReserva, pasajero, aeropuertoOrigen, aeropuertoDestino, fecha FROM RESERVA WHERE " + condicionWhere;
        int contador = 0;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(inicio));
            pstmt.setDate(2, Date.valueOf(fin));
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n--- RESULTADOS DEL FILTRO ---");
                while (rs.next()) {
                    System.out.printf("%s, %s, %s -> %s (%s)%n",
                            rs.getString("idReserva"),
                            rs.getString("pasajero"),
                            rs.getString("aeropuertoOrigen"),
                            rs.getString("aeropuertoDestino"),
                            rs.getDate("fecha").toString());
                    contador++;
                }
            }
            registrarHistorial("Filtro aplicado en BD (" + tipoFiltro + "). Coincidencias: " + contador);
            System.out.println("\nFiltro completado. Se encontraron " + contador + " registros.");

        } catch (SQLException e) {
            System.err.println("Error al consultar la base de datos: " + e.getMessage());
        }
    }

    /*  funcion2  * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /* ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /*  funcion3  * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */

    public static void buscarCodigoSql() {
        System.out.print("Introduzca el código de reserva a consultar: ");
        String codigoBuscado = sc.nextLine().trim();

        String sql = "SELECT idReserva, pasajero, aeropuertoOrigen, aeropuertoDestino, fecha FROM RESERVA WHERE idReserva = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, codigoBuscado);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n--- Reserva Encontrada ---");
                    System.out.println("ID Reserva: " + rs.getString("idReserva"));
                    System.out.println("Pasajero: " + rs.getString("pasajero"));
                    System.out.println("Origen: " + rs.getString("aeropuertoOrigen"));
                    System.out.println("Destino: " + rs.getString("aeropuertoDestino"));
                    System.out.println("Fecha: " + rs.getDate("fecha").toString());
                    registrarHistorial("Consulta de reserva por código en BD: " + codigoBuscado);
                } else {
                    System.out.println("No se ha encontrado ninguna reserva con el código: " + codigoBuscado);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en la base de datos: " + e.getMessage());
        }
    }

    /*  funcion3  * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /* ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /* funcionADM * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */

    public static boolean esAdmin() {
        String sql = "SELECT idUsuario FROM USUARIO ORDER BY idUsuario ASC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("idUsuario").equals(usuarioActual);
            }
        } catch (SQLException e) {
            // Error silencioso
        }
        return false;
    }

    public static void crearReservaSql() {
        System.out.print("Introduzca el aeropuerto de origen (ej: MAD): ");
        String origen = sc.nextLine().trim();
        System.out.print("Introduzca el aeropuerto de destino (ej: BCN): ");
        String destino = sc.nextLine().trim();
        System.out.print("Introduzca la fecha (YYYY-MM-DD): ");
        String fecha = sc.nextLine().trim();
        System.out.print("Introduzca el nombre del pasajero: ");
        String pasajero = sc.nextLine().trim();

        String idReserva = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String sql = "INSERT INTO RESERVA (idReserva, pasajero, aeropuertoOrigen, aeropuertoDestino, fecha) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, idReserva);
            pstmt.setString(2, pasajero);
            pstmt.setString(3, origen);
            pstmt.setString(4, destino);
            pstmt.setDate(5, Date.valueOf(fecha));
            pstmt.executeUpdate();

            registrarHistorial("Creación de reserva en BD. ID: " + idReserva);
            System.out.println("¡Reserva creada con éxito en MySQL! Código: " + idReserva);

        } catch (SQLException e) {
            System.err.println("Error al insertar reserva en MySQL: " + e.getMessage());
        }
    }

    public static void modificarReservaSql() {
        System.out.print("Introduzca el código (ID) de la reserva que desea modificar: ");
        String codigoBuscado = sc.nextLine().trim();

        String sqlSelect = "SELECT aeropuertoOrigen, aeropuertoDestino, fecha, pasajero FROM RESERVA WHERE idReserva = ?";
        String origenActual = "", destinoActual = "", pasajeroActual = "";
        Date fechaActual = null;

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sqlSelect)) {

            pstmt.setString(1, codigoBuscado);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    origenActual = rs.getString("aeropuertoOrigen");
                    destinoActual = rs.getString("aeropuertoDestino");
                    fechaActual = rs.getDate("fecha");
                    pasajeroActual = rs.getString("pasajero");
                    System.out.println("Actual -> Origen: " + origenActual + " | Destino: " + destinoActual + " | Fecha: " + fechaActual + " | Pasajero: " + pasajeroActual);
                } else {
                    System.out.println("No se encontró ninguna reserva con ese código.");
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error de lectura: " + e.getMessage());
            return;
        }

        System.out.print("Nuevo origen [Enter para mantener]: ");
        String nuevoOrigen = sc.nextLine().trim();
        System.out.print("Nuevo destino [Enter para mantener]: ");
        String nuevoDestino = sc.nextLine().trim();
        System.out.print("Nueva fecha YYYY-MM-DD [Enter para mantener]: ");
        String nuevaFecha = sc.nextLine().trim();
        System.out.print("Nuevo pasajero [Enter para mantener]: ");
        String nuevoPasajero = sc.nextLine().trim();

        String origenFinal = nuevoOrigen.isEmpty() ? origenActual : nuevoOrigen;
        String destinoFinal = nuevoDestino.isEmpty() ? destinoActual : nuevoDestino;
        LocalDate fechaFinal = nuevaFecha.isEmpty() ? fechaActual.toLocalDate() : LocalDate.parse(nuevaFecha);
        String pasajeroFinal = nuevoPasajero.isEmpty() ? pasajeroActual : nuevoPasajero;

        String sqlUpdate = "UPDATE RESERVA SET aeropuertoOrigen = ?, aeropuertoDestino = ?, fecha = ?, pasajero = ? WHERE idReserva = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {

            pstmt.setString(1, origenFinal);
            pstmt.setString(2, destinoFinal);
            pstmt.setDate(3, Date.valueOf(fechaFinal));
            pstmt.setString(4, pasajeroFinal);
            pstmt.setString(5, codigoBuscado);
            pstmt.executeUpdate();

            registrarHistorial("Modificación de reserva en BD. ID: " + codigoBuscado);
            System.out.println("Reserva actualizada correctamente en MySQL.");

        } catch (SQLException e) {
            System.err.println("Error al actualizar: " + e.getMessage());
        }
    }

    public static void eliminarReservaSql() {
        System.out.print("Introduzca el código (ID) de la reserva que desea eliminar: ");
        String codigoBuscado = sc.nextLine().trim();

        String sql = "DELETE FROM RESERVA WHERE idReserva = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, codigoBuscado);
            int filasAfectadas = pstmt.executeUpdate();

            if (filasAfectadas > 0) {
                registrarHistorial("Eliminación de reserva en BD. ID: " + codigoBuscado);
                System.out.println("Reserva eliminada correctamente de MySQL.");
            } else {
                System.out.println("No se encontró ninguna reserva con el código: " + codigoBuscado);
            }

        } catch (SQLException e) {
            System.err.println("Error al eliminar: " + e.getMessage());
        }
    }

    public static void crearUsuarioSql() {
        System.out.print("Introduzca el nombre del nuevo usuario (idUsuario): ");
        String nuevoUser = sc.nextLine().trim();
        System.out.print("Introduzca la contraseña: ");
        String nuevoPass = sc.nextLine().trim();

        if (nuevoUser.isEmpty() || nuevoPass.isEmpty()) {
            System.out.println("Campos vacíos no permitidos.");
            return;
        }

        String sql = "INSERT INTO USUARIO (idUsuario, password) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nuevoUser);
            pstmt.setString(2, nuevoPass);
            pstmt.executeUpdate();

            registrarHistorial("Creación de usuario en BD: " + nuevoUser);
            System.out.println("¡Usuario creado con éxito en MySQL!");

        } catch (SQLException e) {
            System.err.println("Error al crear usuario (quizás ya exista): " + e.getMessage());
        }
    }

    /* funcionADM * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /* ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /*  historial * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */

    public static void registrarHistorial(String accion) {
        // Nota: Puedes crear una tabla `historial` si lo deseas, o registrarlo directamente:
        String sql = "CREATE TABLE IF NOT EXISTS HISTORIAL (id INT AUTO_INCREMENT PRIMARY KEY, fecha_hora DATETIME, usuario VARCHAR(50), accion TEXT)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);

            String insertSql = "INSERT INTO HISTORIAL (fecha_hora, usuario, accion) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                pstmt.setString(2, usuarioActual.isEmpty() ? "SISTEMA" : usuarioActual);
                pstmt.setString(3, accion);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            // Error silencioso en log
        }
    }

    public static void revisarHistorialSql() {
        String sql = "SELECT fecha_hora, usuario, accion FROM HISTORIAL ORDER BY id DESC LIMIT 50";

        System.out.println("\n--- HISTORIAL DE CAMBIOS Y ACCESOS (MySQL) ---");
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                System.out.printf("[%s] Usuario: %s - Acción: %s%n",
                        rs.getTimestamp("fecha_hora").toString(),
                        rs.getString("usuario"),
                        rs.getString("accion"));
            }

        } catch (SQLException e) {
            System.out.println("No hay historial registrado todavía o la tabla no existe.");
        }
    }

    /*  historial * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /* ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /*  validar   * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */

    public static boolean validarUsuario() {
        System.out.print("Introduzca su usuario: ");
        String usuarioIngresado = sc.nextLine().trim();

        System.out.print("Introduzca su contraseña: ");
        String passwordIngresada = sc.nextLine().trim();

        String sql = "SELECT password FROM USUARIO WHERE idUsuario = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuarioIngresado);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String passwordDb = rs.getString("password");
                    if (passwordDb.equals(passwordIngresada)) {
                        usuarioActual = usuarioIngresado;
                        registrarHistorial("Inicio de sesión exitoso.");
                        System.out.println("\n¡Acceso concedido! Bienvenido, " + usuarioActual + ".");
                        return true;
                    }
                }
            }

            System.out.println("\nAcceso denegado. Usuario o contraseña incorrectos.");
            return false;

        } catch (SQLException e) {
            System.err.println("Error de conexión con MySQL: " + e.getMessage());
            return false;
        }
    }

    /*  validar   * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /* ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
    /* ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## * ########## */
}